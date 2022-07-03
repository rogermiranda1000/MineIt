package com.rogermiranda1000.versioncontroller.blocks;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BlockTypePre13 extends BlockType {
    private ItemStack type;

    @Nullable
    private static final Method setTypeMethod = BlockTypePre13.getSetTypeMethod();

    @Nullable
    private static Method getSetTypeMethod() {
        try {
            return Block.class.getMethod("setTypeIdAndData", int.class, byte.class, boolean.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public BlockTypePre13(ItemStack type) {
        this.type = type;
    }

    @Override
    public String getName() {
        String material = this.type.getType().name();
        byte subId = this.type.getData().getData();

        if (subId > 0) return material + ":" + String.valueOf(subId);
        else return material;
    }

    @Override
    public void setType(@NotNull Block block) {
        try {
            BlockTypePre13.setTypeMethod.invoke(block, this.type.getType().getId(), this.type.getData().getData(), true); // TODO gravity
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException ignored) {}
    }

    @Override
    public ItemStack getItemStack(boolean verbose) {
        return (type == null) ? null : new ItemStack(this.type);
    }
}
