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
    if 'startDuelMatch(match);' not in text:
        text = text.replace(
            '        this.activeMatches.put(matchId, match);\n',
            '        this.activeMatches.put(matchId, match);\n'
            '        com.ultimateduels.ffa.DroppedItemClearManager.get(this.plugin).startDuelMatch(match);\n', 1)
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


edit('com/ultimateduels/duel/DuelManager.java', patch_duel_manager)
edit('com/ultimateduels/ffa/FFAManager.java', patch_ffa_manager)
edit('com/ultimateduels/ffa/DroppedItemClearManager.java', patch_manager)
print('explicit match-start item cleanup hooks applied')
