package com.rogermiranda1000.mineit.protections;

import com.google.common.base.Preconditions;
import com.rogermiranda1000.mineit.MineIt;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class WorldGuardProtectionOverrider implements ProtectionOverrider {
    @Override
    public Object getProtection(BlockBreakEvent event) {
        org.bukkit.Location tmp = event.getBlock().getLocation();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        Location loc = BukkitAdapter.adapt(tmp);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (query.testState(loc, localPlayer, Flags.BLOCK_BREAK)) return null;

        // Can't destroy
        RegionManager manager;
        if (tmp.getWorld() == null || (manager = container.get(BukkitAdapter.adapt(tmp.getWorld()))) == null) return null;

        ArrayList<ProtectedRegion> regions = new ArrayList<>();
        for (ProtectedRegion region : manager.getApplicableRegions(BukkitAdapter.asBlockVector(tmp)).getRegions()) {
            if (!region.getMembers().contains(localPlayer) && !region.getOwners().contains(localPlayer)) regions.add(region);
        }

        return (regions.isEmpty() ? null : regions);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void overrideProtection(@NotNull Object region, Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        ((ArrayList<ProtectedRegion>)region).forEach((r)->{
            // TODO region.getParent() == null
            DefaultDomain members = r.getMembers();
            members.addPlayer(localPlayer);
            Bukkit.getScheduler().runTaskLater(MineIt.instance,()->members.removePlayer(localPlayer),1);
        });
    }
}
