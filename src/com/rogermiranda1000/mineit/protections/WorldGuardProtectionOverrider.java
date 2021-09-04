package com.rogermiranda1000.mineit.protections;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.event.block.BlockBreakEvent;

public class WorldGuardProtectionOverrider implements ProtectionOverrider {
    @Override
    public void overrideProtection(BlockBreakEvent event) {
        org.bukkit.Location tmp = event.getBlock().getLocation();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        Location loc = BukkitAdapter.adapt(tmp);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (!query.testState(loc, localPlayer, Flags.BLOCK_BREAK)) {
            // Can't destroy
            RegionManager manager = container.get( BukkitAdapter.adapt(tmp.getWorld()) );
            ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(tmp));
            String greeting = set.queryValue(localPlayer, Flags.DENY_MESSAGE);
            System.out.println(greeting);
        }
    }
}
