package com.rogermiranda1000.mineit.protections;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.event.block.BlockBreakEvent;

public class WorldGuardProtectionOverrider implements ProtectionOverrider {
    @Override
    public void overrideProtection(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager;
        if (container == null || (manager = container.get(BukkitAdapter.adapt(loc.getWorld()))) == null) return; // no region
        ApplicableRegionSet regions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
        for (ProtectedRegion region : regions.getRegions()) {
            // TODO
        }

        /*if (!query.testState(loc, localPlayer, Flags.BUILD)) {
            // Can't build
            // TODO
        }*/
    }
}
