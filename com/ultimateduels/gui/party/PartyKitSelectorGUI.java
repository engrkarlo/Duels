/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.inventory.ItemStack
 */
package com.ultimateduels.gui.party;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.party.model.Party;
import com.ultimateduels.party.model.PartyResult;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PartyKitSelectorGUI
extends AbstractGUI {
    private final PartyMode partyMode;
    private int page = 1;
    private static final int KITS_PER_PAGE = 28;

    public PartyKitSelectorGUI(UltimateDuels plugin, PartyMode partyMode) {
        super(plugin, partyMode == PartyMode.PARTY_VS_PARTY ? "&5&lParty vs Party &8- &eSelect Kit" : "&6&lParty Split &8- &eSelect Kit", 6);
        this.partyMode = partyMode;
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.PURPLE_STAINED_GLASS_PANE);
        ArrayList<DuelKit> kits = new ArrayList<DuelKit>(this.plugin.getKitManager().getAllAdminKits());
        int totalPages = Math.max(1, (int)Math.ceil((double)kits.size() / 28.0));
        int startIndex = (this.page - 1) * 28;
        int endIndex = Math.min(startIndex + 28, kits.size());
        int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        int slotIndex = 0;
        for (int i = startIndex; i < endIndex && slotIndex < slots.length; ++i) {
            DuelKit kit = (DuelKit)kits.get(i);
            int slot = slots[slotIndex++];
            this.setItem(slot, this.createKitItem(kit), event -> this.handleKitClick(event.getPlayer(), kit));
        }
        this.setItem(4, this.createInfoItem());
        if (this.page > 1) {
            this.setItem(45, GUIItem.previousPage(this.page, event -> {
                --this.page;
                this.refresh(event.getPlayer());
            }));
        }
        if (this.page < totalPages) {
            this.setItem(53, GUIItem.nextPage(this.page, totalPages, event -> {
                ++this.page;
                this.refresh(event.getPlayer());
            }));
        }
        this.setItem(49, GUIItem.closeButton());
    }

    private ItemStack createKitItem(DuelKit kit) {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        if (kit.getDescription() != null && !kit.getDescription().isEmpty()) {
            lore.add("&7" + kit.getDescription());
            lore.add("");
        }
        lore.add(this.partyMode == PartyMode.PARTY_VS_PARTY ? "&eClick to queue Party vs Party" : "&eClick to start Party Split");
        Material icon = kit.getIcon() != null ? kit.getIcon() : Material.DIAMOND_SWORD;
        return new GUIItem.Builder(icon).name("&e&l" + kit.getDisplayName()).lore(lore).glowing(true).buildItem();
    }

    private ItemStack createInfoItem() {
        String title = this.partyMode == PartyMode.PARTY_VS_PARTY ? "&5&lParty vs Party" : "&6&lParty Split";
        String desc = this.partyMode == PartyMode.PARTY_VS_PARTY ? "&7Queue against another party" : "&7Split your party into two teams";
        return this.createItem(Material.BOOK, title, "", desc, "", "&7Select a kit to continue");
    }

    private void handleKitClick(Player player, DuelKit kit) {
        Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) {
            MessageUtils.sendMessage(player, "&cYou are not in a party!");
            player.closeInventory();
            return;
        }
        if (!party.isLeader(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "&cOnly the party leader can do this!");
            return;
        }
        player.closeInventory();
        if (this.partyMode == PartyMode.PARTY_VS_PARTY) {
            PartyResult result = this.plugin.getPartyManager().queuePartyVsParty(player, kit.getName());
            MessageUtils.sendMessage(player, result.message());
        } else {
            int teamSize = party.getSize() / 2;
            PartyResult result = this.plugin.getPartyManager().queuePartySplit(player, kit.getName(), teamSize);
            MessageUtils.sendMessage(player, result.message());
        }
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }

    public static enum PartyMode {
        PARTY_VS_PARTY,
        PARTY_SPLIT;

    }
}

