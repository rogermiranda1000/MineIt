package com.rogermiranda1000.mineit.inventories;

import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.MineIt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TpMineInventory extends MinesInventory {
    private static final String INVENTORY_NAME = "§bTp mine";
    public TpMineInventory() {
        super(TpMineInventory.INVENTORY_NAME);
    }

    @Override
    void mineClicked(InventoryClickEvent e, Mine m) {
        Player p = (Player)e.getWhoClicked();
        if (m.getTp() == null) {
            p.sendMessage(MineIt.instance.errorPrefix + "The mine " + m.getName() + " doesn't have a tp established yet.");
            return;
        }

        p.teleport(m.getTp());
        p.sendMessage(MineIt.instance.clearPrefix + "Teleporting to mine " + m.getName() + "...");
    }



    @Override
    public MinesInventory create(int offset, MinesInventory pre) {
        return new TpMineInventory(offset, pre);
    }

    public TpMineInventory(int offset, MinesInventory pre) {
        super(TpMineInventory.INVENTORY_NAME, offset, pre);
    }
}
