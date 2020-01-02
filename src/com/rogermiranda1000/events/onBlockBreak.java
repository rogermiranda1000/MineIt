package com.rogermiranda1000.events;

import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.Mines;
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

        for(Mines m: MineIt.instance.minas) {
            for(String s: m.loc()) {
                Location loc = e.getBlock().getLocation();
                if(s.equalsIgnoreCase(loc.getWorld().getName()+","+String.valueOf(loc.getX())+","+String.valueOf(loc.getY())+","+String.valueOf(loc.getZ()))) {
                    Player ply = e.getPlayer();
                    if(!ply.hasPermission("mineit.mine.all") && !ply.hasPermission("mineit.mine."+m.name)) return;

                    // Se aprueba
                    //e.setCancelled(true);
                    Location l = e.getBlock().getLocation();
                    if(new Location(l.getWorld(), l.getX(), l.getY()+1, l.getZ()).getBlock().getType()==Material.AIR) l.setY(l.getY()+1D);
                    else if(new Location(l.getWorld(), l.getX()+1, l.getY(), l.getZ()).getBlock().getType()==Material.AIR) l.setX(l.getX()+1D);
                    else if(new Location(l.getWorld(), l.getX()-1, l.getY(), l.getZ()).getBlock().getType()==Material.AIR) l.setX(l.getX()-1D);
                    else if(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()+1).getBlock().getType()==Material.AIR) l.setZ(l.getZ()+1D);
                    else if(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()-1).getBlock().getType()==Material.AIR) l.setZ(l.getZ()-1D);
                    else if(new Location(l.getWorld(), l.getX(), l.getY()-1, l.getZ()).getBlock().getType()==Material.AIR) l.setY(l.getY()-1D);

                    //Dropear item
                    //for(ItemStack item: e.getBlock().getDrops(ply.getItemInHand()/*getInventory().getItemInMainHand()*/)) e.getBlock().getWorld().dropItemNaturally(l, item);

                    //Reducir durabilidad
                    /*if(ply.getItemInHand().getType().getMaxDurability()>0) {
                        Random randomGenerator = new Random();
                        int randomInt = randomGenerator.nextInt(100) + 1;
                        int num = 1;
                        if (ply.getItemInHand().containsEnchantment(Enchantment.DURABILITY))
                            num += ply.getItemInHand().getEnchantmentLevel(Enchantment.DURABILITY);
                        int prob = (int) 100 / num;
                        if (prob >= randomInt) {
                            if (ply.getItemInHand().getType().getMaxDurability() <= ply.getItemInHand().getDurability())
                                ply.setItemInHand(null);
                            else ply.getItemInHand().setDurability((short) (ply.getItemInHand().getDurability() + 1));
                        }
                    }*/

                    for(int x = 0; x < m.stages.length; x++) {
                        if(!e.getBlock().getType().toString().equalsIgnoreCase(m.stages[x])) continue;

                        if(x<=1) break;

                        if(MineIt.instance.limit) {
                            m.stageBlocks[x]--;
                            m.stageBlocks[m.stageGo[x-2]]++;
                        }
                        establecer(e.getBlock(),Material.getMaterial(m.stages[m.stageGo[x-2]]));
                        //e.getBlock().setType(Material.getMaterial(m.stages[m.stageGo[x-2]]));
                        return;
                    }

                    establecer(e.getBlock(),Material.getMaterial(m.stages[0]));
                    //e.getBlock().setType(Material.getMaterial(m.stages[0]));
                    return;
                }
            }
        }
    }

    public void establecer(Block b,Material material){
        Bukkit.getScheduler().runTaskLater(MineIt.instance,()->b.setType(material),1);
    }
}
