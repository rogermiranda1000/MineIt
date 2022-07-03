package com.rogermiranda1000.versioncontroller.blocks;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class BlockType {
    /**
     * Get the block's name (material/id)
     * @return Material/ID identifying the block
     */
    public abstract String getName();

    /**
     * Get the block's name (material/id)
     * @return Material/ID partially identifying the block
     */
    public String getFriendlyName() {
        return this.getName();
    }

    /**
     * Change the block's type
     * @param block Block to change
     */
    public abstract void setType(@NotNull Block block);

    /**
     * Given an object created by this class, it returns the ItemStack
     * @param verbose If the material is non-standard, use the lore to be more precise
     * @return Object's ItemStack
     */
    public abstract ItemStack getItemStack(boolean verbose);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BlockType)) return false;

        if (this == o) return true;
        return this.getName().equals(((BlockType)o).getName());
    }
}
