package com.rogermiranda1000.mineit.events;

import com.rogermiranda1000.mineit.Mine;
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

import java.util.ArrayList;

public class InteractEvent implements Listener {
    private static final Material SELECTED_BLOCK = Material.EMERALD_BLOCK;

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        Player ply = e.getPlayer();
        if(e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(e.isCancelled()) return;
        //if(MineIt.instance.version=="1.8") {
            if(!ply.getItemInHand().equals(MineIt.item)) return;
        /*}
        else if(!ply.getInventory().getItemInMainHand().equals(MineIt.item) && !ply.getInventory().getItemInOffHand().equals(MineIt.item)) return;*/
        e.setCancelled(true);
        if(e.getClickedBlock().getType()!=Material.STONE) {
            ply.sendMessage(MineIt.errorPrefix+"You can only hit stone with the Mine Creator!");
            return;
        }

        if(!ply.hasPermission("mineit.create")) {
            ply.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
            return;
        }

        ArrayList<Location> b = MineIt.instance.selectedBlocks.get(ply.getName());
        if (b == null) {
            b = new ArrayList<>();
            MineIt.instance.selectedBlocks.put(ply.getName(), b);
        }
        b.addAll(InteractEvent.getSurroundingBlocks(e.getClickedBlock().getLocation()));
    }


    /**
     * Given a location it explores the surrounding blocks until the expansion area it's bigger than 'MineIt.instance.rango'
     * It also converts the matching blocks into 'InteractEvent.SELECTED_BLOCK' to avoid recursion
     * @param loc Initial block
     * @return All the converted blocks
     */
    private static ArrayList<Location> getSurroundingBlocks(Location loc) {
        try {
            return InteractEvent.getSurroundingBlocks(loc, loc);
        } catch (StackOverflowError err) {
            MineIt.instance.printConsoleErrorMessage("Reduce the mine_creator_range value on the config.yml!");
            return new ArrayList<>();
        }
    }

    private static ArrayList<Location> getSurroundingBlocks(Location loc, Location origin) {
        ArrayList<Location> b = new ArrayList<>();
        if(loc.getBlock().getType() != Material.STONE) return b;
        if(InteractEvent.isDistanceGreater(origin, loc, MineIt.instance.rango)) return b;

        if (!InteractEvent.airNear(loc)) return b;
        b.add(loc);
        loc.getBlock().setType(InteractEvent.SELECTED_BLOCK); // convert block visually

        for(int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if(x==0 && y==0 && z==0) continue; // same block
                    Location next = new Location(loc.getWorld(), loc.getX() + x, loc.getY() + y, loc.getZ() + z);
                    b.addAll(getSurroundingBlocks(next, origin));
                }
            }
        }

        return b;
    }

    private static boolean isDistanceGreater(Location l1, Location l2, int threshold) {
        return (Math.pow(l1.getX() - l2.getX(), 2) + Math.pow(l1.getY() - l2.getY(), 2) + Math.pow(l1.getZ() - l2.getZ(), 2) > Math.pow(threshold, 2));
    }

    private static boolean airNear(Location l) {
        return (new Location(l.getWorld(), l.getX()+1, l.getY(), l.getZ()).getBlock().getType()==Material.AIR ||
                new Location(l.getWorld(), l.getX()-1, l.getY(), l.getZ()).getBlock().getType()==Material.AIR ||
                new Location(l.getWorld(), l.getX(), l.getY()+1, l.getZ()).getBlock().getType()==Material.AIR ||
                new Location(l.getWorld(), l.getX(), l.getY()-1, l.getZ()).getBlock().getType()==Material.AIR ||
                new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()+1).getBlock().getType()==Material.AIR ||
                new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()-1).getBlock().getType()==Material.AIR);
    }
}
