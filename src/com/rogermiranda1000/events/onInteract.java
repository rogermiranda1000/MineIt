package com.rogermiranda1000.events;

import com.rogermiranda1000.mineit.MineIt;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class onInteract implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        Player ply = e.getPlayer();
        if(e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(e.getClickedBlock().getType()!=Material.STONE) return;
        if(e.isCancelled()) return;
        if(!ply.getInventory().getItemInMainHand().equals(MineIt.item) && !ply.getInventory().getItemInOffHand().equals(MineIt.item)) return;
        e.setCancelled(true);

        if(!ply.hasPermission("mineit.create")) {
            ply.sendMessage(MineIt.prefix + "You don't have the permissions to do that.");
            return;
        }

        List<Location> b = new ArrayList<>();
        if(MineIt.instance.bloques.containsKey(ply.getName())) b.addAll(Arrays.asList(MineIt.instance.bloques.get(ply.getName())));
        Location[] nb = newBlock(e.getClickedBlock(), 0);
        if(nb==null) return;
        b.addAll(Arrays.asList(nb));
        MineIt.instance.bloques.put(ply.getName(), b.toArray(new Location[b.size()]));

        //ply.sendMessage(">"+String.valueOf(bloques.get(ply.getName())));
        for(Location l : b/*MineIt.instance.bloques.get(ply.getName())*/) l.getBlock().setType(Material.EMERALD_BLOCK);
    }

    Location[] newBlock(Block blk, int current) {
        List<Location> b = new ArrayList<>();
        if(current>=MineIt.instance.rango) return null;
        if(blk.getType() != Material.STONE) return null;

        Location l = blk.getLocation();
        if (air(l) && !b.contains(l)) b.add(l);

        for(int x = -1; x<2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {
                    if(x==0 && y==0 && z==0) continue;
                    Block bloque = new Location(l.getWorld(), l.getX() + Long.valueOf(x), l.getY() + Long.valueOf(y), l.getZ() + Long.valueOf(z)).getBlock();
                    if (bloque.getType() != Material.STONE) continue;

                    Location[] temp = newBlock(bloque, current + 1);
                    if(temp==null) continue;
                    for(Location s: temp) {
                        if(!air(s) || b.contains(s)) continue;
                        b.add(s);
                    }
                }
            }
        }

        //MineIt.instance.getLogger().info(b.toString());
        if(b.size()==0) return null;
        return b.toArray(new Location[b.size()]);
    }

    boolean air(Location l) {
        if(new Location(l.getWorld(), l.getX()+1, l.getY(), l.getZ()).getBlock().getType()==Material.AIR ||
                new Location(l.getWorld(), l.getX()-1, l.getY(), l.getZ()).getBlock().getType()==Material.AIR ||
                new Location(l.getWorld(), l.getX(), l.getY()+1, l.getZ()).getBlock().getType()==Material.AIR ||
                new Location(l.getWorld(), l.getX(), l.getY()-1, l.getZ()).getBlock().getType()==Material.AIR ||
                new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()+1).getBlock().getType()==Material.AIR ||
                new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()-1).getBlock().getType()==Material.AIR) return true;
        return false;
    }
}
