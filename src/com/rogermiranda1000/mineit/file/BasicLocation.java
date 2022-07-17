package com.rogermiranda1000.mineit.file;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class BasicLocation {
    private final String world;
    private final double x;
    private final double y;
    private final double z;

    public BasicLocation(Location l) {
        this.world = (l.getWorld() == null) ? "" : l.getWorld().getName();
        this.x = l.getX();
        this.y = l.getY();
        this.z = l.getZ();
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z);
    }
}
