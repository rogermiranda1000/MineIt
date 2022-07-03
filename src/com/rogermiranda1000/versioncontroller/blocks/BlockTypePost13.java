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
    private static final Pattern blockDataData = Pattern.compile("minecraft:[^\\[]+(\\[(.+)\\])?");
    private final BlockData data;

    public BlockTypePost13(Material type) {
        this.data = type.createBlockData();
    }

    public BlockTypePost13(Block block) {
        this.data = block.getBlockData().clone();
    }

    public BlockTypePost13(String str) throws IllegalArgumentException {
        BlockData data;
        try {
            data = Bukkit.createBlockData(str);
        } catch (IllegalArgumentException ex) {
            Material mat = Material.getMaterial(str);
            if (mat == null) throw new IllegalArgumentException(str + " is not a Material, nor BlockData");
            data = mat.createBlockData();
        }
        this.data = data;
    }

    @Override
    public String getName() {
        return this.data.getAsString();
    }

    @Override
    public String getFriendlyName() {
        return this.data.getMaterial().name().toLowerCase();
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
            data.add(0, "BlockData:");
            for (int i = 1; i < data.size(); i++) data.set(i, "- " + data.get(i).replaceAll("=", ": "));

            ItemMeta meta = item.getItemMeta();
            meta.setLore(data);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String []getDataList(BlockData data) {
        Matcher m = BlockTypePost13.blockDataData.matcher(data.getAsString());
        if (!m.find()) throw new IllegalArgumentException("Expecting block data to be 'minecraft:...[...]', found '" + data.getAsString() + "' instead.");
        String match = m.group(2);
        if (match == null) return new String[]{};
        return match.split(",");
    }

    private static List<String> getNonStandardDataList(BlockData data) {
        String []current = BlockTypePost13.getDataList(data),
                original = BlockTypePost13.getDataList(data.getMaterial().createBlockData());
        ArrayList<String> r = new ArrayList<>();
        for (String e : current) {
            if (Arrays.stream(original).noneMatch(e::equals)) r.add(e);
        }
        return r;
    }

    public boolean defaultMaterial() {
        return this.data.matches(this.data.getMaterial().createBlockData());
    }
}
