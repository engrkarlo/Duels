from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def edit(rel, fn):
    path = ROOT / rel
    text = path.read_text(encoding='utf-8')
    new = fn(text)
    if new != text:
        path.write_text(new, encoding='utf-8')
        print('patched', rel)
    else:
        print('already patched', rel)


def patch_duel_manager(text):
    if 'startDuelMatch(match)' not in text:
        text = text.replace(
            '        this.activeMatches.put(matchId, match);\n',
            '        this.activeMatches.put(matchId, match);\n'
            '        com.ultimateduels.ffa.DroppedItemClearManager.get(this.plugin).startDuelMatch(match);\n', 1)
    if 'startDuelMatch(duelMatch)' not in text:
        text = text.replace(
            '        this.activeMatches.put(matchId, duelMatch);\n',
            '        this.activeMatches.put(matchId, duelMatch);\n'
            '        com.ultimateduels.ffa.DroppedItemClearManager.get(this.plugin).startDuelMatch(duelMatch);\n', 1)
    return text


def patch_ffa_manager(text):
    if 'startFFAMatch(arenaName, arena.getArena())' not in text:
        needle = '        arena.addPlayer(uuid);\n        this.cancelArenaRegeneration(arenaName);\n'
        replacement = (
            '        arena.addPlayer(uuid);\n'
            '        if (arena.getPlayerCount() == 1) {\n'
            '            com.ultimateduels.ffa.DroppedItemClearManager.get(this.plugin).startFFAMatch(arenaName, arena.getArena());\n'
            '        }\n'
            '        this.cancelArenaRegeneration(arenaName);\n'
        )
        if needle not in text:
            raise RuntimeError('FFA join insertion point not found')
        text = text.replace(needle, replacement, 1)
    return text


def patch_manager(text):
    if 'public void startFFAMatch(String arenaName, DuelArena arena)' in text:
        return text
    marker = '    /** Compatibility hook: item drops do not create individual timers. */\n'
    method = '''    /** Starts an independent repeating cleanup cycle for an FFA arena. */
    public void startFFAMatch(String arenaName, DuelArena arena) {
        if (arenaName == null || arena == null) return;
        if (!this.plugin.getConfigManager().getConfig().getBoolean("item-clear.enabled", true)) return;
        String key = "ffa:" + arenaName.toLowerCase();
        this.nextClearAt.remove(key);
        this.warnedCycles.remove(key);
        clearArena(arena);
        this.nextClearAt.put(key, System.currentTimeMillis()
                + Math.max(1, this.plugin.getConfigManager().getConfig().getInt("item-clear.interval-seconds", 30)) * 1000L);
        ensureTask();
    }

'''
    if marker not in text:
        raise RuntimeError('cleanup manager insertion marker not found')
    return text.replace(marker, method + marker, 1)


def patch_command_reload(text):
    """Make /duels reload refresh the ConfigManager before the command handler runs."""
    if 'ULTIMATEDUELS_CONFIG_RELOAD_HOOK' in text:
        return text
    marker = '    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)\n'
    if marker not in text:
        return text
    helper = '''    // ULTIMATEDUELS_CONFIG_RELOAD_HOOK
    private void refreshPluginConfiguration() {
        try {
            Object manager = this.plugin.getConfigManager();
            if (manager == null) return;
            java.lang.reflect.Method method = null;
            for (String name : new String[]{"reload", "reloadConfig", "load", "loadConfig"}) {
                try {
                    method = manager.getClass().getMethod(name);
                    break;
                } catch (NoSuchMethodException ignored) { }
            }
            if (method != null) method.invoke(manager);
        } catch (Throwable ignored) { }
    }

'''
    text = text.replace(marker, helper + marker, 1)
    needle = '        Player player = event.getPlayer();\n'
    replacement = '''        Player player = event.getPlayer();
        String commandLine = event.getMessage().trim();
        if (commandLine.startsWith("/")) commandLine = commandLine.substring(1);
        String[] parts = commandLine.split("\\\\s+");
        if (parts.length >= 2 && "duels".equalsIgnoreCase(parts[0]) && "reload".equalsIgnoreCase(parts[1])) {
            refreshPluginConfiguration();
            return;
        }
'''
    if needle in text:
        text = text.replace(needle, replacement, 1)
    return text


def patch_manager_messages(text):
    # Do not require an item to exist before displaying the configured warning.
    text = text.replace(
        '        if (messageAt > 0 && remaining == messageAt && !this.warnedCycles.contains(key)) {\n            if (hasItems(arena)) warnParticipants(participants, remaining);\n            this.warnedCycles.add(key);\n        }',
        '        if (messageAt > 0 && remaining <= messageAt && !this.warnedCycles.contains(key)) {\n            warnParticipants(participants, remaining);\n            this.warnedCycles.add(key);\n        }'
    )
    # If a lag spike skips the exact configured second, warn on the next tick instead of missing it.
    text = text.replace(
        '            this.nextClearAt.replaceAll((key, ignored) -> now + interval * 1000L);',
        '            for (String key : new java.util.HashSet<>(this.nextClearAt.keySet())) { this.nextClearAt.put(key, now + interval * 1000L); }'
    )
    return text


edit('com/ultimateduels/duel/DuelManager.java', patch_duel_manager)
edit('com/ultimateduels/ffa/FFAManager.java', patch_ffa_manager)
edit('com/ultimateduels/ffa/DroppedItemClearManager.java', patch_manager)
edit('com/ultimateduels/ffa/DroppedItemClearManager.java', patch_manager_messages)
edit('com/ultimateduels/listeners/CommandBlockListener.java', patch_command_reload)
print('explicit match-start item cleanup hooks and reload/message fixes applied')
