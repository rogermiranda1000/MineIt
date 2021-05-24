package com.rogermiranda1000.mineit.events;

import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.Stage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class onBlockBreak implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if(e.isCancelled()) return;

        for(Mine m: MineIt.instance.minas) {
            for(Location mineLoc: m.getMineBlocks()) {
                Location loc = e.getBlock().getLocation();
                if(mineLoc.equals(loc)) {
                    Player ply = e.getPlayer();
                    if(!ply.hasPermission("mineit.mine.all") && !ply.hasPermission("mineit.mine."+m.name)) return;

                    Stage s = Stage.getMatch(m.getStages(), e.getBlock().getType().toString());
                    if (s == null) {
                        establecer(e.getBlock(), m.getStages().get(0).getStageMaterial());
                        return;
                    }

                    Stage prev = s.getPreviousStage();
                    if(prev == null) break;

                    s.decrementStageBlocks();
                    prev.incrementStageBlocks();
                    establecer(e.getBlock(), prev.getStageMaterial());
                    return;
                }
            }
        }
    }

    public void establecer(Block b,Material material){
        Bukkit.getScheduler().runTaskLater(MineIt.instance,()->b.setType(material),1);
    }
}
