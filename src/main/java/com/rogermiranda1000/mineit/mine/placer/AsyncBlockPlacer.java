package com.rogermiranda1000.mineit.mine.placer;

import com.github.davidmoten.rtreemulti.Entry;
import com.github.davidmoten.rtreemulti.RTree;
import com.github.davidmoten.rtreemulti.geometry.Point;
import com.github.davidmoten.rtreemulti.geometry.Rectangle;
import com.github.davidmoten.rtreemulti.geometry.internal.RectangleDouble;
import com.rogermiranda1000.helper.IteratorIterator;
import com.rogermiranda1000.helper.blocks.CustomBlock;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncBlockPlacer implements Listener, Runnable, BlockPlacer {
    public static final int MAX_BLOCKS_PER_TICK = 20000; // TODO move to config file

    private RTree<Boolean, Rectangle> loadedRegions;
    private RTree<BlockType, Point> todoBlocksInUnloadedChunks, todoBlocksInLoadedChunks;

    private AsyncBlockPlacer() {
        this.todoBlocksInUnloadedChunks = RTree.star().dimensions(5).create(); // MSB[world], LSB[world], x, y, z
        this.todoBlocksInLoadedChunks = RTree.star().dimensions(5).create(); // MSB[world], LSB[world], x, y, z
        this.loadedRegions = RTree.star().dimensions(5).create(); // MSB[world], LSB[world], x, y, z
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // search blocks from `todoBlocks` in the chunk and add them into `todoBlocksInLoadedChunks`
        Rectangle area = CustomBlock.getPointWithMargin(new Location(event.getWorld(), event.getChunk().getX() << 4, -64, event.getChunk().getZ() << 4));
        double []maxs = area.maxes().clone();
        maxs[2] += 15; maxs[3] += 500; maxs[4] += 15; // grab a whole chunk
        area = RectangleDouble.create(area.mins(), maxs);

        synchronized (this) {
            this.loadedRegions = this.loadedRegions.add(true, area);

            List<Entry<BlockType,Point>> toRemove = new ArrayList<>();
            this.todoBlocksInUnloadedChunks.search(area).forEach(e -> {
                this.todoBlocksInLoadedChunks = this.todoBlocksInLoadedChunks.add(e);
                toRemove.add(e);
            });
            this.todoBlocksInUnloadedChunks = this.todoBlocksInUnloadedChunks.delete(toRemove);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        System.out.println("Unloaded chunk " + event.getChunk().getX() + "," + event.getChunk().getZ());
    }

    @Override
    public synchronized void placeBlock(Location place, BlockType b) {
        Point placeRTree = CustomBlock.getPoint(place);
        boolean loadedChunk = this.loadedRegions.search(placeRTree).iterator().hasNext();
        if (loadedChunk) this.todoBlocksInLoadedChunks = this.todoBlocksInLoadedChunks.add(b, placeRTree);
        else this.todoBlocksInUnloadedChunks = this.todoBlocksInUnloadedChunks.add(b, placeRTree);
    }

    @Override
    public synchronized boolean isPending(final Location loc) {
        final AtomicBoolean pending = new AtomicBoolean(false);
        Iterator<Entry<BlockType, Point>> iter = new IteratorIterator<>(
                this.todoBlocksInUnloadedChunks.entries().iterator(),
                this.todoBlocksInLoadedChunks.entries().iterator()
        );

        iter.forEachRemaining(e -> {
            if (!pending.get()) {
                // still no pending element found
                Location pendingBlockLocation = CustomBlock.getLocation(e.geometry());
                pending.set(loc.equals(pendingBlockLocation));
            }
        });

        return pending.get();
    }

    @Override
    public void run() {
        this.todoBlocksInLoadedChunks.entries().forEach(e -> System.out.println("Need to place one " + e.value().getFriendlyName()));
        this.todoBlocksInUnloadedChunks.entries().forEach(e -> System.out.println("Requested to place one " + e.value().getFriendlyName() + " when loaded"));

        // get a set of blocks
        final List<Entry<BlockType,Point>> entriesToProcess = new ArrayList<>();
        synchronized (this) {
            this.todoBlocksInLoadedChunks.entries().forEach(e -> {
                if (entriesToProcess.size() < AsyncBlockPlacer.MAX_BLOCKS_PER_TICK) entriesToProcess.add(e);
            });
        }

        // set the blocks
        for (Entry<BlockType,Point> e : entriesToProcess) {
            BlockType requestedToPlace = e.value();
            Location requestedAt = CustomBlock.getLocation(e.geometry());

            requestedToPlace.setType(requestedAt.getBlock());
        }

        // remove
        synchronized (this) {
            this.todoBlocksInLoadedChunks = this.todoBlocksInLoadedChunks.delete(entriesToProcess);
        }
    }

    private static AsyncBlockPlacer instance = null;
    public static AsyncBlockPlacer getInstance() {
        if (AsyncBlockPlacer.instance == null) AsyncBlockPlacer.instance = new AsyncBlockPlacer();
        return AsyncBlockPlacer.instance;
    }
}
