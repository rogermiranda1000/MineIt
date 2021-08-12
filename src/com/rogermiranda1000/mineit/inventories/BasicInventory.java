package com.rogermiranda1000.mineit.inventories;

import com.rogermiranda1000.mineit.MineIt;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public abstract class BasicInventory implements Listener {
    protected Inventory inv;
    private final ArrayList<HumanEntity> playersWithOpenInventory;

    public BasicInventory() {
        this.playersWithOpenInventory = new ArrayList<>();
    }

    public void openInventory(HumanEntity p) {
        p.closeInventory();

        // run on next tick
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MineIt.instance, ()-> {
            p.openInventory(this.inv);
            this.playersWithOpenInventory.add(p);
        }, 1L);
    }

    abstract public void onClick(InventoryClickEvent e);

    /**
     * Changes all the opened inventories to the new one
     * @param other New inventory
     */
    protected void newInventory(Inventory other) {
        for (HumanEntity player : this.playersWithOpenInventory) {
            player.closeInventory();

            // run on next tick
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MineIt.instance, ()->player.openInventory(other), 1L);
        }
    }

    public void closeInventories() {
        for (HumanEntity player : this.playersWithOpenInventory) player.closeInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(this.inv)) this.playersWithOpenInventory.remove(e.getPlayer());
    }
}
