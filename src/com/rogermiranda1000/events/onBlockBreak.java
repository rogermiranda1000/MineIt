package com.rogermiranda1000.events;

import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.Mines;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class onBlockBreak implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        for(Mines m: MineIt.instance.minas) {
            for(String s: m.loc()) {
                Location loc = e.getBlock().getLocation();
                if(s.equalsIgnoreCase(loc.getWorld().getName()+","+String.valueOf(loc.getX())+","+String.valueOf(loc.getY())+","+String.valueOf(loc.getZ()))) {
                    e.setCancelled(true);
                    Location l = e.getBlock().getLocation();
                    if(new Location(l.getWorld(), l.getX(), l.getY()+1, l.getZ()).getBlock().getType()==Material.AIR) l.setY(l.getY()+1D);
                    else if(new Location(l.getWorld(), l.getX()+1, l.getY(), l.getZ()).getBlock().getType()==Material.AIR) l.setX(l.getX()+1D);
                    else if(new Location(l.getWorld(), l.getX()-1, l.getY(), l.getZ()).getBlock().getType()==Material.AIR) l.setX(l.getX()-1D);
                    else if(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()+1).getBlock().getType()==Material.AIR) l.setZ(l.getZ()+1D);
                    else if(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()-1).getBlock().getType()==Material.AIR) l.setZ(l.getZ()-1D);
                    else if(new Location(l.getWorld(), l.getX(), l.getY()-1, l.getZ()).getBlock().getType()==Material.AIR) l.setY(l.getY()-1D);

                    for(ItemStack item: e.getBlock().getDrops(e.getPlayer().getItemInHand()/*getInventory().getItemInMainHand()*/))
                        e.getBlock().getWorld().dropItemNaturally(l, item);

                    for(int x = 0; x < m.stages.length; x++) {
                        if(!e.getBlock().getType().toString().equalsIgnoreCase(m.stages[x])) continue;

                        if(x>1) {
                            if(MineIt.instance.limit) {
                                m.stageBlocks[x]--;
                                m.stageBlocks[m.stageGo[x-2]]++;
                            }
                            e.getBlock().setType(Material.getMaterial(m.stages[m.stageGo[x-2]]));
                        }
                        else e.getBlock().setType(Material.getMaterial(m.stages[0]));
                        break;
                    }
                    return;
                }
            }
        }
    }
}
