package com.rogermiranda1000.mineit;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

/**
 * MineIt API. Use it to access to the plugin's functions.
 */
public class MineItApi {
    private static MineItApi mineItApi = null;

    private MineItApi() { }

    /**
     * Get the singletone class
     * @return Object to run all the MineIt API's functions
     */
    public static MineItApi getInstance() {
        if (MineItApi.mineItApi == null) MineItApi.mineItApi = new MineItApi();
        return MineItApi.mineItApi;
    }

    /**
     * Get the mine (if any) that a block belongs
     * @param block Block
     * @return Block's mine, or null (if any mine)
     */
    public Mine getMine(Block block) {
        return this.getMine(block.getLocation());
    }

    /**
     * Get the mine (if any) that a block belongs
     * @param loc Block location
     * @return Block's mine, or null (if any mine)
     */
    public Mine getMine(Location loc) {
        return Mine.getMine(loc);
    }

    public int getMineCount() {
        return Mine.getMinesLength();
    }

    public List<Mine> getMines() {
        return Mine.getMines();
    }
}
