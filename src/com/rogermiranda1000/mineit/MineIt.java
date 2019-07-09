package com.rogermiranda1000.mineit;

import com.rogermiranda1000.events.onBlockBreak;
import com.rogermiranda1000.events.onClick;
import com.rogermiranda1000.events.onInteract;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

public class MineIt extends JavaPlugin {
    public static final String clearPrefix = ChatColor.GOLD+""+ChatColor.BOLD+"[MineIt] ", prefix=clearPrefix+ChatColor.RED;
    public static ItemStack item;
    public static MineIt instance;
    public static FileConfiguration config;

    //Inv
    public static Inventory inv = Bukkit.createInventory(null, 9, "§6§lMineIt");
    public static ItemStack item2;
    public static ItemStack crear;
    public static ItemStack editar;
    public static ItemStack anvil = new ItemStack(Material.ANVIL);

    public List<Mines> minas = new ArrayList<Mines>();
    public HashMap<String, Location[]> bloques = new HashMap<>();

    public int rango;
    public int delay;

    public void onEnable() {
        getLogger().info("Plugin enabled.");

        instance = this;

        //Config
        HashMap<String,String> c = new HashMap<String, String>();
        c.put("mine_creator_range", "5");
        c.put("seconds_per_block", "80");
        config = getConfig();
        //Create/actualize config file
        try {
            if (!getDataFolder().exists()) getDataFolder().mkdirs();
            File file = new File(getDataFolder(), "config.yml");
            boolean need = false;

            if (!file.exists()) {
                getLogger().info("Creating config.yml...");
                file.createNewFile();
                need = true;
            }

            for(Map.Entry<String, String> entry : c.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if(!getConfig().isSet(key)) {
                    if(value=="true") getConfig().set(key,Boolean.valueOf(true));
                    else if(value=="false") getConfig().set(key,Boolean.valueOf(false));
                    else if(value=="5") getConfig().set(key,Integer.valueOf(5));
                    else if(value=="80") getConfig().set(key,Integer.valueOf(80));
                    else getConfig().set(key,value);
                    need = true;
                }
            }
            if(need) saveConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
        rango = config.getInt("mine_creator_range");
        delay = config.getInt("seconds_per_block");

        //Minas
        for(File archivo: getDataFolder().listFiles()) {
            if(archivo.getName().equalsIgnoreCase("config.yml")) continue;

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(archivo)));
                String l;
                Mines mina = new Mines();
                l = br.readLine();
                String[] args2 = l.split(";");
                if(args2.length!=3) continue;
                mina.name = args2[0].replace(".yml","");
                List<String> stages = new ArrayList<>();
                for(String s: args2[1].split(",")) stages.add(s);
                mina.stages = stages.toArray(new String[stages.size()]);
                String world = args2[2];
                while ((l=br.readLine())!=null) {
                    String[] args = l.split(",");
                    if(args.length!=3) continue;
                    mina.add(world,Double.valueOf(args[0]),Double.valueOf(args[1]),Double.valueOf(args[2]));
                }
                minas.add(mina);
                br.close();
            } catch (Exception e) { e.printStackTrace(); }
        }

        //Crear herramienta
        item = new ItemStack(Material.STICK);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(ChatColor.GOLD+""+ChatColor.BOLD+"Mine creator");
        item.setItemMeta(m);
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
        m = anvil.getItemMeta();
        m.setDisplayName(ChatColor.GREEN+"Go back");
        anvil.setItemMeta(m);

        //Inv
        item2 = item.clone();
        ItemMeta meta = item2.getItemMeta();
        List<String> l = new ArrayList<String>();
        l.add("Get the Mine creator");
        meta.setLore(l);
        item2.setItemMeta(meta);
        inv.setItem(0, item2);
        crear = new ItemStack(Material.DIAMOND_ORE);
        meta = crear.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN+"Create mine");
        l.clear();
        l.add("Create a new mine");
        meta.setLore(l);
        crear.setItemMeta(meta);
        inv.setItem(4, crear);
        editar = new ItemStack(Material.COMPASS);
        meta = editar.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN+"Edit mine");
        l.clear();
        l.add("Edit current mines");
        meta.setLore(l);
        editar.setItemMeta(meta);
        inv.setItem(8, editar);

        getServer().getPluginManager().registerEvents(new onBlockBreak(), this);
        getServer().getPluginManager().registerEvents(new onInteract(), this);
        getServer().getPluginManager().registerEvents(new onClick(), this);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                for(Mines m: minas) {
                    m.currentTime++;
                    if(m.currentTime<(double)(delay*20D)/m.bloques.size()) continue;

                    m.currentTime=0;
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
        },1,1);
    }

    public void onDisable() {
        getLogger().info("Plugin disabled.");

        for (Mines mina : minas) {
            try {
                File file = new File(getDataFolder(), mina.name+".yml");
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));

                String txt = mina.name+";";
                for(String st: mina.stages) txt += st+",";
                bw.write(txt.substring(0, txt.length()-1)+";"+mina.loc()[0].split(",")[0]);
                bw.newLine();

                for (String n : mina.loc()) {
                    String[] args = n.split(",");
                    if(args.length!=4) continue;
                    bw.write(args[1]+","+args[2]+","+args[3]);
                    bw.newLine();
                }
                bw.flush();
                bw.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        if (cmd.getName().equalsIgnoreCase("mineit")) {
            if (player == null) {
                sender.sendMessage("Don't use this command in console.");
                return true;
            }
            if(!player.hasPermission("mineit.open")) {
                player.sendMessage(MineIt.prefix + "You don't have the permissions to do that.");
                return true;
            }

            if(args.length == 0) {
                player.openInventory(MineIt.inv);
                /*getLogger().info("Giving Mine creator to "+player.getName()+"...");
                player.getInventory().addItem(item);*/
                return true;
            }

            if(args[0].equalsIgnoreCase("create")) {
                if(!bloques.containsKey(player.getName()) || bloques.get(player.getName()).length==0) {
                    player.sendMessage(prefix+"Please, select the mine's blocks first.");
                    return true;
                }
                if(args.length!=2) {
                    player.sendMessage(prefix+"Command error, use /mineit create [name].");
                    return true;
                }
                if(new File(getDataFolder(), args[1]+".yml").exists()) {
                    player.sendMessage(prefix+"There's already a mine named '"+args[1]+"'.");
                    return true;
                }

                Mines m = new Mines();
                m.name = args[1];
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
