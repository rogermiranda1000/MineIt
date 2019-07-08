package com.rogermiranda1000.mineit;

import com.rogermiranda1000.events.onBlockBreak;
import com.rogermiranda1000.events.onInteract;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MineIt extends JavaPlugin {
    public static final String clearPrefix = ChatColor.GOLD+""+ChatColor.BOLD+"[MineIt] ", prefix=clearPrefix+ChatColor.RED;
    public static ItemStack item;
    public static MineIt instance;

    public List<Mines> minas = new ArrayList<Mines>();
    public HashMap<String, Location[]> bloques = new HashMap<>();

    public int rango = 5;
    public int delay = 70;

    public void onEnable() {
        getLogger().info("Plugin enabled.");

        instance = this;

        //Crear herramienta
        item = new ItemStack(Material.STICK);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(ChatColor.GOLD+""+ChatColor.BOLD+"Mine creator");
        item.setItemMeta(m);
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        getServer().getPluginManager().registerEvents(new onBlockBreak(), this);
        getServer().getPluginManager().registerEvents(new onInteract(), this);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                for(Mines m: minas) {
                    Random r = new Random();
                    String l = m.loc()[r.nextInt(m.loc().length)];
                    Location loc = new Location(Bukkit.getWorld(l.split(",")[0]),Double.valueOf(l.split(",")[1]),
                            Double.valueOf(l.split(",")[2]),Double.valueOf(l.split(",")[3]));
                    int fase = -1;
                    for(int x = 0; x<m.stages.length; x++) {
                        if(m.stages[x].equalsIgnoreCase(loc.getBlock().getType().toString())) {
                            fase = x;
                            break;
                        }
                    }
                    if(fase!=-1 && m.stages.length>fase+1) loc.getBlock().setType(Material.getMaterial(m.stages[fase+1]));
                }
            }
        },delay,delay);
    }

    public void onDisable() {
        getLogger().info("Plugin disabled.");
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        if (cmd.getName().equalsIgnoreCase("mineit")) {
            if (player == null) {
                sender.sendMessage("Don't use this command in console.");
                return true;
            }
            if(!player.hasPermission("mineit.create")) {
                player.sendMessage(MineIt.prefix + "You don't have the permissions to do that.");
                return true;
            }

            if(args.length == 0) {
                getLogger().info("Giving Mine creator to "+player.getName()+"...");
                player.getInventory().addItem(item);
                return true;
            }

            if(args[0].equalsIgnoreCase("create")) {
                if(!bloques.containsKey(player.getName()) || bloques.get(player.getName()).length==0) {
                    player.sendMessage(prefix+"Please, select the mine's blocks first.");
                    return true;
                }

                Mines m = new Mines();
                for(Location loc : bloques.get(player.getName())) {
                    m.add(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
                    loc.getBlock().setType(Material.getMaterial(m.stages[0]));
                }
                minas.add(m);
                bloques.remove(player.getName());

                player.sendMessage(clearPrefix+ChatColor.GREEN+"Mine created successfully.");
                return true;
            }

            //player.sendMessage("Under consatruction...");
            return true;
        }
        return false;
    }


}
