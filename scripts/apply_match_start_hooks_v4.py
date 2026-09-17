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
            '        com.ultimateduels.ffa.DroppedItemClearManager.get(this.plugin).startDuelMatch(match);\n',
            1,
        )
    if 'startDuelMatch(duelMatch)' not in text:
        text = text.replace(
            '        this.activeMatches.put(matchId, duelMatch);\n',
            '        this.activeMatches.put(matchId, duelMatch);\n'
            '        com.ultimateduels.ffa.DroppedItemClearManager.get(this.plugin).startDuelMatch(duelMatch);\n',
            1,
        )
    # Party FFA matches also have their own arena/match scope.
    if 'startDuelMatch(match);' not in text:
        text = text.replace(
            '        this.activeMatches.put(matchId, match);\n',
            '        this.activeMatches.put(matchId, match);\n'
            '        com.ultimateduels.ffa.DroppedItemClearManager.get(this.plugin).startDuelMatch(match);\n',
            1,
        )
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


edit('com/ultimateduels/duel/DuelManager.java', patch_duel_manager)
edit('com/ultimateduels/ffa/FFAManager.java', patch_ffa_manager)
print('explicit match-start item cleanup hooks applied')
