package com.rogermiranda1000.versioncontroller.blocks;

import com.rogermiranda1000.versioncontroller.VersionController;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BlockTypePre13 extends BlockType {
    private final ItemStack type;

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

    public BlockTypePre13(@NotNull ItemStack type) throws IllegalArgumentException {
        List<String> lore;
        if (type.getItemMeta() == null || (lore = type.getItemMeta().getLore()) == null || lore.size() < 3 || !lore.get(0).equals("-- BlockData --")) this.type = type.clone();
        else {
            String []data = lore.get(1).split(":");
            this.type = new ItemStack(Material.valueOf(data[0]), 1, Short.parseShort(data[1]));
        }
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
        if (VersionController.get().isItem(this.type) || this.type.getType().equals(Material.AIR)) return this.type.clone();

        // not an item
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 2, true);
        meta.setDisplayName(this.type.getType().name());
        if (verbose) {
            ArrayList<String> lore = new ArrayList<>();
            lore.add("-- BlockData --");
            lore.add(this.type.getType().name() + ":" + this.type.getData().getData());
            lore.add("-------------");
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }
}
