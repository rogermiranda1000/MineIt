package com.rogermiranda1000.versioncontroller.items;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public abstract class ItemManager {
    /**
     * Implement method to get the item(s) in hand
     * @param playerInventory Inventory
     * @return Item(s) holded
     */
    public abstract ItemStack[] getItemInHand(PlayerInventory playerInventory);

    /**
     * Get player's inventory and call getItemInHand(PlayerInventory)
     * @param player Player
     * @return Item(s) holded
     */
    public ItemStack[] getItemInHand(Player player) {
        return this.getItemInHand(player.getInventory());
    }

    public abstract void setItemInHand(PlayerInventory playerInventory, ItemStack item);

    /**
     * It checks the material, name and enchantments of an item
     * @param i First item
     * @param i2 Second item
     * @return If i == i2
     */
    public boolean sameItem(ItemStack i, ItemStack i2) {
        if (i == null && i2 == null) return true;
        if (i == null || i2 == null) return false;

        if (!i2.getType().equals(i.getType())) return false;
        if (i.getEnchantments().size() != i2.getEnchantments().size()) return false;

        boolean match = true;
        for (Map.Entry<Enchantment, Integer> enchantment : i2.getEnchantments().entrySet()) {
            Integer value = i.getEnchantments().get(enchantment.getKey());
            if (value == null || !value.equals(enchantment.getValue())) {
                match = false;
                break;
            }
        }
        if (!match) return false;

        ItemMeta m = i.getItemMeta(),
                m2 = i2.getItemMeta();
        if (m == null && m2 == null) return true;
        if (m == null || m2 == null) return false;
        return m.getDisplayName().equals(m2.getDisplayName());
    }

    /**
     * Check if player is holding an item
     * @param p Player
     * @param i Item
     * @return If the player is holding that item (true), or not (false)
     */
    public boolean hasItemInHand(Player p, ItemStack i) {
        for (ItemStack item : this.getItemInHand(p)) {
            if (this.sameItem(i, item)) return true;
        }

        return false;
    }
}
