package com.rogermiranda1000.mineit;

import com.rogermiranda1000.mineit.blocks.Mines;
//import me.Mohamad82.MineableGems.Core.CustomAttribute;
import me.Mohamad82.MineableGems.Core.CustomDrop;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockBreakEvent;

public class MineableGemsMine /*implements CustomAttribute*/ {
    private static final String customAttributeName = "mine";

    //@Override
    public CustomDrop readCustomDrop(CustomDrop customDrop, ConfigurationSection configurationSection) {
        String mineName = configurationSection.getString("Mine", null);
        if (mineName != null) customDrop.addCustomAttribute(MineableGemsMine.customAttributeName, mineName);
        return customDrop;
    }

    //@Override
    public boolean shouldPass(BlockBreakEvent blockBreakEvent, CustomDrop customDrop) {
        String mineName = (String) customDrop.getCustomAttribute(MineableGemsMine.customAttributeName);
        if (mineName == null) return true; // no mine => any drop can happen

        Mine miningInMine = Mines.getInstance().getBlock(blockBreakEvent.getBlock().getLocation());
        if (miningInMine == null) return false; // mining outside a mine, but the drop is only for one mine
        return miningInMine.getName().equals(mineName); // same mine => enable drop
    }
}
