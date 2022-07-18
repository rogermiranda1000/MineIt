package com.rogermiranda1000.mineit.inventories;

import com.rogermiranda1000.helper.BasicInventory;
import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.versioncontroller.VersionController;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SelectMineInventory extends MinesInventory {
    private static final String INVENTORY_NAME = "§cEdit mine";
    private final HashMap<Mine,BasicInventory> editMineInventory;

    public SelectMineInventory() {
        super(SelectMineInventory.INVENTORY_NAME);
        this.editMineInventory = new HashMap<>();
    }

    public SelectMineInventory(HashMap<Mine,BasicInventory> minesInventories, int offset, MinesInventory pre) {
        super(SelectMineInventory.INVENTORY_NAME, offset, pre);
        this.editMineInventory = minesInventories;
    }

    @Override
    void mineClicked(InventoryClickEvent e, Mine m) {
        Player player = (Player) e.getWhoClicked();
        if (!player.getItemOnCursor().getType().equals(Material.AIR)) {
            // change mine type
            m.setMineBlockIdentifier(VersionController.get().getObject(player.getItemOnCursor()));
        }
        else {
            // edit mine
            BasicInventory inv = this.editMineInventory.get(m);
            if (inv != null) inv.openInventory(player);
        }
    }

    public Collection<BasicInventory> getMinesInventories() {
        return this.editMineInventory.values();
    }

    @Nullable
    public BasicInventory searchMine(String title) {
        for (Map.Entry<Mine,BasicInventory> m : this.editMineInventory.entrySet()) {
            if (m.getKey().getName().equalsIgnoreCase(title)) return m.getValue();
        }
        return null; // not found
    }

    @Override
    public void onMineRemoved(Mine m) {
        this.editMineInventory.remove(m);
        super.onMineRemoved(m);
    }

    @Override
    public void onMineAdded(Mine m) {
        this.editMineInventory.put(m, new EditMineInventory(m));
        super.onMineAdded(m);
    }

    @Override
    public MinesInventory create(int offset, MinesInventory pre) {
        return new SelectMineInventory(this.editMineInventory, offset, pre);
    }
}
