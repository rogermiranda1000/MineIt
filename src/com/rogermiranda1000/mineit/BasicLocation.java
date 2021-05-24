package com.rogermiranda1000.mineit;

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

    public static Location[] getLocations(ArrayList<BasicLocation> locations) {
        Location[] r = new Location[locations.size()];
        for (int i = 0; i < r.length; i++) r[i] = locations.get(i).getLocation();
        return r;
    }
}
