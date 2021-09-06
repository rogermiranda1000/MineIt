package com.rogermiranda1000.mineit.events;

import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.Stage;
import com.rogermiranda1000.mineit.protections.ProtectionOverrider;
import com.rogermiranda1000.versioncontroller.VersionController;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class BreakEvent implements Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if(!MineIt.instance.overrideProtection) return;

        Mine m = Mine.getMine(e.getBlock().getLocation());
        if (m == null) return;

        Player ply = e.getPlayer();
        if(!ply.hasPermission("mineit.mine.all") && !ply.hasPermission("mineit.mine."+m.mineName)) {
            ply.sendMessage(MineIt.errorPrefix + "You can't mine here!");
            e.setCancelled(true);
            return;
        }

        for (ProtectionOverrider prot : MineIt.instance.protectionOverrider) prot.overrideProtection(e);

        Stage s = m.getStage(e.getBlock().getType().toString());
        Stage prev;
        if (s == null || (prev = s.getPreviousStage()) == null) {
            // unstaged block in mine or first stage mined
            BreakEvent.changeBlock(e.getBlock(), m.getStage(0).getStageMaterial());
            return;
        }

        s.decrementStageBlocks();
        prev.incrementStageBlocks();
        BreakEvent.changeBlock(e.getBlock(), prev.getStageMaterial());
    }

    private static void changeBlock(@NotNull Block b, Object type) {
        Bukkit.getScheduler().runTaskLater(MineIt.instance,()->VersionController.get().setType(b, type),1);
    }
}
