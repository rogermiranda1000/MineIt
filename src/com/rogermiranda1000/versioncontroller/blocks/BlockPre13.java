package com.rogermiranda1000.versioncontroller.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * BlockManager for version < 1.13
 */
public class BlockPre13 implements BlockManager {
    @Override
    public @Nullable BlockType getMaterial(String type) {
        BlockType r = null;
        String []s = type.split(":");

        try {
            r = new BlockTypePre13(new ItemStack(Material.valueOf(s[0]), (short)1, s.length == 2 ? Short.valueOf(s[1]) : 0));
        }
        catch (IllegalArgumentException ignored) { }

        return r;
    }

    @Override
    public BlockType getObject(@NotNull Block block) {
        return new BlockTypePre13(new ItemStack(block.getType(), 1, block.getData()));
    }

    @Override
    public BlockType getObject(@NotNull ItemStack item) {
        return new BlockTypePre13(item);
    }

    @Override
    public boolean isPassable(@NotNull Block block) {
        return !block.getType().isSolid();
    }
}
