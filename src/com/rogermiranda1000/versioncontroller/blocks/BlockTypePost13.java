package com.rogermiranda1000.versioncontroller.blocks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockTypePost13 extends BlockType {
    private static final Pattern blockDataData = Pattern.compile("minecraft:[^\\[]+\\[(.+)\\]");
    private BlockData data;

    public BlockTypePost13(Material type) {
        this.data = type.createBlockData();
    }

    public BlockTypePost13(Block block) {
        this.data = block.getBlockData().clone();
    }

    public BlockTypePost13(String str) throws IllegalArgumentException {
        this.data = Bukkit.createBlockData(str);
    }

    @Override
    public String getName() {
        return this.data.getAsString();
    }

    @Override
    public void setType(@NotNull Block block) {
        block.setBlockData(this.data);
    }

    @Override
    public ItemStack getItemStack(boolean verbose) {
        ItemStack item = new ItemStack(this.data.getMaterial());
        List<String> data;
        if (verbose && (data = BlockTypePost13.getNonStandardDataList(this.data)).size() > 0) {
            ItemMeta meta = item.getItemMeta();
            meta.setLore(data);
        }
        return item;
    }

    private static String []getDataList(BlockData data) {
        Matcher m = BlockTypePost13.blockDataData.matcher(data.getAsString());
        if (!m.find()) throw new IllegalArgumentException("Expecting block data to be 'minecraft:...[...]'");
        return m.group(1).split(",");
    }

    private static List<String> getNonStandardDataList(BlockData data) {
        String []current = BlockTypePost13.getDataList(data);
        List<String> original = Arrays.asList(BlockTypePost13.getDataList(data.getMaterial().createBlockData()));
        ArrayList<String> r = new ArrayList<>();
        for (String e : current) {
            if (!original.contains(e)) r.add(e);
        }
        return r;
    }

    public boolean defaultMaterial() {
        return this.data.matches(this.data.getMaterial().createBlockData());
    }
}
