package com.rogermiranda1000.mineit.file;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;

public class BasicLocation {
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;

    public BasicLocation(Location l) {
        this.worldName = (l.getWorld() == null) ? "" : l.getWorld().getName();
        this.x = l.getX();
        this.y = l.getY();
        this.z = l.getZ();
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(this.worldName), this.x, this.y, this.z);
    }

    public static ArrayList<Location> getLocations(ArrayList<BasicLocation> locations) {
        ArrayList<Location> r = new ArrayList<>(locations.size());
        for (BasicLocation bl : locations) r.add(bl.getLocation());
        return r;
    }
}
