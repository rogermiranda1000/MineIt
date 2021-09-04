package com.rogermiranda1000.mineit.protections;

import com.rogermiranda1000.mineit.MineIt;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
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
            if (tmp.getWorld() == null) return;
            RegionManager manager = container.get( BukkitAdapter.adapt(tmp.getWorld()) );
            if (manager == null) return;

            manager.getApplicableRegions(BukkitAdapter.asBlockVector(tmp)).forEach((region)->{
                // TODO region.getParent() == null
                DefaultDomain members = region.getMembers();
                System.out.println(members.toString());
                if (!members.contains(localPlayer)) {
                    members.addPlayer(localPlayer);
                    System.out.println(region.getMembers().toString());
                    Bukkit.getScheduler().runTaskLater(MineIt.instance,()->region.getMembers().removePlayer(localPlayer),1);
                }
            });
        }
    }
}
