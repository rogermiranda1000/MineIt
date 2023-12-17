package com.rogermiranda1000.mineit.mine.placer;

import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.Location;

public interface BlockPlacer {
    void placeBlock(Location place, BlockType b);
    boolean isPending(Location loc);
}
