package com.rogermiranda1000.mineit.inventories;

import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class BasicInventory implements Listener {
    protected Inventory inv;

    public Inventory getInventory() {
        return this.inv;
    }

    abstract public void onClick(InventoryClickEvent e);
}
