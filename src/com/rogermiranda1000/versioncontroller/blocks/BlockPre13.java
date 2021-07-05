package com.rogermiranda1000.versioncontroller.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * BlockManager for version < 1.13
 */
public class BlockPre13 implements BlockManager {
    @Nullable private static final Method getTypeIdMethod = BlockPre13.getGetTypeIdMethod();
    @Nullable private static final Method setTypeMethod = BlockPre13.getSetTypeMethod();

    @Nullable
    private static Method getGetTypeIdMethod() {
        try {
            return ItemStack.class.getMethod("getTypeId");
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return null;
        }
    }

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
        return new ItemStack(block.getType(), (short)1, block.getData());
    }

    @Override
    public boolean isPassable(@NotNull Block block) {
        return !block.getType().isSolid();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @Nullable
    public String getName(@NotNull Object block) {
        try {
            return String.valueOf((int)BlockPre13.getTypeIdMethod.invoke((ItemStack)block)) + ":" + "0"; // TODO sub-id
        } catch (InvocationTargetException | IllegalAccessException | NullPointerException ignored) { }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setType(@NotNull Block block, Object type) {
        try {
            String[] typeInfo = this.getName(type).split(":");
            BlockPre13.setTypeMethod.invoke(block, Integer.parseInt(typeInfo[0]), Byte.parseByte(typeInfo[1]), true);
        } catch (InvocationTargetException | IllegalAccessException | NullPointerException ignored) { }
    }

    @Override
    public ItemStack getItemStack(Object type) {
        return new ItemStack((ItemStack) type);
    }
}
