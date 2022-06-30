package com.rogermiranda1000.mineit.events;

import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.Stage;
import com.rogermiranda1000.mineit.protections.OnEvent;
import com.rogermiranda1000.versioncontroller.VersionController;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

public class BreakEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Mine m = Mine.getMine(e.getBlock().getLocation());
        if (m == null) {
            if (MineIt.instance.isSelected(e.getBlock().getLocation())) {
                // trying to break a selected block
                e.setCancelled(true);
                return;
            }

            // no mine; launch other's protections
            for (OnEvent prot : MineIt.instance.protectionOverrider) {
                if (!e.isCancelled()) prot.onEvent(e);
            }
            return;
        }

        Player ply = e.getPlayer();
        if(!ply.hasPermission("mineit.mine.all") && !ply.hasPermission("mineit.mine."+m.getName())) {
            ply.sendMessage(MineIt.errorPrefix + "You can't mine here!");
            e.setCancelled(true);
            return;
        }

        this.breakBlock(m, e.getBlock());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        for (Block b : e.blockList()) {
            Mine m = Mine.getMine(b.getLocation());
            if (m == null) continue;

            this.breakBlock(m, b);
            // TODO e.setCancelled(false)
        }
    }

    private void breakBlock(Mine m, Block block) {
        Stage s = m.getStage(VersionController.get().getObject(block));
        Stage prev;
        if (s == null || (prev = s.getPreviousStage()) == null) {
            // unstaged block in mine or first stage mined
            BreakEvent.changeBlock(block, m.getStage(0).getStageMaterial());
            return;
        }

        s.decrementStageBlocks();
        prev.incrementStageBlocks();
        BreakEvent.changeBlock(block, prev.getStageMaterial());
    }

    private static void changeBlock(@NotNull Block b, Object type) {
        Bukkit.getScheduler().runTaskLater(MineIt.instance,()->VersionController.get().setType(b, type),1);
    }
}
