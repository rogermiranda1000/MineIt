package com.rogermiranda1000.mineit;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;

public class Mines {
    private final ArrayList<BasicLocation> bloques = new ArrayList<>();
    public int currentTime = 0;

    public String[] stages = {"BEDROCK", "STONE", "OBSIDIAN", "DIAMOND_ORE"};
    public int[] stageBlocks = new int[stages.length];
    public int[] stageGo = {1, 2};
    public int[] stageLimit = {9999, 9999, 9999, 9999};
    public String name = "";
    public boolean start = true;

    Mines() { }

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
}
