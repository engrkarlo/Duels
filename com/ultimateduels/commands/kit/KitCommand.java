/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package com.ultimateduels.commands.kit;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.BaseCommand;
import com.ultimateduels.kit.model.DuelKit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitCommand
extends BaseCommand {
    private static final List<String> SUBCOMMANDS = Arrays.asList("create", "delete", "edit", "list", "info", "give", "load", "save", "seticon", "rename", "copy");

    public KitCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.kit");
    }

    @Override
    protected void execute(Player player, String[] args) {
        String subCommand;
        if (this.plugin.getKitManager() == null) {
            this.sendMessage(player, "&cKit system is not available!");
            return;
        }
        if (args.length == 0) {
            this.sendHelp(player);
            return;
        }
        switch (subCommand = args[0].toLowerCase()) {
            case "create": {
                this.handleCreate(player, args);
                break;
            }
            case "delete": 
            case "remove": {
                this.handleDelete(player, args);
                break;
            }
            case "edit": {
                this.handleEdit(player, args);
                break;
            }
            case "list": {
                this.handleList(player);
                break;
            }
            case "info": {
                this.handleInfo(player, args);
                break;
            }
            case "give": {
                this.handleGive(player, args);
                break;
            }
            case "load": {
                this.handleLoad(player, args);
                break;
            }
            case "save": {
                this.handleSave(player, args);
                break;
            }
            case "seticon": {
                this.handleSetIcon(player, args);
                break;
            }
            case "rename": {
                this.handleRename(player, args);
                break;
            }
            case "copy": 
            case "clone": {
                this.handleCopy(player, args);
                break;
            }
            default: {
                DuelKit kit = this.plugin.getKitManager().getAdminKit(subCommand);
                if (kit != null) {
                    this.handleLoad(player, new String[]{"load", subCommand});
                    break;
                }
                this.sendHelp(player);
            }
        }
    }

    private void handleCreate(Player player, String[] args) {
        if (!player.hasPermission("ultimateduels.admin.kit")) {
            this.sendNoPermission((CommandSender)player);
            return;
        }
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/kit create <name>");
            this.sendMessage(player, "&7This will save your current inventory as a new kit.");
            return;
        }
        String name = args[1].toLowerCase();
        if (!name.matches("^[a-zA-Z0-9_-]+$")) {
            this.sendMessage(player, "&cKit name can only contain letters, numbers, underscores, and dashes!");
            return;
        }
        if (name.length() > 32) {
            this.sendMessage(player, "&cKit name cannot be longer than 32 characters!");
            return;
        }
        if (this.plugin.getKitManager().adminKitExists(name)) {
            this.sendMessage(player, "&cA kit with that name already exists!");
            this.sendMessage(player, "&7Use &e/kit save " + name + " &7to overwrite it.");
            return;
        }
        boolean created = this.plugin.getKitManager().createAdminKitFromPlayer(player, name);
        if (!created) {
            this.sendMessage(player, "&cFailed to create kit!");
            return;
        }
        DuelKit kit = this.plugin.getKitManager().getAdminKit(name);
        if (kit != null) {
            String displayName = name.substring(0, 1).toUpperCase() + name.substring(1);
            kit.setDisplayName(displayName);
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem.getType() != Material.AIR) {
                kit.setIcon(heldItem.getType());
            }
            this.plugin.getKitManager().saveAdminKits();
        }
        this.sendMessage(player, "&aKit &e" + name + " &acreated successfully!");
        this.sendMessage(player, "&7Your current inventory has been saved to this kit.");
        this.sendMessage(player, "&7Including: Armor, Inventory, and &eOffhand&7 items.");
        this.sendMessage(player, "");
        this.sendMessage(player, "&7Commands:");
        this.sendMessage(player, "&e/kit seticon " + name + " &7- Set kit icon");
        this.sendMessage(player, "&e/kit edit " + name + " &7- Edit kit in GUI");
        this.sendMessage(player, "&e/kit info " + name + " &7- View kit info");
    }

    private void handleDelete(Player player, String[] args) {
        if (!player.hasPermission("ultimateduels.admin.kit")) {
            this.sendNoPermission((CommandSender)player);
            return;
        }
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/kit delete <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelKit kit = this.plugin.getKitManager().getAdminKit(name);
        if (kit == null) {
            this.sendMessage(player, "&cKit not found: " + name);
            return;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().getQueueSize(name) > 0) {
            this.sendMessage(player, "&cThis kit is currently in use by players in queue!");
            return;
        }
        this.plugin.getKitManager().deleteAdminKit(name);
        this.sendMessage(player, "&cKit &e" + name + " &chas been deleted!");
    }

    private void handleEdit(Player player, String[] args) {
        if (!player.hasPermission("ultimateduels.admin.kit")) {
            this.sendNoPermission((CommandSender)player);
            return;
        }
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/kit edit <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelKit kit = this.plugin.getKitManager().getAdminKit(name);
        if (kit == null) {
            this.sendMessage(player, "&cKit not found: " + name);
            return;
        }
        this.sendMessage(player, "&cGUI editor not available. Use &e/kit save " + name + " &cto update from your inventory.");
    }

    private void handleList(Player player) {
        ArrayList<DuelKit> kits = new ArrayList<DuelKit>(this.plugin.getKitManager().getAllAdminKits());
        if (kits.isEmpty()) {
            this.sendMessage(player, "&cNo kits have been created yet.");
            this.sendMessage(player, "&7Use &e/kit create <name> &7to create one.");
            return;
        }
        this.sendHeader((CommandSender)player, "Kits (" + kits.size() + ")");
        for (DuelKit kit : kits) {
            String status = "&a\u2713";
            int itemCount = this.countItems(kit);
            this.sendMessage(player, status + " &f" + kit.getDisplayName() + " &7(" + kit.getName() + ") &8- &f" + itemCount + " items");
        }
        this.sendFooter((CommandSender)player);
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/kit info <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelKit kit = this.plugin.getKitManager().getAdminKit(name);
        if (kit == null) {
            this.sendMessage(player, "&cKit not found: " + name);
            return;
        }
        this.sendHeader((CommandSender)player, "Kit: " + kit.getDisplayName());
        this.sendMessage(player, "&7Internal Name: &f" + kit.getName());
        this.sendMessage(player, "&7Display Name: &f" + kit.getDisplayName());
        this.sendMessage(player, "&7Status: &aEnabled");
        this.sendMessage(player, "&7Icon: &f" + kit.getIcon().name());
        this.sendMessage(player, "");
        this.sendMessage(player, "&7Contents:");
        this.sendMessage(player, "  &7Inventory Items: &f" + this.countInventoryItems(kit));
        this.sendMessage(player, "  &7Armor Pieces: &f" + this.countArmorPieces(kit));
        this.sendMessage(player, "  &7Offhand: &f" + (kit.getOffhand() != null && kit.getOffhand().getType() != Material.AIR ? kit.getOffhand().getType().name() : "Empty"));
        if (kit.getDescription() != null && !kit.getDescription().isEmpty()) {
            this.sendMessage(player, "");
            this.sendMessage(player, "&7Description: &f" + kit.getDescription());
        }
        this.sendFooter((CommandSender)player);
    }

    private void handleGive(Player player, String[] args) {
        if (!player.hasPermission("ultimateduels.admin.kit")) {
            this.sendNoPermission((CommandSender)player);
            return;
        }
        if (args.length < 3) {
            this.sendUsage((CommandSender)player, "/kit give <player> <kit>");
            return;
        }
        String targetName = args[1];
        String kitName = args[2].toLowerCase();
        Player target = this.plugin.getServer().getPlayer(targetName);
        if (target == null) {
            this.sendMessage(player, "&cPlayer not found: " + targetName);
            return;
        }
        DuelKit kit = this.plugin.getKitManager().getAdminKit(kitName);
        if (kit == null) {
            this.sendMessage(player, "&cKit not found: " + kitName);
            return;
        }
        this.plugin.getKitManager().applyKit(target, kit, true);
        this.sendMessage(player, "&aGave kit &e" + kit.getDisplayName() + " &ato &e" + target.getName() + "&a!");
        this.sendMessage(target, "&aYou received the &e" + kit.getDisplayName() + " &akit!");
    }

    private void handleLoad(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/kit load <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelKit kit = this.plugin.getKitManager().getAdminKit(name);
        if (kit == null) {
            this.sendMessage(player, "&cKit not found: " + name);
            return;
        }
        if (!player.hasPermission("ultimateduels.admin.kit") && !player.hasPermission("ultimateduels.kit.load." + name)) {
            this.sendNoPermission((CommandSender)player);
            return;
        }
        this.plugin.getKitManager().applyKit(player, kit, true);
        this.sendMessage(player, "&aLoaded kit: &e" + kit.getDisplayName());
    }

    private void handleSave(Player player, String[] args) {
        if (!player.hasPermission("ultimateduels.admin.kit")) {
            this.sendNoPermission((CommandSender)player);
            return;
        }
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/kit save <name>");
            this.sendMessage(player, "&7This will overwrite the kit with your current inventory.");
            return;
        }
        String name = args[1].toLowerCase();
        DuelKit kit = this.plugin.getKitManager().getAdminKit(name);
        if (kit == null) {
            this.sendMessage(player, "&cKit not found: " + name);
            this.sendMessage(player, "&7Use &e/kit create " + name + " &7to create a new kit.");
            return;
        }
        DuelKit updatedKit = this.plugin.getKitManager().capturePlayerInventory(player, name, kit.getKitType());
        kit.setInventoryContents(updatedKit.getInventoryContents());
        kit.setArmorContents(updatedKit.getArmorContents());
        kit.setOffhand(updatedKit.getOffhand());
        this.plugin.getKitManager().saveAdminKits();
        this.sendMessage(player, "&aKit &e" + name + " &ahas been updated with your current inventory!");
        this.sendMessage(player, "&7Including: Armor, Inventory, and &eOffhand&7 items.");
    }

    private void handleSetIcon(Player player, String[] args) {
        Material icon;
        if (!player.hasPermission("ultimateduels.admin.kit")) {
            this.sendNoPermission((CommandSender)player);
            return;
        }
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/kit seticon <name> [material]");
            this.sendMessage(player, "&7If no material is specified, uses held item.");
            return;
        }
        String name = args[1].toLowerCase();
        DuelKit kit = this.plugin.getKitManager().getAdminKit(name);
        if (kit == null) {
            this.sendMessage(player, "&cKit not found: " + name);
            return;
        }
        if (args.length >= 3) {
            try {
                icon = Material.valueOf((String)args[2].toUpperCase());
            }
            catch (IllegalArgumentException e) {
                this.sendMessage(player, "&cInvalid material: " + args[2]);
                return;
            }
        } else {
            ItemStack held = player.getInventory().getItemInMainHand();
            if (held.getType() == Material.AIR) {
                this.sendMessage(player, "&cYou must hold an item or specify a material!");
                return;
            }
            icon = held.getType();
        }
        kit.setIcon(icon);
        this.plugin.getKitManager().saveAdminKits();
        this.sendMessage(player, "&aKit icon set to: &e" + icon.name());
    }

    private void handleRename(Player player, String[] args) {
        if (!player.hasPermission("ultimateduels.admin.kit")) {
            this.sendNoPermission((CommandSender)player);
            return;
        }
        if (args.length < 3) {
            this.sendUsage((CommandSender)player, "/kit rename <kit> <new-display-name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelKit kit = this.plugin.getKitManager().getAdminKit(name);
        if (kit == null) {
            this.sendMessage(player, "&cKit not found: " + name);
            return;
        }
        String newDisplayName = this.joinArgs(args, 2);
        kit.setDisplayName(newDisplayName);
        this.plugin.getKitManager().saveAdminKits();
        this.sendMessage(player, "&aKit display name changed to: &e" + newDisplayName);
    }

    private void handleCopy(Player player, String[] args) {
        if (!player.hasPermission("ultimateduels.admin.kit")) {
            this.sendNoPermission((CommandSender)player);
            return;
        }
        if (args.length < 3) {
            this.sendUsage((CommandSender)player, "/kit copy <source> <new-name>");
            return;
        }
        String sourceName = args[1].toLowerCase();
        String newName = args[2].toLowerCase();
        DuelKit sourceKit = this.plugin.getKitManager().getAdminKit(sourceName);
        if (sourceKit == null) {
            this.sendMessage(player, "&cSource kit not found: " + sourceName);
            return;
        }
        if (this.plugin.getKitManager().adminKitExists(newName)) {
            this.sendMessage(player, "&cA kit with name '" + newName + "' already exists!");
            return;
        }
        this.plugin.getKitManager().applyKit(player, sourceKit, true);
        boolean created = this.plugin.getKitManager().createAdminKitFromPlayer(player, newName);
        if (!created) {
            this.sendMessage(player, "&cFailed to copy kit!");
            return;
        }
        DuelKit copiedKit = this.plugin.getKitManager().getAdminKit(newName);
        if (copiedKit != null) {
            copiedKit.setDisplayName(sourceKit.getDisplayName() + " (Copy)");
            copiedKit.setDescription(sourceKit.getDescription());
            copiedKit.setIcon(sourceKit.getIcon());
            this.plugin.getKitManager().saveAdminKits();
        }
        this.sendMessage(player, "&aKit &e" + sourceName + " &acopied to &e" + newName + "&a!");
    }

    private void sendHelp(Player player) {
        this.sendHeader((CommandSender)player, "Kit Commands");
        this.sendMessage(player, "&e/kit list &7- List all kits");
        this.sendMessage(player, "&e/kit info <name> &7- View kit info");
        this.sendMessage(player, "&e/kit load <name> &7- Load a kit");
        if (player.hasPermission("ultimateduels.admin.kit")) {
            this.sendMessage(player, "");
            this.sendMessage(player, "&c&lAdmin Commands:");
            this.sendMessage(player, "&e/kit create <name> &7- Create kit from inventory");
            this.sendMessage(player, "&e/kit delete <name> &7- Delete a kit");
            this.sendMessage(player, "&e/kit edit <name> &7- Edit kit in GUI");
            this.sendMessage(player, "&e/kit save <name> &7- Update kit from inventory");
            this.sendMessage(player, "&e/kit give <player> <kit> &7- Give kit to player");
            this.sendMessage(player, "&e/kit seticon <name> [material] &7- Set kit icon");
            this.sendMessage(player, "&e/kit rename <name> <display> &7- Rename kit");
            this.sendMessage(player, "&e/kit copy <source> <new> &7- Copy a kit");
        }
        this.sendFooter((CommandSender)player);
    }

    private int countItems(DuelKit kit) {
        int count = 0;
        if (kit.getInventoryContents() != null) {
            for (ItemStack item : kit.getInventoryContents()) {
                if (item == null || item.getType() == Material.AIR) continue;
                ++count;
            }
        }
        if (kit.getArmorContents() != null) {
            for (ItemStack item : kit.getArmorContents()) {
                if (item == null || item.getType() == Material.AIR) continue;
                ++count;
            }
        }
        if (kit.getOffhand() != null && kit.getOffhand().getType() != Material.AIR) {
            ++count;
        }
        return count;
    }

    private int countInventoryItems(DuelKit kit) {
        int count = 0;
        if (kit.getInventoryContents() != null) {
            for (ItemStack item : kit.getInventoryContents()) {
                if (item == null || item.getType() == Material.AIR) continue;
                ++count;
            }
        }
        return count;
    }

    private int countArmorPieces(DuelKit kit) {
        int count = 0;
        if (kit.getArmorContents() != null) {
            for (ItemStack item : kit.getArmorContents()) {
                if (item == null || item.getType() == Material.AIR) continue;
                ++count;
            }
        }
        return count;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        String sub;
        if (args.length == 1) {
            ArrayList<String> completions = new ArrayList<String>(Arrays.asList("list", "info", "load"));
            if (sender.hasPermission("ultimateduels.admin.kit")) {
                completions.addAll(Arrays.asList("create", "delete", "edit", "save", "give", "seticon", "rename", "copy"));
            }
            completions.addAll(this.getKitNames());
            return this.filterCompletions(completions, args[0]);
        }
        if (args.length == 2) {
            switch (sub = args[0].toLowerCase()) {
                case "delete": 
                case "edit": 
                case "info": 
                case "load": 
                case "save": 
                case "seticon": 
                case "rename": 
                case "copy": {
                    return this.filterCompletions(this.getKitNames(), args[1]);
                }
                case "give": {
                    return this.filterCompletions(this.getOnlinePlayerNames(), args[1]);
                }
                case "create": {
                    return List.of("<name>");
                }
            }
        }
        if (args.length == 3) {
            switch (sub = args[0].toLowerCase()) {
                case "give": {
                    return this.filterCompletions(this.getKitNames(), args[2]);
                }
                case "seticon": {
                    return this.filterCompletions(Arrays.stream(Material.values()).filter(Material::isItem).map(Enum::name).limit(50L).collect(Collectors.toList()), args[2]);
                }
                case "copy": {
                    return List.of("<new-name>");
                }
            }
        }
        return new ArrayList<String>();
    }
}

