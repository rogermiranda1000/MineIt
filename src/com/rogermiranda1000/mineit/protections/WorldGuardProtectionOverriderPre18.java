package com.rogermiranda1000.mineit.protections;

import com.rogermiranda1000.mineit.MineIt;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
//import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class WorldGuardProtectionOverriderPre18 implements ProtectionOverrider {
    @Override
    public Object getProtection(BlockBreakEvent event) {
        org.bukkit.Location tmp = event.getBlock().getLocation();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        ArrayList<ProtectedRegion> regions = new ArrayList<>();
        try {
            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Method adapt_location = bukkitAdapterClass.getDeclaredMethod("adapt", org.bukkit.Location.class);
            adapt_location.setAccessible(true);

            Location loc = (Location)adapt_location.invoke(null, tmp);
            RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
            RegionQuery query = container.createQuery();

            //if (query.testState(loc, localPlayer, Flags.BLOCK_BREAK)) return null;

            // Can't destroy
            RegionManager manager;
            if (tmp.getWorld() == null || (manager = container.get(tmp.getWorld())) == null) return null;

            for (ProtectedRegion region : manager.getApplicableRegions(tmp).getRegions()) {
                if (!region.getMembers().contains(localPlayer) && !region.getOwners().contains(localPlayer)) regions.add(region);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
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
