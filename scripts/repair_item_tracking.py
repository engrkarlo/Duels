from pathlib import Path

path = Path('com/ultimateduels/ffa/FFAManager.java')
text = path.read_text(encoding='utf-8')
old = 'player.getWorld().dropItemNaturally(player.getLocation(), item.clone());'
new = '''org.bukkit.entity.Item dropped = player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                    this.getDroppedItemClearManager().track(dropped, player);'''
text = text.replace(old, new)
old_offhand = 'player.getWorld().dropItemNaturally(player.getLocation(), offHand.clone());'
new_offhand = '''org.bukkit.entity.Item dropped = player.getWorld().dropItemNaturally(player.getLocation(), offHand.clone());
                this.getDroppedItemClearManager().track(dropped, player);'''
text = text.replace(old_offhand, new_offhand)
path.write_text(text, encoding='utf-8')
print('tracked all FFA item drops')
