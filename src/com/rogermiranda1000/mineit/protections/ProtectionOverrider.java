package com.rogermiranda1000.mineit.protections;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public interface ProtectionOverrider {
    /**
     * It gets the object required for overrideProtection
     * @param event BlockBreakEvent
     * @return Object required for overrideProtection; null if no region
     */
    public Object getProtection(BlockBreakEvent event);

    public void overrideProtection(@NotNull Object region, Player player);
}
