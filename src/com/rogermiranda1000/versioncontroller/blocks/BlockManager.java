package com.rogermiranda1000.versioncontroller.blocks;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * User blocks
 */
public interface BlockManager {
    /**
     * String to material
     * @param type Material's name (e.g. WOOL:2 or WHITE_WOOL)
     * @return Material (null if IllegalArgumentException)
     */
    @Nullable BlockType getMaterial(String type);

    /**
     * Block to material
     * @param block Block
     * @return Material (null if IllegalArgumentException)
     */
    BlockType getObject(@NotNull Block block);

    /**
     * Item to material
     * @param item Item
     * @return Material
     */
    BlockType getObject(@NotNull ItemStack item);

    /**
     * Is the block passable?
     * @param block Block to get the information
     * @return If it's passable (true), or not (false)
     */
    boolean isPassable(@NotNull Block block);
}
