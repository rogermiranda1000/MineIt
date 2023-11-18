package com.rogermiranda1000.mineit.mine.stage;

import com.rogermiranda1000.mineit.mine.Mine;
import com.rogermiranda1000.versioncontroller.VersionController;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class UncachedStage implements StageProvider {
    private final Location stageBlockLoc;
    private final Mine mine;

    public UncachedStage(Location stageBlockLoc, Mine m) {
        this.stageBlockLoc = stageBlockLoc;
        this.mine = m;
    }

    public Location getStageBlockLoc() {
        return stageBlockLoc;
    }

    @Override
    public Stage getStage() {
        Block b = this.stageBlockLoc.getBlock();
        BlockType myBlock = VersionController.get().getObject(b);
        return this.mine.getStage(myBlock);
    }
}
