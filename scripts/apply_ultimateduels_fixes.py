from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]


def edit(rel, fn):
    path = ROOT / rel
    text = path.read_text(encoding="utf-8")
    new = fn(text)
    if new == text:
        raise RuntimeError(f"No changes made to {rel}; source layout did not match")
    path.write_text(new, encoding="utf-8")
    print(f"patched {rel}")


# Paper 26.2 NMS fields may no longer be public. Use declared-field fallback.
def patch_health(text):
    replacements = {
        'vec3Zero = clazz.getField("ZERO").get(null);': 'vec3Zero = getStaticField(clazz, "ZERO");',
        'entityTypeTextDisplay = entityTypeClass.getField("TEXT_DISPLAY").get(null);': 'entityTypeTextDisplay = getStaticField(entityTypeClass, "TEXT_DISPLAY");',
        'serByte = serializersClass.getField("BYTE").get(null);': 'serByte = getStaticField(serializersClass, "BYTE");',
        'serInt = serializersClass.getField("INT").get(null);': 'serInt = getStaticField(serializersClass, "INT");',
        'serFloat = serializersClass.getField("FLOAT").get(null);': 'serFloat = getStaticField(serializersClass, "FLOAT");',
        'serComponent = serializersClass.getField("COMPONENT").get(null);': 'serComponent = getStaticField(serializersClass, "COMPONENT");',
        'streamCodecTeleport = teleportClass.getField("STREAM_CODEC").get(null);': 'streamCodecTeleport = getStaticField(teleportClass, "STREAM_CODEC");',
    }
    for old, new in replacements.items():
        text = text.replace(old, new)
    marker = '    private static Field findFieldByTypeName(Class<?> clazz, String typeNameContains) {'
    helper = '''    private static Object getStaticField(Class<?> type, String name) throws Exception {
        try {
            Field field = type.getField(name);
            field.setAccessible(true);
            return field.get(null);
        } catch (NoSuchFieldException ignored) {
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(null);
        }
    }

'''
    if 'private static Object getStaticField(Class<?> type, String name)' not in text:
        text = text.replace(marker, helper + marker)
    return text


edit("com/ultimateduels/visuals/HealthPacketSender.java", patch_health)


# The old listener treated every non-duel/non-FFA world as the lobby.
def patch_block_protection(text):
    old = '''    private boolean isInLobby(Player player) {
        UUID playerUUID = player.getUniqueId();
        DuelManager duelManager = this.plugin.getDuelManager();
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (duelManager == null || ffaManager == null) {
            return true;
        }
        return !duelManager.isInMatch(playerUUID) && !ffaManager.isInFFA(playerUUID) && !duelManager.isSpectating(playerUUID);
    }'''
    new = '''    private boolean isInLobby(Player player) {
        if (player == null || this.plugin.getLobbyManager() == null) {
            return false;
        }
        return this.plugin.getLobbyManager().isInLobbyWorld(player);
    }'''
    if old not in text:
        raise RuntimeError("BlockProtectionListener lobby method not found")
    text = text.replace(old, new)
    # Bucket and hanging restrictions must also be lobby-only outside active matches/FFA.
    text = text.replace('''        if (!player.hasPermission("ultimateduels.admin.build")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBucketFill''', '''        if (this.isInLobby(player) && !player.hasPermission("ultimateduels.admin.build")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBucketFill''', 1)
    text = text.replace('''        if (!player.hasPermission("ultimateduels.admin.build")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onPlayerInteract''', '''        if (this.isInLobby(player) && !player.hasPermission("ultimateduels.admin.build")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onPlayerInteract''', 1)
    text = text.replace('''        Player player = (Player)entity;
        if (!player.hasPermission("ultimateduels.admin.build")) {
            event.setCancelled(true);
        }''', '''        Player player = (Player)entity;
        if (this.isInLobby(player) && !player.hasPermission("ultimateduels.admin.build")) {
            event.setCancelled(true);
        }''')
    return text


edit("com/ultimateduels/listeners/BlockProtectionListener.java", patch_block_protection)


# FFA death drops: allow lethal damage to reach PlayerDeathEvent when enabled,
# and preserve the old no-drop behavior when disabled.
def patch_death(text):
    # In the lethal FFA damage branch, only cancel/handle manually when drops are disabled.
    old = '''        if (inFFA) {
            event.setCancelled(true);
            Player finalKiller = killer;'''
    new = '''        if (inFFA) {
            boolean dropItems = this.ffaManager != null && this.ffaManager.shouldDropItemsOnDeath();
            if (dropItems) {
                return;
            }
            event.setCancelled(true);
            Player finalKiller = killer;'''
    if old not in text:
        raise RuntimeError("FFA lethal-damage branch not found")
    text = text.replace(old, new, 1)
    old2 = '''        if (this.ffaManager != null && this.ffaManager.isInFFAArena(victim)) {
            event.setDeathMessage(null);
            event.setDroppedExp(0);
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();'''
    new2 = '''        if (this.ffaManager != null && this.ffaManager.isInFFAArena(victim)) {
            event.setDeathMessage(null);
            event.setDroppedExp(0);
            boolean dropItems = this.ffaManager.shouldDropItemsOnDeath();
            event.setKeepInventory(!dropItems);
            event.setKeepLevel(true);
            if (!dropItems) {
                event.getDrops().clear();
            }'''
    if old2 not in text:
        raise RuntimeError("FFA PlayerDeathEvent branch not found")
    text = text.replace(old2, new2, 1)
    return text


edit("com/ultimateduels/listeners/PlayerDeathListener.java", patch_death)


# Add the setting/helper to FFAManager.
def patch_ffa(text):
    text = text.replace('    private boolean rekitOnKill;\n', '    private boolean rekitOnKill;\n    private boolean deathItemsDrop;\n', 1)
    needle = '        this.rekitOnKill = config.getBoolean("ffa.kill-rewards.rekit-on-kill", true);'
    if 'ffa.death-items-drop' not in text:
        text = text.replace(needle, needle + '\n        this.deathItemsDrop = config.getBoolean("ffa.death-items-drop", false);', 1)
    marker = '    public boolean isInFFA('
    if 'boolean shouldDropItemsOnDeath()' not in text and marker in text:
        text = text.replace(marker, '    public boolean shouldDropItemsOnDeath() {\n        return this.deathItemsDrop;\n    }\n\n' + marker, 1)
    return text


edit("com/ultimateduels/ffa/FFAManager.java", patch_ffa)


# Add a documented default config fragment without overwriting the original config.
fragment = ROOT / "ULTIMATEDUELS-FIXES-CONFIG.yml"
fragment.write_text('''# Merge these keys into UltimateDuels/config.yml\n# FFA death item behavior\nffa:\n  death-items-drop: false\n\n# World-specific command blocking. Commands are checked by their root label,\n# including commands registered by other plugins.\ncommand-blocking:\n  enabled: false\n  worlds:\n    - lobby\n  commands:\n    - pl\n    - plugins\n    - version\n    - ver\n    - help\n    - spawn\n    - home\n    - tpa\n    - tpaccept\n    - tpadeny\n    - tp\n    - pwarp\n\n# FFA combat logging message\ncombat-log:\n  message: '&cYou are in combat for &e{time}s&c!'\n  display: action-bar\n''', encoding='utf-8')
print("wrote ULTIMATEDUELS-FIXES-CONFIG.yml")
''