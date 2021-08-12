package com.rogermiranda1000.mineit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.github.davidmoten.rtreemulti.Entry;
import com.github.davidmoten.rtreemulti.RTree;
import com.github.davidmoten.rtreemulti.geometry.Point;
import com.rogermiranda1000.versioncontroller.VersionController;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;

public class Mine implements Runnable {
    public static final Material AIR_STAGE = Material.GLASS;
    public static final Material STATE_ZERO = Material.BEDROCK;

    /**
     * Default seconds to chenge the stage
     */
    private static final int DEFAULT_DELAY = 30;

    /**
     * Used for optimize search
     */
    private static RTree<Mine, Point> tree = RTree.star().dimensions(5).create(); // MSB[world], LSB[world], x, y, z

    /**
     * All the active mines
     */
    private static final ArrayList<Mine> mines = new ArrayList<>();

    /**
     * Ticks per block (seconds per block * 20)
     */
    private int delay;

    private final ArrayList<Location> blocks;
    public int currentTime;
    private final ArrayList<Stage> stages;
    public final String mineName;
    private boolean started;
    private int scheduleID;

    public Mine(String name, boolean started, ArrayList<Location> blocks, ArrayList<Stage> stages, int delay) {
        this.currentTime = 0;

        this.mineName = name;
        this.blocks = blocks;
        this.stages = stages;
        this.setDelay(delay);

        this.setStart(started);
    }

    public Mine(String name, ArrayList<Location> blocks) {
        this(name, false, blocks, Mine.getDefaultStages(), DEFAULT_DELAY);
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
            Object mat = VersionController.get().getObject(loc.getBlock());
            Stage match = this.getStage(VersionController.get().getName(mat));
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
        this.delay = delay * 20;
    }

    /**
     * Get the mine delay
     * @return Seconds to change the stage
     */
    public int getDelay() {
        return this.delay / 20;
    }

    @Nullable
    public Stage getStage(String search) {
        return this.stages.stream().filter( e -> e.getName().equalsIgnoreCase(search) ).findAny().orElse(null);
    }

    @Nullable
    synchronized public static Mine getMine(String search) {
        return Mine.mines.stream().filter( e -> e.mineName.equalsIgnoreCase(search) ).findAny().orElse(null);
    }

    private static Point getPoint(Location loc) {
        if (loc.getWorld() == null) return Point.create(0,0,loc.getX(), loc.getY(), loc.getZ());

        return Point.create(Double.longBitsToDouble(loc.getWorld().getUID().getMostSignificantBits()),
                Double.longBitsToDouble(loc.getWorld().getUID().getLeastSignificantBits()),
                loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * It gets the mine that the location belongs
     * @param loc Location to get the mine
     * @return Mine that contains 'loc', null if any
     */
    @Nullable
    synchronized public static Mine getMine(Location loc) {
        Iterator<Entry<Mine, Point>> results = Mine.tree.search(Mine.getPoint(loc)).iterator();

        if (!results.hasNext()) return null;
        return results.next().value();
    }

    synchronized public static int getMinesLength() {
        return Mine.mines.size();
    }

    public static ArrayList<Mine> getMines() {
        return Mine.mines;
    }

    synchronized public static void removeMine(Mine m) {
        Mine.mines.remove(m);

        for (Location loc : m.blocks) Mine.tree = Mine.tree.delete(m, Mine.getPoint(loc));
    }

    /**
     * Updates the mine stages and add it into the internal list. It also adds them into the optimized search list.
     * @param m Mine to add
     */
    synchronized public static void addMine(Mine m) {
        m.updateStages();

        Mine.mines.add(m);

        for (Location loc : m.blocks) Mine.tree = Mine.tree.add(m, Mine.getPoint(loc));
    }

    @Override
    public void run() {
        this.currentTime++;
        int changedBlocks = (this.currentTime * this.getTotalBlocks()) / this.delay;
        if (changedBlocks == 0) return;

        this.currentTime = 0;
        // we need to change 'changedBlocks' blocks
        for (int x = 0; x < changedBlocks; x++) {
            Location loc = this.getRandomBlockInMine();
            Stage current = this.getStage(loc.getBlock().getType().toString());
            if (current == null) continue; // wtf
            Stage next = current.getNextStage();
            if (next != null && next.fitsOneBlock()) {
                current.decrementStageBlocks();
                next.incrementStageBlocks();
                VersionController.get().setType(loc.getBlock(), next.getStageMaterial());
            }
        }
    }
}
