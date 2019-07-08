package com.rogermiranda1000.eventos;

import com.rogermiranda1000.mineit.MineIt;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class onUse implements Listener {
    HashMap<String, Location[]> bloques = new HashMap<>();

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent e) {
        Player ply = e.getPlayer();
        if(e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(e.getItem()!=MineIt.item) return;

        if(!ply.hasPermission("mineit.create")) {
            ply.sendMessage(MineIt.prefix + "You don't have the permissions to do that.");
            return;
        }

        if(bloques.containsKey(ply.getName())) {
            List<Location> b = new ArrayList<>();
            b.addAll(Arrays.asList(bloques.get(ply.getName())));
            b.addAll(Arrays.asList(newBlock(e.getClickedBlock(), 0)));

            bloques.put(ply.getName(), (Location[]) b.toArray());
        }
        else bloques.put(ply.getName(), newBlock(e.getClickedBlock(), 0));

        ply.sendMessage(">"+String.valueOf(bloques.get(ply.getName())));
        for(Location l : bloques.get(ply.getName())) l.getBlock().setType(Material.EMERALD_BLOCK);

        e.setCancelled(true);
    }

    Location[] newBlock(Block blk, int current) {
        List<Location> b = new ArrayList<>();
        if(current>=40) return null;

        if(blk.getType() == Material.STONE) {
            Location l = blk.getLocation();
            for(int x = -1; x<2; x++) {
                for(int y = -1; x<2; x++) {
                    for(int z = -1; x<2; x++) {
                        Block bloque = new Location(l.getWorld(), l.getX()+x, l.getY()+y, l.getZ()+z).getBlock();
                        for(Location loc: newBlock(bloque, current+1)) {
                            if(air(loc) && !b.contains(loc)) b.add(loc);
                        }
                    }
                }
            }
        }
        return (Location[]) b.toArray();
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
