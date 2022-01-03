package com.rogermiranda1000.mineit.protections;

import com.rogermiranda1000.mineit.MineIt;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
//import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * WorldGuard for version < 1.13
 */
public class WorldGuardProtectionOverriderPre13 implements ProtectionOverrider {
    private Method adapt_location, getRegionContainer, createQuery, get_world, aplicableRegions_location;
    private boolean methodFail;

    public WorldGuardProtectionOverriderPre13() {
        this.methodFail = false;

        try {
            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter"),
                    regionContainerClass = Class.forName("com.sk89q.worldguard.bukkit.RegionContainer"),
                    regionQueryClass = Class.forName("com.sk89q.worldguard.bukkit.RegionQuery");

            this.adapt_location = bukkitAdapterClass.getDeclaredMethod("adapt", org.bukkit.Location.class);
            this.adapt_location.setAccessible(true);

            this.getRegionContainer = WorldGuardPlugin.class.getDeclaredMethod("getRegionContainer");
            this.createQuery = regionContainerClass.getDeclaredMethod("createQuery");
            this.get_world = regionContainerClass.getDeclaredMethod("get", org.bukkit.World.class);
            this.aplicableRegions_location = RegionManager.class.getDeclaredMethod("getApplicableRegions", org.bukkit.Location.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
            this.methodFail = true;
        }
    }

    @Override
    public Object getProtection(BlockBreakEvent event) {
        ArrayList<ProtectedRegion> regions = new ArrayList<>();
        if (this.methodFail) return regions;

        try {
            org.bukkit.Location tmp = event.getBlock().getLocation();
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());

            Location loc = (Location) this.adapt_location.invoke(null, tmp);
            /*RegionContainer*/ Object container = this.getRegionContainer.invoke(WorldGuardPlugin.inst());
            /*RegionQuery*/ Object query = this.createQuery.invoke(container);

            //if (query.testState(loc, localPlayer, Flags.BLOCK_BREAK)) return null;

            // Can't destroy
            RegionManager manager;
            if (tmp.getWorld() == null || (manager = (RegionManager) this.get_world.invoke(container, tmp.getWorld())) == null) return null;

            for (ProtectedRegion region : ((ApplicableRegionSet) this.aplicableRegions_location.invoke(manager, tmp)).getRegions()) {
                if (!region.getMembers().contains(localPlayer) && !region.getOwners().contains(localPlayer)) regions.add(region);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
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
