package com.rogermiranda1000.versioncontroller.blocks;

import org.bukkit.Material;
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
        BlockType r = null;

        try {
            r = new BlockTypePost13(Material.valueOf(type));
        }
        catch (IllegalArgumentException ignored) { }

        return r;
    }

    @Override
    public BlockType getObject(@NotNull Block block) {
        return new BlockTypePost13(block.getType());
    }

    @Override
    public BlockType getObject(@NotNull ItemStack item) {
        return new BlockTypePost13(item.getType());
    }

    @Override
    public boolean isPassable(@NotNull Block block) {
        return block.isPassable();
    }
}
