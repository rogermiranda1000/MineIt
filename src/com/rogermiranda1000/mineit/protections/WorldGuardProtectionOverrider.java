package com.rogermiranda1000.mineit.protections;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.event.block.BlockBreakEvent;

public class WorldGuardProtectionOverrider implements ProtectionOverrider {
    @Override
    public void overrideProtection(BlockBreakEvent event) {
        org.bukkit.Location tmp = event.getBlock().getLocation();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        Location loc = new Location(BukkitAdapter.adapt(tmp.getWorld()), tmp.getX(), tmp.getY(), tmp.getZ());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (!query.testState(loc, localPlayer, Flags.BLOCK_BREAK)) {
            // Can't destroy
        }
    }
}
