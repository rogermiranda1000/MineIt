package com.rogermiranda1000.mineit.mine;

import com.rogermiranda1000.mineit.mine.stage.Stage;
import com.rogermiranda1000.mineit.mine.stage.StageProvider;
import com.rogermiranda1000.mineit.mine.stage.StageProxy;
import org.bukkit.Location;

public class MineBlock implements StageProvider {
    private final StageProxy stage;
    private final Mine mine;
    private final Location blockLocation;

    public MineBlock(Location stageBlockLoc, Mine m) {
        this.blockLocation = stageBlockLoc;
        this.mine = m;

        this.stage = new StageProxy(stageBlockLoc, m);
    }

    @Override
    public Stage getStage() {
        return this.stage.getStage();
    }

    public void setStage(Stage s) {
        this.stage.setStage(s);
    }

    public Mine getMine() {
        return mine;
    }

    public Location getBlockLocation() {
        return blockLocation;
    }
}
