package com.rogermiranda1000.mineit;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;

public class Mine implements Runnable {
    public static final Material STATE_ZERO = Material.BEDROCK;

    /**
     * Ticks per block (seconds per block * 20)
     */
    private static int MINE_DELAY;
    private final ArrayList<Location> blocks;
    public int currentTime;
    private final ArrayList<Stage> stages;
    public final String mineName;
    private boolean started;
    private int scheduleID;

    public Mine(String name, boolean started, ArrayList<Location> blocks, ArrayList<Stage> stages) {
        this.currentTime = 0;

        this.mineName = name;
        this.setStart(started);
        this.blocks = blocks;
        this.stages = stages;
    }

    public Mine(String name, ArrayList<Location> blocks) {
        this(name, false, blocks, Mine.getDefaultStages());
    }

    public void setStart(boolean value) {
        if (this.started == value) return;

        this.started = value;
        if (value) this.scheduleID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MineIt.instance, this, 1, 1);
        else Bukkit.getServer().getScheduler().cancelTask(this.scheduleID);
    }

    public boolean getStart() {
        return this.started;
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

    /**
     * Sets all the blocks of the mine to STATE_ZERO
     */
    public void resetBlocksMine() {
        for (Location l: this.blocks) {
            if (l.getBlock().getType()!=Mine.STATE_ZERO) l.getBlock().setType(Mine.STATE_ZERO);
        }
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

    public static void setMineDelay(int delay) {
        Mine.MINE_DELAY = delay * 20;
    }

    @Nullable
    public Stage getStage(String search) {
        return this.stages.stream().filter( e -> e.getName().equalsIgnoreCase(search) ).findAny().orElse(null);
    }

    @Nullable
    public static Mine getMine(ArrayList<Mine> minas, String search) {
        return minas.stream().filter( e -> e.mineName.equalsIgnoreCase(search) ).findAny().orElse(null);
    }

    @Override
    public void run() {
        this.currentTime++;
        int changedBlocks = (this.currentTime * this.getTotalBlocks()) / Mine.MINE_DELAY;
        if (changedBlocks == 0) return;

        this.currentTime = 0;
        // we need to change 'changedBlocks' blocks
        for (int x = 0; x < changedBlocks; x++) {
            Location loc = this.getRandomBlockInMine();
            Stage current = this.getStage(loc.getBlock().getType().toString());
            if (current == null) return; // wtf
            Stage next = current.getNextStage();
            if (next != null && next.fitsOneBlock()) {
                current.decrementStageBlocks();
                next.incrementStageBlocks();
                loc.getBlock().setType(next.getStageMaterial());
            }
        }
    }
}
