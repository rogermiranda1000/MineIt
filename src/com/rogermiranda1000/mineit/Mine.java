package com.rogermiranda1000.mineit;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;

public class Mine /*implements Runnable*/ {
    public static final Material STATE_ZERO = Material.BEDROCK;
    private static boolean DEFAULT_START;

    //private static int MINE_DELAY;
    private final ArrayList<Location> blocks;
    public int currentTime;
    private final ArrayList<Stage> stages;
    public final String mineName;
    public boolean started;

    public Mine(String name, boolean started, ArrayList<Location> blocks, ArrayList<Stage> stages) {
        this.currentTime = 0;

        this.mineName = name;
        this.started = started;
        this.blocks = blocks;
        this.stages = stages;
    }

    public Mine(String name, ArrayList<Location> blocks) {
        this(name, Mine.DEFAULT_START, blocks, Mine.getDefaultStages());
    }

    public String getName() {
        return this.mineName;
    }

    public boolean isStarted() {
        return this.started;
    }

    public void add(Location loc) {
        this.blocks.add(loc);
    }

    public ArrayList<Location> getMineBlocks() {
        return this.blocks;
    }

    public int getTotalBlocks() {
        return this.blocks.size();
    }

    public Location getRandomBlockInMine() {
        return this.blocks.get(new Random().nextInt(this.blocks.size()));
    }

    public ArrayList<Stage> getStages() {
        return this.stages;
    }

    public Stage getStage(int i) {
        return this.stages.get(i);
    }

    public int getStageCount() {
        return this.stages.size();
    }

    public void removeStage(int index) {
        Stage remove = this.getStage(index);
        Stage prev = remove.getPreviousStage(), next = remove.getNextStage();

        // remove all stages that points to the one that will be removed
        for (Stage s : this.stages) {
            if (s.equals(remove)) continue; // ignore the stage that will be removed

            if (remove.equals(s.getPreviousStage())) s.setPreviousStage(prev);
            if (remove.equals(s.getNextStage())) s.setNextStage(next);
        }

        this.stages.remove(index);
        this.updateStages();
        // TODO quitar bloques del estado eliminado?
    }

    public void addStage(Stage stage) {
        Stage prev = this.getStage(this.getStageCount()-1);
        stage.setPreviousStage(prev);
        prev.setNextStage(stage);

        this.stages.add(stage);
        this.updateStages();
    }

    private void resetStagesCount() {
        for (Stage s : this.stages) s.resetStageCount();
    }

    private static ArrayList<Stage> getDefaultStages() {
        ArrayList<Stage> r = new ArrayList<>(4);
        Stage bedrock = new Stage(Mine.STATE_ZERO.name(), Integer.MAX_VALUE);
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

    /**
     * Recalculates the number of blocks for each stage in the mine
     */
    public void updateStages() {
        this.resetStagesCount();

        for(Location loc: this.getMineBlocks()) {
            Material mat = loc.getBlock().getType();
            Stage match = this.getStage(mat.name());
            if (match != null) match.incrementStageBlocks();
        }
    }

    /*public static void setMineDelay(int delay) {
        Mine.MINE_DELAY = delay;
    }*/

    public static void setDefaultStart(boolean start) {
        Mine.DEFAULT_START = start;
    }

    @Nullable
    public Stage getStage(String search) {
        return this.stages.stream().filter( e -> e.getName().equalsIgnoreCase(search) ).findAny().orElse(null);
    }

    @Nullable
    public static Mine getMine(ArrayList<Mine> minas, String search) {
        return minas.stream().filter( e -> e.mineName.equalsIgnoreCase(search) ).findAny().orElse(null);
    }

    /*@Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        while (true) {
            if (!this.start) continue; // TODO notifies
            this.currentTime++;
            if (this.currentTime < (Mine.MINE_DELAY * 20D) / this.getTotalBlocks()) continue;

            this.currentTime = 0;
            Location loc = this.getRandomBlockInMine();
            Stage current = this.getStage(loc.getBlock().getType().toString());
            if (current == null) continue; // wtf
            Stage next = current.getNextStage();
            if (next != null && next.fitsOneBlock()) {
                current.decrementStageBlocks();
                next.incrementStageBlocks();
                loc.getBlock().setType(next.getStageMaterial());
            }
        }
    }*/
}
