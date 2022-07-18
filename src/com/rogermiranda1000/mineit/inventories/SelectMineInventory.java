package com.rogermiranda1000.mineit.inventories;

import com.rogermiranda1000.helper.BasicInventory;
import com.rogermiranda1000.mineit.Mine;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;

public class SelectMineInventory extends MinesInventory implements CreateMinesInventory {
    public SelectMineInventory() {
        super();
    }

    @Override
    void mineClicked(InventoryClickEvent e, Mine m) {
        BasicInventory inv = this.editMineInventory.get(m);
        if (inv != null) inv.openInventory(e.getWhoClicked());
    }


    public SelectMineInventory(HashMap<Mine,BasicInventory> minesInventories, int offset, MinesInventory pre) {
        super(minesInventories, offset, pre);
    }

    @Override
    public MinesInventory create(HashMap<Mine, BasicInventory> minesInventories, int offset, MinesInventory pre) {
        return new SelectMineInventory(minesInventories, offset, pre);
    }
}
