package com.rogermiranda1000.mineit.blocks;

import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.helper.blocks.CachedCustomBlock;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public class SelectedBlocks extends CachedCustomBlock<OfflinePlayer> {
    private static final String id = "SelectedBlocks";
    private static SelectedBlocks instance = null;

    public SelectedBlocks(RogerPlugin plugin) {
        super(plugin, SelectedBlocks.id, e -> e instanceof BlockBreakEvent, false, false, null);
    }

    public static SelectedBlocks getInstance() {
        return SelectedBlocks.instance;
    }

    public static SelectedBlocks setInstance(SelectedBlocks mines) {
        SelectedBlocks.instance = mines;
        return SelectedBlocks.instance;
    }

    @NotNull
    @Override
    @SuppressWarnings("ConstantConditions") // ignore NotNull
    public OfflinePlayer onCustomBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        return null; // never reached
    }

    @Override
    public boolean onCustomBlockBreak(BlockBreakEvent blockBreakEvent, OfflinePlayer offlinePlayer) {
        blockBreakEvent.setCancelled(true); // anyone can break a selected block
        return true;
    }
}
