package com.rogermiranda1000.mineit.mine.blocks;

import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.helper.blocks.*;
import com.rogermiranda1000.mineit.*;
import com.rogermiranda1000.mineit.mine.Mine;
import com.rogermiranda1000.mineit.mine.MineBlock;
import com.rogermiranda1000.mineit.mine.MinesChangedEvent;
import com.rogermiranda1000.mineit.mine.stage.Stage;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Mines extends ComplexCachedCustomBlock<MineBlock,Mine> {
    public static class StoreMine implements StoreConversion<MineBlock> {
        public StoreMine() {}

        @Override
        public BiFunction<MineBlock, Location, String> storeName() {
            return (b,loc) -> b.getMine().getName();
        }

        @Override
        public BiFunction<String, Location, MineBlock> loadName() {
            return (name,loc) -> new MineBlock(loc,Mines.getInstance().getMine(name));
        }
    }

    private static final String id = "Mines";
    private static Mines instance = null;

    private final ArrayList<MinesChangedEvent> globalEvents;

    public Mines(RogerPlugin plugin) {
        super(plugin, Mines.id, e -> e instanceof BlockBreakEvent, true, false, new StoreMine(), true, MineBlock::getMine);
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
        for (Mine m : this.getAllCValues()) {
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
        List<CustomBlocksEntry<MineBlock>> toRemove = this.getAllBlocksByValue(m);
        for (CustomBlocksEntry<MineBlock> b : toRemove) {
            this.removeBlocksArtificiallyByValue(b.getKey());
        }
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
    public MineBlock onCustomBlockPlace(BlockPlaceEvent e) {
        return null; // never reached
    }

    @Override
    public boolean onCustomBlockBreak(BlockBreakEvent e, MineBlock b) {
        Player ply = e.getPlayer();
        if(!ply.hasPermission("mineit.mine.all") && !ply.hasPermission("mineit.mine."+b.getMine().getName())) {
            ply.sendMessage(MineIt.instance.errorPrefix + "You can't mine here!");
            e.setCancelled(true);
            return true;
        }

        e.setCancelled(this.breakBlock(ply, b.getMine(), b));
        return true;
    }

    /**
     * @retval TRUE     The event is cancelled
     * @retval FALSE    All ok
     */
    private boolean breakBlock(@Nullable Player ply, Mine m, MineBlock block) {
        @Nullable Stage s = block.getStage();
        Stage prev;

        if (s != null && !s.isBreakable() && (ply == null || !ply.hasPermission("mineit.unbreakable"))) return true; // cancel
        // if he have the permission, it will enter in the next if (there's no previous stage)

        if (s == null || (prev = s.getPreviousStage()) == null) {
            // unstaged block in mine or first stage mined
            prev = m.getStage(0);
        }

        if (s != null) s.decrementStageBlocks(); // decrement previous stage (if known)
        prev.incrementStageBlocks();
        block.setStage(prev); // now the block will be on the previous stage
        Mines.changeBlock(block.getBlockLocation().getBlock(), prev.getStageMaterial());
        return false;
    }

    private static void changeBlock(@NotNull Block b, BlockType type) {
        Bukkit.getScheduler().runTaskLater(MineIt.instance,()->type.setType(b),1);
    }
}
