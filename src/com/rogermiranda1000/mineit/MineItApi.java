package com.rogermiranda1000.mineit;

import com.rogermiranda1000.mineit.blocks.Mines;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Set;

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
        return Mines.getInstance().getBlock(loc);
    }

    public int getMineCount() {
        return Mines.getInstance().getDifferentValuesNum();
    }

    public Set<Mine> getMines() {
        return Mines.getInstance().getAllValues();
    }
}
