package com.rogermiranda1000.mineit;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;

public class Mines {
    public static final Random random = new Random();

    private ArrayList<Location> bloques = new ArrayList<>();
    public String[] stages = {"BEDROCK", "STONE", "OBSIDIAN", "DIAMOND_ORE"};
    public int[] stageBlocks = new int[stages.length];
    public int[] stageGo = {1, 2};
    public int[] stageLimit = {9999, 9999, 9999, 9999};
    public String name = "";
    public int currentTime = 0;
    public boolean start = true;

    Mines() { }

    public void add(Location loc) {
        bloques.add(loc);
    }

    public ArrayList<Location> getMineBlocks() {
        return this.bloques;
    }

    public int getTotalBlocks() {
        return this.bloques.size();
    }

    public Location getRandomBlockInMine() {
        return this.bloques.get(Mines.random.nextInt(this.bloques.size()));
    }
}
