from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]

def edit(path, transform):
    p = ROOT / path
    text = p.read_text(encoding='utf-8')
    new = transform(text)
    if new != text:
        p.write_text(new, encoding='utf-8')
        print('repaired', path)
    else:
        print('already compatible', path)


def player_state(text):
    text = re.sub(r'(    private final Map<UUID, PlayerState> lobbyStates;\n)(?:\1)+', r'\1', text)
    text = re.sub(r'(        this\.lobbyStates = new ConcurrentHashMap<UUID, PlayerState>\(\);\n)(?:\1)+', r'\1', text)
    text = text.replace('Attribute.GENERIC_MAX_HEALTH', 'Attribute.MAX_HEALTH')
    # CFR loses generic information when decompiling ObjectInputStream results.
    text = text.replace('Map serializedStates = (Map)ois.readObject();', 'Map<?, ?> serializedStates = (Map<?, ?>) ois.readObject();')
    text = text.replace('for (Map.Entry entry : serializedStates.entrySet()) {', 'for (Map.Entry<?, ?> entry : serializedStates.entrySet()) {')
    return text


def lobby(text):
    text = text.replace('for (String effectStr : effectList) {', 'for (Object effectObj : effectList) {\n            String effectStr = String.valueOf(effectObj);')
    text = text.replace('pdc.set(this.lobbyItemKey, PersistentDataType.BYTE, (Object)1);', 'pdc.set(this.lobbyItemKey, PersistentDataType.BYTE, (byte) 1);')
    return text


def ffa(text):
    text = text.replace('ex.getMessage()', 'String.valueOf(ex)')
    text = text.replace('throwable.getMessage()', 'String.valueOf(throwable)')
    # CFR can infer rewardsList as ArrayList<Object>; normalize to CharSequence values.
    text = text.replace('message.append(String.join((CharSequence)" \\u00a77| ", rewardsList));', 'message.append(String.join(" \\u00a77| ", rewardsList.stream().map(String::valueOf).toList()));')
    text = re.sub(r'message\.append\(String\.join\((?:CharSequence)?\s*"([^\"]*)", rewardsList\)\);', r'message.append(String.join("\1", rewardsList.stream().map(String::valueOf).toList()));', text)
    return text


def command_blocker(text):
    return text.replace('public final class CommandBlockListener {', 'public final class CommandBlockListener implements Listener {')

edit('com/ultimateduels/player/PlayerStateManager.java', player_state)
edit('com/ultimateduels/lobby/LobbyManager.java', lobby)
edit('com/ultimateduels/ffa/FFAManager.java', ffa)
edit('com/ultimateduels/listeners/CommandBlockListener.java', command_blocker)
