package com.rogermiranda1000.mineit.blocks;

import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.helper.blocks.CachedCustomBlock;
import com.rogermiranda1000.helper.blocks.StoreConversion;
import com.rogermiranda1000.mineit.*;
import com.rogermiranda1000.versioncontroller.VersionController;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Function;

public class Mines extends CachedCustomBlock<Mine> {
    public static class StoreMine implements StoreConversion<Mine> {
        public StoreMine() {}

        @Override
        public Function<Mine, String> storeName() {
            return Mine::getName;
        }

        @Override
        public Function<String, Mine> loadName() {
            return (name) -> Mines.getInstance().getMine(name);
        }
    }

    private static final String id = "Mines";
    private static Mines instance = null;

    private final ArrayList<MinesChangedEvent> globalEvents;

    public Mines(RogerPlugin plugin) {
        super(plugin, Mines.id, e -> e instanceof BlockBreakEvent, true, false, new StoreMine(), true);
        this.globalEvents = new ArrayList<>();
    }

    public static Mines getInstance() {
        return Mines.instance;
    }

    public static Mines setInstance(Mines mines) {
        Mines.instance = mines;
        return Mines.instance;
    }

    @Nullable
    public Mine getMine(String name) {
        for (Mine m : this.getAllValues()) {
            if (m.getName().equals(name)) return m;
        }
        return null;
    }

    /**
     * Adds a mine in 'stop' state
     * @param m Mine to add
     */
    public void addMine(Mine m) {
        this.addObject(m);
        for (MinesChangedEvent e : this.globalEvents) e.onMineAdded(m);
    }

    public void removeMine(Mine m) {
        this.removeBlocksArtificiallyByValue(m);
        this.removeObject(m);

        for (MinesChangedEvent e : new ArrayList<>(this.globalEvents)) e.onMineRemoved(m);

        // notify & unsubscribe
        for (MineChangedEvent e : new ArrayList<>(m.getEvents())) {
            e.onMineRemoved();
            m.getEvents().remove(e);
        }

        m.setStart(false); // stop the mine
    }

    public void addMinesListener(MinesChangedEvent e) {
        this.globalEvents.add(e);
    }

    public void notifyMinesListeners() {
        for (MinesChangedEvent e : this.globalEvents) e.onMinesChanged();
    }

    @Override
    @NotNull
    @SuppressWarnings("ConstantConditions") // ignore NotNull
    public Mine onCustomBlockPlace(BlockPlaceEvent e) {
        return null; // never reached
    }

    @Override
    public boolean onCustomBlockBreak(BlockBreakEvent e, Mine m) {
        Player ply = e.getPlayer();
        if(!ply.hasPermission("mineit.mine.all") && !ply.hasPermission("mineit.mine."+m.getName())) {
            ply.sendMessage(MineIt.instance.errorPrefix + "You can't mine here!");
            e.setCancelled(true);
            return true;
        }

        e.setCancelled(this.breakBlock(ply, m, e.getBlock()));
        return true;
    }

    /**
     * @retval TRUE     The event is cancelled
     * @retval FALSE    All ok
     */
    private boolean breakBlock(@Nullable Player ply, Mine m, Block block) {
        @Nullable Stage s = m.getStage(VersionController.get().getObject(block));
        Stage prev;

        if (s != null && !s.isBreakable() && (ply == null || !ply.hasPermission("mineit.unbreakable"))) return true; // cancel
        // if he have the permission, it will enter in the next if (there's no previous stage)

        if (s == null || (prev = s.getPreviousStage()) == null) {
            // unstaged block in mine or first stage mined
            Mines.changeBlock(block, m.getStage(0).getStageMaterial());
            return false;
        }

        s.decrementStageBlocks();
        prev.incrementStageBlocks();
        Mines.changeBlock(block, prev.getStageMaterial());
        return false;
    }

    private static void changeBlock(@NotNull Block b, BlockType type) {
        Bukkit.getScheduler().runTaskLater(MineIt.instance,()->type.setType(b),1);
    }
}
