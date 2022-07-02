package com.rogermiranda1000.versioncontroller.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BlockTypePost13 extends BlockType {
    private Material type;

    public BlockTypePost13(Material type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return this.type.name();
    }

    @Override
    public void setType(@NotNull Block block) {
        block.setType(this.type);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(this.type);
    }
}
