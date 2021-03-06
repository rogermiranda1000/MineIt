package com.rogermiranda1000.mineit.file;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;

import java.util.ArrayList;

public class BasicLocation {
    private final double x;
    private final double y;
    private final double z;

    public BasicLocation(Location l) {
        this.x = l.getX();
        this.y = l.getY();
        this.z = l.getZ();
    }

    public Location getLocation(@Nullable String worldName) {
        return new Location(Bukkit.getWorld(worldName), this.x, this.y, this.z);
    }

    public static ArrayList<Location> getLocations(@Nullable String world, ArrayList<BasicLocation> locations) throws InvalidLocationException {
        ArrayList<Location> r = new ArrayList<>(locations.size());
        for (BasicLocation bl : locations) {
            Location loc = bl.getLocation(world);
            try {
                loc.getBlock(); // throws NPE if invalid world
                r.add(loc);
            } catch (NullPointerException ex) {
                throw new InvalidLocationException("Invalid location (world:" + loc.getWorld() + ", x:" + loc.getX() + ", y:" + loc.getY() + ", z:" + loc.getZ() + ")");
            }
        }
        return r;
    }
}
