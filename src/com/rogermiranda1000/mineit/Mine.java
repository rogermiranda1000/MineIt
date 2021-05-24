package com.rogermiranda1000.mineit;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;

public class Mine implements Runnable {
    private static int MINE_DELAY;
    private final ArrayList<BasicLocation> bloques = new ArrayList<>();
    public int currentTime = 0;

    private final ArrayList<Stage> stages = Mine.getDefaultStages();
    public String name = "";
    public boolean start = true;

    public Mine() {

    }

    public void add(Location loc) {
        this.bloques.add(new BasicLocation(loc));
    }

    public Location[] getMineBlocks() {
        return BasicLocation.getLocations(this.bloques);
    }

    public int getTotalBlocks() {
        return this.bloques.size();
    }

    public Location getRandomBlockInMine() {
        return this.bloques.get(new Random().nextInt(this.bloques.size())).getLocation();
    }

    public ArrayList<Stage> getStages() {
        return this.stages;
    }

    public void resetStagesCount() {
        for (Stage s : this.stages) s.resetStageCount();
    }

    private static ArrayList<Stage> getDefaultStages() {
        ArrayList<Stage> r = new ArrayList<>(4);
        Stage bedrock = new Stage("BEDROCK", Integer.MAX_VALUE);
        r.add(bedrock);
        Stage stone = new Stage("STONE", Integer.MAX_VALUE, bedrock);
        bedrock.setNextStage(stone);
        r.add(stone);
        Stage obsidian = new Stage("OBSIDIAN", Integer.MAX_VALUE, stone);
        stone.setNextStage(obsidian);
        r.add(obsidian);
        Stage diamond = new Stage("DIAMOND_ORE", Integer.MAX_VALUE, obsidian);
        obsidian.setNextStage(diamond);
        r.add(diamond);
        return r;
    }

    public static void setMineDelay(int delay) {
        Mine.MINE_DELAY = delay;
    }

    @Override
    public void run() {
        if(!this.start) return; // TODO notifies
        this.currentTime++;
        if(this.currentTime < (Mine.MINE_DELAY*20D)/this.getTotalBlocks()) return;

        this.currentTime=0;
        Location loc = this.getRandomBlockInMine();
        Stage current = Stage.getMatch(this.stages, loc.getBlock().getType().toString());
        if (current == null) return; // wtf
        Stage next = current.getNextStage();
        if (next != null && next.fitsOneBlock()) {
            current.decrementStageBlocks();
            next.incrementStageBlocks();
            loc.getBlock().setType(next.getStageMaterial());
        }
    }
}
