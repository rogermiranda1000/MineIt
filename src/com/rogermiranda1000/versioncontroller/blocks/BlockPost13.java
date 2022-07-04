package com.rogermiranda1000.versioncontroller.blocks;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BlockManager for version >= 1.13
 */
public class BlockPost13 implements BlockManager {
    @Override
    public @Nullable BlockType getMaterial(String type) {
        try {
            return new BlockTypePost13(type);
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public BlockType getObject(@NotNull Block block) {
        return new BlockTypePost13(block);
    }

    @Override
    public BlockType getObject(@NotNull ItemStack item) {
        return new BlockTypePost13(item);
    }

    @Override
    public boolean isPassable(@NotNull Block block) {
        return block.isPassable();
    }
}
