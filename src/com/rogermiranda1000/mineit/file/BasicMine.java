package com.rogermiranda1000.mineit.file;

import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.Stage;
import org.bukkit.Location;

import java.io.IOException;
import java.util.ArrayList;

public class BasicMine {
    private final ArrayList<BasicLocation> blocks;

    private final ArrayList<BasicStage> stages;
    private final String mineName;
    private final boolean started;

    public BasicMine(Mine mine) {
        this.mineName = mine.getName();
        this.started = mine.isStarted();

        this.stages = new ArrayList<>();
        for (Stage s : mine.getStages()) this.stages.add(new BasicStage(s));

        this.blocks = new ArrayList<>();
        for (Location block : mine.getMineBlocks()) this.blocks.add(new BasicLocation(block));
    }

    public Mine getMine() throws IOException {
        return new Mine(this.mineName, this.started, BasicLocation.getLocations(this.blocks), BasicStage.getStages(this.stages));
    }
}
