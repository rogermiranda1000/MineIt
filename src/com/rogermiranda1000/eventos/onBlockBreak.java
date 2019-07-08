package com.rogermiranda1000.eventos;

import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.Mines;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class onBlockBreak implements Listener {
    boolean end = false;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        for(Mines m: MineIt.instance.minas) {
            for(String s: m.loc()) {
                Location loc = e.getBlock().getLocation();
                if(s.equalsIgnoreCase(loc.getWorld().getName()+","+String.valueOf(loc.getX())+","+String.valueOf(loc.getY())+","+String.valueOf(loc.getZ()))) {
                    e.setCancelled(true);
                    for(ItemStack item: e.getBlock().getDrops()) e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), item);
                    for(int x = 0; x < m.stages.length; x++) {
                        if(e.getBlock().getType().toString().equalsIgnoreCase(m.stages[x])) {
                            if(x>0) e.getBlock().setType(Material.getMaterial(m.stages[x-1]));
                            break;
                        }
                    }
                    return;
                }
            }
        }
    }
}
