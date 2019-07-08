package com.rogermiranda1000.events;

import com.rogermiranda1000.mineit.MineIt;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class onClick implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        Inventory inventory = e.getInventory();

        if(!inventory.getName().equals(MineIt.inv.getName())) return;
        e.setCancelled(true);
        if(!player.hasPermission("mineit.open")) {
            player.closeInventory();
            player.sendMessage(MineIt.prefix+"You can't use this menu.");
            return;
        }

        if(clicked.equals(MineIt.item2)) {
            if(!player.hasPermission("mineit.create")) {
                player.sendMessage(MineIt.prefix+"You can't get the Mine creator.");
                return;
            }

            player.closeInventory();
            MineIt.instance.getLogger().info("Giving Mine creator to "+player.getName()+"...");
            player.getInventory().addItem(MineIt.item);
        }
    }
}
