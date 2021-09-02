package com.rogermiranda1000.mineit.file;

import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.Stage;
import org.bukkit.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BasicMine {
    /**
     * World1 -> [(x,y,z), (x,y,z), ...]
     * World2 -> [(x,y,z), (x,y,z), ...]
     * ...
     */
    private final HashMap<String, ArrayList<BasicLocation>> blocks;

    private final ArrayList<BasicStage> stages;
    private final String mineName;
    private final boolean started;
    private final int delay;

    public BasicMine(Mine mine) {
        this.mineName = mine.getName();
        this.started = mine.isStarted();
        this.delay = mine.getDelay();

        this.stages = new ArrayList<>();
        for (Stage s : mine.getStages()) this.stages.add(new BasicStage(s));

        this.blocks = new HashMap<>();
        for (Location block : mine.getMineBlocks()) {
            String world = (block.getWorld() == null) ? null : block.getWorld().getName();
            ArrayList<BasicLocation> worldBlocks = this.blocks.get(world);
            if (worldBlocks == null) {
                worldBlocks = new ArrayList<>();
                this.blocks.put(world, worldBlocks);
            }

            worldBlocks.add(new BasicLocation(block));
        }
    }

    public Mine getMine() throws IOException, InvalidLocationException {
        ArrayList<Location> blocks = new ArrayList<>();
        for (Map.Entry<String, ArrayList<BasicLocation>> basicLocationList : this.blocks.entrySet()) {
            blocks.addAll(BasicLocation.getLocations(basicLocationList.getKey(), basicLocationList.getValue()));
        }
        return new Mine(this.mineName, this.started, blocks, BasicStage.getStages(this.stages), this.delay);
    }
}
