package com.rogermiranda1000.mineit.events;

import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.Stage;
import com.rogermiranda1000.mineit.protections.OnEvent;
import com.rogermiranda1000.versioncontroller.VersionController;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            for (int n = 0; n < MineIt.instance.protectionOverrider.size() && !e.isCancelled(); n++) {
                OnEvent prot = MineIt.instance.protectionOverrider.get(n);
                // launch other protection event
                boolean err = prot.onEvent(e);
                if (err) {
                    MineIt.instance.printConsoleErrorMessage("Protection override failure, removing from list. Notice this may involve players being able to remove protected regions, so report this error immediately.");
                    MineIt.instance.protectionOverrider.remove(n);
                    n--; // the n++ will leave the same index on the next iteration
                }
            }
            return;
        }

        Player ply = e.getPlayer();
        if(!ply.hasPermission("mineit.mine.all") && !ply.hasPermission("mineit.mine."+m.getName())) {
            ply.sendMessage(MineIt.errorPrefix + "You can't mine here!");
            e.setCancelled(true);
            return;
        }

        e.setCancelled(this.breakBlock(ply, m, e.getBlock()));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        for (Block b : e.blockList()) {
            Mine m = Mine.getMine(b.getLocation());
            if (m == null) continue;

            this.breakBlock(null, m, b);
            // TODO e.setCancelled(false)
        }
    }

    /**
     * @retval TRUE     The event is cancelled
     * @retval FALSE    All ok
     */
    private boolean breakBlock(@Nullable Player ply, Mine m, Block block) {
        @Nullable Stage s = m.getStage(VersionController.get().getObject(block));
        Stage prev;

        if (s != null && !s.isBreakable() && (ply == null || !ply.hasPermission("mineit.unbreakable"))) return true; // cancel
        // if he have the permission, it will enter in the next if (there's no previous stage)

        if (s == null || (prev = s.getPreviousStage()) == null) {
            // unstaged block in mine or first stage mined
            BreakEvent.changeBlock(block, m.getStage(0).getStageMaterial());
            return false;
        }

        s.decrementStageBlocks();
        prev.incrementStageBlocks();
        BreakEvent.changeBlock(block, prev.getStageMaterial());
        return false;
    }

    private static void changeBlock(@NotNull Block b, BlockType type) {
        Bukkit.getScheduler().runTaskLater(MineIt.instance,()->type.setType(b),1);
    }
}
