package com.rogermiranda1000.mineit.events;

import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.Stage;
import com.rogermiranda1000.versioncontroller.VersionController;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BlockBreakEvent implements Listener {
    @EventHandler
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent e) {
        if(!MineIt.instance.overrideProtection && e.isCancelled()) return;

        Mine m = Mine.getMine(e.getBlock().getLocation());
        if (m == null) return;

        Player ply = e.getPlayer();
        if(!ply.hasPermission("mineit.mine.all") && !ply.hasPermission("mineit.mine."+m.mineName)) {
            ply.sendMessage(MineIt.errorPrefix + "You can't mine here!");
            e.setCancelled(true);
            return;
        }

        e.setCancelled(false);

        Stage s = m.getStage(e.getBlock().getType().toString());
        if (s == null) {
            // unstaged block in mine
            establecer(e.getBlock(), m.getStages().get(0).getStageMaterial());
            return;
        }

        Stage prev = s.getPreviousStage();
        if(prev == null) {
            // first stage mined
            establecer(e.getBlock(), m.getStages().get(0).getStageMaterial());
            return;
        }

        s.decrementStageBlocks();
        prev.incrementStageBlocks();
        establecer(e.getBlock(), prev.getStageMaterial());
    }

    private static void establecer(Block b,Object type){
        Bukkit.getScheduler().runTaskLater(MineIt.instance,()->VersionController.get().setType(b, type),1);
    }
}
