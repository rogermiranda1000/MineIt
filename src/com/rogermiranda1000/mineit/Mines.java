package com.rogermiranda1000.mineit;

import java.util.ArrayList;
import java.util.List;

public class Mines {
    List<Location> bloques = new ArrayList<Location>();
    public String[] stages = {"BEDROCK", "STONE", "OBSIDIAN", "DIAMOND_ORE"};
    public String name = "";

    Mines() { }

    public void add(String w, double x, double y, double z) {
        bloques.add(new Location(w,x,y,z));
    }

    public String[] loc() {
        List<String> l = new ArrayList<String>();
        for(Location b: bloques) l.add(b.world+","+String.valueOf(b.x)+","+String.valueOf(b.y)+","+String.valueOf(b.z));
        return l.toArray(new String[l.size()]);
    }
}

class Location {
    String world;
    double x;
    double y;
    double z;

    Location(String world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
