package com.rogermiranda1000.mineit;

import com.rogermiranda1000.helper.blocks.CachedCustomBlock;
import com.rogermiranda1000.mineit.blocks.Mines;
import com.rogermiranda1000.versioncontroller.VersionController;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Mine implements Runnable {
    @Nullable
    public static Material AIR_STAGE;
    public static final Material SELECT_BLOCK = Material.STONE;
    public static final Material STATE_ZERO = Material.BEDROCK;
    public static final Material DEFAULT_IDENTIFIER = Material.STONE;

    /**
     * Default seconds to chenge the stage
     */
    private static final int DEFAULT_DELAY = 30;

    /**
     * Ticks per block (seconds per block * 20)
     */
    private int delay;
    private final ArrayList<MineChangedEvent> events;

    private final CachedCustomBlock<Mine> blocks;
    private int currentTime;
    private final ArrayList<Stage> stages;
    private final String mineName;
    private BlockType mineBlockIdentifier;
    @Nullable Location tp;
    private boolean started;
    private Integer scheduleID;
    private final int hashCode;

    public Mine(CachedCustomBlock<Mine> blocks, String name, BlockType identifier, boolean started, ArrayList<Stage> stages, int delay, @Nullable Location tp) {
        this.currentTime = 0;
        this.events = new ArrayList<>();

        this.blocks = blocks;
        this.mineName = name;
        this.mineBlockIdentifier = identifier;
        this.tp = tp;
        this.stages = stages;
        this.setDelay(delay);
        this.hashCode = name.hashCode();

        // if we start it before setting the stagelimit we'll get wrong results; the mine is started at `Mines#addMine()`
        //this.setStart(started);
        this.started = started;
        this.scheduleID = null;
    }

    public Mine(CachedCustomBlock<Mine> blocks, String name, String identifier, boolean started, ArrayList<Stage> stages, int delay, @Nullable Location tp) {
        this(blocks, name, VersionController.get().getMaterial(identifier), started, stages, delay, tp);
    }

    public Mine(CachedCustomBlock<Mine> blocks, String name, ArrayList<Location> list) {
        this(blocks, name, Mine.DEFAULT_IDENTIFIER.name(), false, Mine.getDefaultStages(), DEFAULT_DELAY, null);
        this.blocks.placeBlocksArtificially(this, list);
    }

    public void setStart(boolean value) {
        if (this.started == value && this.scheduleID != null) return;

        this.started = value;
        if (value) {
            this.updateStages(); // maybe its blocks has been changed while being stopped
            this.scheduleID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MineIt.instance, this, 1, 1);
        }
        else {
            if (this.scheduleID != null) {
                Bukkit.getServer().getScheduler().cancelTask(this.scheduleID);
                this.scheduleID = null;
            }
        }

        this.notifyMineListeners();
    }

    @Nullable
    public Location getTp() {
        return this.tp;
    }

    public void setTp(@Nullable Location tp) {
        this.tp = tp;
    }

    public BlockType getMineBlockIdentifier() {
        return this.mineBlockIdentifier;
    }

    public void setMineBlockIdentifier(BlockType mineBlockIdentifier) {
        this.mineBlockIdentifier = mineBlockIdentifier;

        Mines.getInstance().notifyMinesListeners();
        this.notifyMineListeners();
    }

    public boolean getStart() {
        return this.started;
    }

    public ArrayList<MineChangedEvent> getEvents() {
        return this.events;
    }

    public String getName() {
        return this.mineName;
    }

    public boolean isStarted() {
        return this.started;
    }

    public void add(Location loc) {
        this.blocks.placeBlockArtificially(this, loc);
    }

    private List<Location> _getMineBlocks() {
        List<Location> r = this.blocks.getAllBlocksByValue(this);
        return (r == null) ? new ArrayList<>() : r;
    }

    public Location []getMineBlocks() {
        return this._getMineBlocks().toArray(new Location[0]);
    }

    public int getTotalBlocks() {
        return this._getMineBlocks().size();
    }

    public Location getRandomBlockInMine() {
        return this._getMineBlocks().get(new Random().nextInt(this.getTotalBlocks()));
    }

    public ArrayList<Stage> getStages() {
        return this.stages;
    }

    public void setStageLimit(int index, int limit) {
        this.stages.get(index).setStageLimit(limit);

        Mines.getInstance().notifyMinesListeners();
        this.notifyMineListeners();
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

        Mines.getInstance().notifyMinesListeners();
        this.notifyMineListeners();
    }

    public void addStage(Stage stage) {
        Stage prev = this.getStage(this.getStageCount()-1);
        if (stage.isBreakable()) stage.setPreviousStage(prev);
        prev.setNextStage(stage);

        this.stages.add(stage);
        this.updateStages();

        Mines.getInstance().notifyMinesListeners();
        this.notifyMineListeners();
    }

    /**
     * Sets all the blocks of the mine to STATE_ZERO
     */
    public void resetBlocksMine() {
        for (Location l: this._getMineBlocks()) {
            if (l.getBlock().getType()!=Mine.STATE_ZERO) l.getBlock().setType(Mine.STATE_ZERO);
        }
    }

    private void resetStagesCount() {
        for (Stage s : this.stages) s.resetStageCount();
    }

    private static ArrayList<Stage> getDefaultStages() {
        ArrayList<Stage> r = new ArrayList<>(4);
        Stage bedrock = new Stage(Mine.STATE_ZERO.name(), Integer.MAX_VALUE, false);
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

        for(Location loc: this._getMineBlocks()) {
            Stage match = this.getStage(VersionController.get().getObject(loc.getBlock()));
            if (match != null) match.incrementStageBlocks();
        }
    }

    /**
     * Override the current seconds per block
     * /!\ If 'delay' is 0 it will be replaced with DEFAULT_DELAY
     * @param delay Seconds to change the stage
     */
    public void setDelay(int delay) {
        if (delay < 1) delay = DEFAULT_DELAY;
        synchronized (this) {
            this.delay = delay * 20;
            this.currentTime = 0;
        }

        this.notifyMineListeners();
    }

    /**
     * Get the mine delay
     * @return Seconds to change the stage
     */
    synchronized public int getDelay() {
        return this.delay / 20;
    }

    @Nullable
    public Stage getStage(BlockType search) {
        return this.stages.stream().filter( e -> e.getStageMaterial().equals(search) ).findAny().orElse(null);
    }

    public void addMineListener(MineChangedEvent e) {
        this.events.add(e);
    }

    public void removeMineListener(MineChangedEvent e) {
        this.events.remove(e);
    }

    private void notifyMineListeners() {
        for (MineChangedEvent e : this.events) e.onMineChanged();
    }

    @Override
    public String toString() {
        return this.mineName;
    }

    @Override
    public void run() {
        int changedBlocks;
        synchronized (this) {
            this.currentTime++;
            changedBlocks = (this.currentTime * this.getTotalBlocks()) / this.getDelay();
            if (changedBlocks == 0) return;

            this.currentTime = 0;
        }
        // we need to change 'changedBlocks' blocks
        for (int x = 0; x < changedBlocks; x++) {
            Location loc = this.getRandomBlockInMine();
            Stage current = this.getStage(VersionController.get().getObject(loc.getBlock())); // TODO I think there's here a synchronization problem with stage
            if (current == null) continue; // ?
            Stage next = current.getNextStage();
            if (next != null && next.fitsOneBlock()) {
                current.decrementStageBlocks();
                next.incrementStageBlocks();
                next.getStageMaterial().setType(loc.getBlock());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Mine)) return false;
        Mine that = (Mine) o;
        return this.getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
