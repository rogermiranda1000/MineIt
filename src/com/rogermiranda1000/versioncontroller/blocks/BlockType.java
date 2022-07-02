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
     * Change the block's type
     * @param block Block to change
     */
    public abstract void setType(@NotNull Block block);

    /**
     * Given an object created by this class, it returns the ItemStack
     * @return Object's ItemStack
     */
    public abstract ItemStack getItemStack();
}
