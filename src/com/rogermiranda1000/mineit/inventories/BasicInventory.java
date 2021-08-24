package com.rogermiranda1000.mineit.inventories;

import com.rogermiranda1000.mineit.MineIt;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public abstract class BasicInventory implements Listener {
    protected Inventory inv;
    private final ArrayList<HumanEntity> playersWithOpenInventory;
    private boolean swappingInventories;

    /**
     * It initializes the players with the current inventory opened list
     */
    public BasicInventory() {
        this.playersWithOpenInventory = new ArrayList<>();
        this.swappingInventories = false;
    }

    /**
     * It registers the inventories event (click & close)
     * @param p The instanciated plugin
     */
    public void registerEvent(Plugin p) {
        p.getServer().getPluginManager().registerEvents(this, p);
    }

    public void openInventory(HumanEntity p) {
        p.closeInventory();

        // run on next tick
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MineIt.instance, ()-> {
            p.openInventory(this.inv);
            synchronized (this.playersWithOpenInventory) {
                this.playersWithOpenInventory.add(p);
            }
        }, 1L);
    }

    abstract public void onClick(InventoryClickEvent e);

    /**
     * Changes all the opened inventories to the new one
     * @param other New inventory
     */
    protected void newInventory(@NotNull Inventory other) {
        synchronized (this) {
            this.swappingInventories = true;

            for (HumanEntity player : this.playersWithOpenInventory) {
                player.closeInventory();

                // run on next tick
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MineIt.instance, ()->player.openInventory(other), 1L);
            }
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MineIt.instance, ()->{
            synchronized (this) {
                this.swappingInventories = false;
            }
        }, 2L);
    }

    public synchronized void closeInventories() {
        for (HumanEntity player : this.playersWithOpenInventory) player.closeInventory();
    }

    @EventHandler
    public synchronized void onInventoryClose(InventoryCloseEvent e) {
        if (this.swappingInventories) return; // ignore (it will close and open immediately)

        if (e.getInventory().equals(this.inv)) this.playersWithOpenInventory.remove(e.getPlayer());
    }
}
