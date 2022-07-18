package com.rogermiranda1000.mineit.inventories;

import com.rogermiranda1000.helper.BasicInventory;
import com.rogermiranda1000.mineit.Mine;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SelectMineInventory extends MinesInventory {
    private final HashMap<Mine,BasicInventory> editMineInventory;

    public SelectMineInventory() {
        super();
        this.editMineInventory = new HashMap<>();
    }

    public SelectMineInventory(HashMap<Mine,BasicInventory> minesInventories, int offset, MinesInventory pre) {
        super(offset, pre);
        this.editMineInventory = minesInventories;
    }

    @Override
    void mineClicked(InventoryClickEvent e, Mine m) {
        // TODO change mine type
        BasicInventory inv = this.editMineInventory.get(m);
        if (inv != null) inv.openInventory(e.getWhoClicked());
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
