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
    @Nullable private static final Method setTypeMethod = BlockPre13.getSetTypeMethod();

    @Nullable
    private static Method getSetTypeMethod() {
        try {
            return Block.class.getMethod("setTypeIdAndData", int.class, byte.class, boolean.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public @Nullable Object getMaterial(String type) {
        ItemStack r = null;
        String []s = type.split(":");

        try {
            r = new ItemStack(Material.valueOf(s[0]), (short)1, s.length == 2 ? Short.valueOf(s[1]) : 0);
        }
        catch (IllegalArgumentException ignored) { }

        return r;
    }

    @Override
    public Object getObject(@NotNull Block block) {
        return new ItemStack(block.getType(), 1, block.getData());
    }

    @Override
    public Object getObject(@NotNull ItemStack item) {
        return new ItemStack(item.getType(), 1, item.getData().getData());
    }

    @Override
    public boolean isPassable(@NotNull Block block) {
        return !block.getType().isSolid();
    }

    @Override
    public String getName(@NotNull Object block) {
        String material = ((ItemStack)block).getType().name();
        byte subId = ((ItemStack)block).getData().getData();

        if (subId > 0) return material + ":" + String.valueOf(subId);
        else return material;
    }

    @Override
    public void setType(@NotNull Block block, Object type) {
        try {
            BlockPre13.setTypeMethod.invoke(block, ((ItemStack)type).getType().getId(), ((ItemStack)type).getData().getData(), true); // TODO gravity
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException ignored) {}
    }

    @Override
    public ItemStack getItemStack(Object type) {
        return (type == null ? null : new ItemStack((ItemStack) type));
    }
}
