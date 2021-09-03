package com.rogermiranda1000.mineit.protections;

import org.bukkit.Location;

public interface ProtectionOverrider {
    public void overrideProtection(org.bukkit.event.block.BlockBreakEvent event);
}
