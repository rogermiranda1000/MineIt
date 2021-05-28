package com.rogermiranda1000.mineit;

import com.google.gson.JsonSyntaxException;
import com.rogermiranda1000.mineit.events.BlockBreakEvent;
import com.rogermiranda1000.mineit.events.ClickEvent;
import com.rogermiranda1000.mineit.events.InteractEvent;
import com.rogermiranda1000.mineit.file.FileManager;
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
import org.fusesource.jansi.Ansi;

import java.io.*;
import java.util.*;

public class MineIt extends JavaPlugin {
    public static final String clearPrefix = ChatColor.GOLD+""+ChatColor.BOLD+"[MineIt] "+ChatColor.GREEN, errorPrefix =clearPrefix+ChatColor.RED;
    public static ItemStack item;
    public static MineIt instance;
    public static FileConfiguration config;

    //Inv
    public static Inventory inv = Bukkit.createInventory(null, 9, "§6§lMineIt");
    public static ItemStack item2;
    public static ItemStack crear;
    public static ItemStack editar;
    public static ItemStack anvil;
    public static ItemStack redstone;
    public static ItemStack glass;

    public ArrayList<Mine> minas = new ArrayList<>();
    public HashMap<String, Location[]> bloques = new HashMap<>(); // TODO command to unselect?
    public String version = "";

    public int rango;
    public int delay;
    public boolean limit;

    public static void printConsoleErrorMessage(String msg) {
        System.out.println(Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() + "[MineIt] " + msg + Ansi.ansi().fg(Ansi.Color.WHITE).boldOff().toString());
    }

    @Override
    public void onEnable() {
        getLogger().info("Plugin enabled.");
        version = Bukkit.getBukkitVersion();
        if(version.charAt(3)=='.') version = version.substring(0, 3);
        else version = version.substring(0,4);

        //getLogger().info("Running in "+version);

        instance = this;

        //Config
        HashMap<String,Object> c = new HashMap<>();
        c.put("mine_creator_range", 5);
        c.put("seconds_per_block", 80);
        c.put("limit_blocks_per_stage", false);
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

            for(Map.Entry<String, Object> entry : c.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if(!getConfig().isSet(key)) {
                    getConfig().set(key,value);
                    need = true;
                }
            }
            if(need) saveConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
        rango = config.getInt("mine_creator_range");
        delay = config.getInt("seconds_per_block");
        Mine.setMineDelay(this.delay);
        limit = config.getBoolean("limit_blocks_per_stage");

        //Minas
        for(File archivo: getDataFolder().listFiles()) {
            if(archivo.getName().equalsIgnoreCase("config.yml")) continue;

            String mineName = archivo.getName().replaceAll("\\.yml$", "");
            try {
                getLogger().info("Loading mine " + mineName + "..."); // TODO .json
                minas.add(FileManager.loadMines(archivo));
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (JsonSyntaxException ex) {
                MineIt.printConsoleErrorMessage( "Invalid file, the mine '" + mineName + "' can't be loaded.");
            }
        }

        //Crear herramienta
        item = new ItemStack(Material.STICK);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(ChatColor.GOLD+""+ChatColor.BOLD+"Mine creator");
        item.setItemMeta(m);
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        anvil = new ItemStack(Material.ANVIL);
        m = anvil.getItemMeta();
        m.setDisplayName(ChatColor.GREEN+"Go back");
        anvil.setItemMeta(m);

        redstone = new ItemStack(Material.REDSTONE_BLOCK);
        m = redstone.getItemMeta();
        m.setDisplayName(ChatColor.RED+"Remove mine");
        redstone.setItemMeta(m);

        glass = new ItemStack(Material.GLASS);
        m = glass.getItemMeta();
        m.setDisplayName("-");
        glass.setItemMeta(m);

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

        getServer().getPluginManager().registerEvents(new BlockBreakEvent(), this);
        getServer().getPluginManager().registerEvents(new InteractEvent(), this);
        getServer().getPluginManager().registerEvents(new ClickEvent(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled.");

        for(Map.Entry<String, Location[]> entry : bloques.entrySet()) {
            for(Location l: entry.getValue()) l.getBlock().setType(Material.STONE);
        }

        for (Mine mina : minas) {
            try {
                File file = new File(getDataFolder(), mina.mineName +".yml");
                FileManager.saveMines(file, mina);
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        if (!cmd.getName().equalsIgnoreCase("mineit")) return false;
        if (player == null) {
            sender.sendMessage("Don't use this command in console.");
            return true;
        }

        if(args.length == 0) {
            if(!player.hasPermission("mineit.open")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return true;
            }
            player.openInventory(MineIt.inv);
            return true;
        }

        if(args[0].equalsIgnoreCase("?")) {
            player.sendMessage(ChatColor.GOLD+"--Mine It--");
            player.sendMessage(ChatColor.GOLD+"/mineit create [name]");
            player.sendMessage(ChatColor.GOLD+"/mineit remove [name]");
            player.sendMessage(ChatColor.GOLD+"/mineit start [name]");
            player.sendMessage(ChatColor.GOLD+"/mineit stop [name]");
            player.sendMessage(ChatColor.GOLD+"/mineit edit mine [name]");
            player.sendMessage(ChatColor.GOLD+"/mineit edit stagelimit [name] [stage number] [limit blocks number]");
            player.sendMessage(ChatColor.GOLD+"/mineit reset [name]" + ChatColor.GREEN + ": it sets all the mine's block to bedrock " + ChatColor.RED + "[if the mine it's too big it may crash your server]");
            return true;
        }
        if(args[0].equalsIgnoreCase("create")) {
            if(!player.hasPermission("mineit.create")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return true;
            }
            if(!bloques.containsKey(player.getName()) || bloques.get(player.getName()).length==0) {
                player.sendMessage(errorPrefix +"Please, select the mine's blocks first.");
                return true;
            }
            if(args.length!=2) {
                player.sendMessage(errorPrefix +"Command error, use /mineit create [name].");
                return true;
            }
            if(minas.size()>=45) {
                player.sendMessage(errorPrefix +"You've reached the current mines limit!");
                return true;
            }
            for (Mine mina: minas) {
                if(mina.mineName.equalsIgnoreCase(args[1])) {
                    player.sendMessage(errorPrefix +"There's already a mine named '"+args[1]+"'.");
                    return true;
                }
            }

            ArrayList<Location> locations = new ArrayList<>();
            for(Location loc : bloques.get(player.getName())) {
                locations.add(loc);
                loc.getBlock().setType(Mine.STATE_ZERO);
            }
            Mine m = new Mine(args[1], locations);
            if(limit) m.updateStages();
            minas.add(m);
            bloques.remove(player.getName());

            player.sendMessage(clearPrefix+ChatColor.GREEN+"Mine created successfully.");
            player.sendMessage(clearPrefix+ChatColor.RED+"The mine it's stopped. Configure it with " + ChatColor.GREEN + "/mineit edit mine " + args[1] + ChatColor.RED + " and then enable it with " + ChatColor.GREEN + "/mineit enable " + args[1]);
            return true;
        }
        if(args[0].equalsIgnoreCase("remove")) {
            if(!player.hasPermission("mineit.remove")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return true;
            }
            if(args.length!=2) {
                player.sendMessage(errorPrefix +"Command error, use /mineit create [name].");
                return true;
            }
            Mine m = null;
            for (Mine mina: minas) {
                if(mina.mineName.equalsIgnoreCase(args[1])) m = mina;
            }
            if(m==null) {
                player.sendMessage(errorPrefix +"The mine '"+args[1]+"' doesn't exist.");
                return true;
            }

            minas.remove(m);
            try {
                File f = new File(getDataFolder(), args[1] + ".yml");
                if (f.exists()) f.delete();
            }
            catch (Exception e) { e.printStackTrace(); }
            player.sendMessage(clearPrefix+"Mine '"+args[1]+"' removed.");
            return true;
        }
        if (args[0].equalsIgnoreCase("start")) {
            if(!player.hasPermission("mineit.start")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return true;
            }
            if(args.length!=2) {
                player.sendMessage(errorPrefix +"Command error, use /mineit start [name].");
                return true;
            }
            Mine m = null;
            for (Mine mina: minas) {
                if(mina.mineName.equalsIgnoreCase(args[1])) m = mina;
            }
            if(m==null) {
                player.sendMessage(errorPrefix +"The mine '"+args[1]+"' doesn't exist.");
                return true;
            }

            m.setStart(true);
            player.sendMessage(clearPrefix+"Mine "+args[1]+" started.");
            return true;
        }
        if (args[0].equalsIgnoreCase("stop")) {
            if(!player.hasPermission("mineit.stop")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return true;
            }
            if(args.length!=2) {
                player.sendMessage(errorPrefix +"Command error, use /mineit stop [name].");
                return true;
            }
            Mine m = null;
            for (Mine mina: minas) {
                if(mina.mineName.equalsIgnoreCase(args[1])) m = mina;
            }
            if(m==null) {
                player.sendMessage(errorPrefix +"The mine '"+args[1]+"' doesn't exist.");
                return true;
            }

            m.setStart(false);
            player.sendMessage(clearPrefix+"Mine "+args[1]+" stopped.");
            return true;
        }
        if(args[0].equalsIgnoreCase("edit")) {
            if (args.length == 1) {
                player.sendMessage(errorPrefix +"Invalid syntax, use /mineit ?");
                return true;
            }

            if(args[1].equalsIgnoreCase("mine")) {
                if(args.length!=3) {
                    player.sendMessage(errorPrefix +"Use /mineit edit mine [name]");
                    return true;
                }
                if(!player.hasPermission("mineit.open")) {
                    player.closeInventory();
                    player.sendMessage(errorPrefix +"You can't use MineIt menus.");
                    return true;
                }
                for(Mine m: minas) {
                    if(m.mineName.equalsIgnoreCase(args[2])) {
                        edintingMine(player, m);
                        return true;
                    }
                }

                player.sendMessage(MineIt.errorPrefix +"Mine '"+args[2]+"' not found.");
                return true;
            }

            if(!player.hasPermission("mineit.stagelimit")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return true;
            }
            if(args.length!=5 || !args[1].equalsIgnoreCase("stagelimit")) {
                player.sendMessage(errorPrefix +"Command error, use /mineit edit stagelimit [name] [stage number] [limit blocks number].");
                player.sendMessage(clearPrefix+"Ex. /mineit edit stagelimit Gold 2 30");
                return true;
            }
            for(Mine m: minas) {
                if(m.mineName.equalsIgnoreCase(args[2])) {
                    int num = -1;
                    int lim = -1;
                    try {
                        num = Integer.valueOf(args[3])-1;
                        if(num<=0) {
                            player.sendMessage(errorPrefix +"The stage number can't be lower to 1.");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(errorPrefix +"'"+args[3]+"' is not a number!");
                        return true;
                    }
                    try {
                        lim = Integer.valueOf(args[4]);
                        if(lim<0) {
                            player.sendMessage(errorPrefix +"The limit number can't be lower to 0.");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(errorPrefix +"'"+args[4]+"' is not a number!");
                        return true;
                    }
                    if(m.getStages().size()<=num) {
                        player.sendMessage(errorPrefix +"There's only " + m.getStages().size() + " stages!");
                        return true;
                    }

                    m.getStages().get(num).setStageLimit(lim);
                    player.sendMessage(clearPrefix+"Set "+args[2]+"'s stage "+args[3]+" limit to "+args[4]+".");
                    return true;
                }
            }

            player.sendMessage(errorPrefix +"Mine '"+args[2]+"' not found.");
            return true;
        }
        if(args[0].equalsIgnoreCase("reset")) {
            if(args.length!=2) {
                player.sendMessage(errorPrefix +"Use /mineit reset [name]");
                return true;
            }
            if(!player.hasPermission("mineit.reset")) {
                player.sendMessage(errorPrefix +"You can't reset mines!");
                return true;
            }

            for(Mine m: minas) {
                if(m.mineName.equalsIgnoreCase(args[1])) {
                    m.resetBlocksMine();
                    return true;
                }
            }

            player.sendMessage(MineIt.errorPrefix +"Mine '"+args[1]+"' not found.");
            return true;
        }

        player.sendMessage(MineIt.errorPrefix +"Use "+ChatColor.GOLD+"/mineit ?"+ChatColor.RED+".");
        return true;
    }

    public void edintingMine(Player player, Mine mine) {
        int lin = mine.getStages().size()/9 + 1;
        if(lin>2) {
            if(mine.getStages().size() % 9 > 0) {
                player.sendMessage(MineIt.errorPrefix + "You've reached the max mines stages! Please, remove some in the mine's config or delete the mine.");
                return;
            }
            lin = mine.getStages().size()/9;
        }
        Inventory i = Bukkit.createInventory(null, (lin*2 + 1)*9, "§cEdit mine §d"+mine.mineName);

        for(int x = 0; x<lin*9; x++) {
            int actualLine = (x/9)*18 + (x%9);

            if(mine.getStages().size()>x) {
                Stage current = mine.getStages().get(x);
                ItemStack block = new ItemStack(current.getStageMaterial());
                ItemMeta meta = block.getItemMeta();
                List<String> l = new ArrayList<>();
                l.add("Stage " + (x + 1));
                if(MineIt.instance.limit) l.add("Limit setted to " + current.getStageLimit() + " blocks");
                meta.setLore(l);
                block.setItemMeta(meta);
                i.setItem(actualLine, block);

                if(current.getPreviousStage() != null) {
                    block = new ItemStack(current.getPreviousStage().getStageMaterial());
                    meta = block.getItemMeta();
                    l = new ArrayList<>();
                    l.add("On break, go to stage " + current.getPreviousStage().getName());
                    meta.setLore(l);
                    block.setItemMeta(meta);

                    i.setItem(actualLine+9, block);
                }
            }
            else {
                i.setItem(actualLine, glass);
                i.setItem(actualLine+9, glass);
            }
        }
        i.setItem(lin*18, MineIt.anvil);
        i.setItem(((lin*2 + 1)*9)-2, watch(mine));
        i.setItem(((lin*2 + 1)*9)-1, MineIt.redstone);
        player.openInventory(i);
    }

    public ItemStack watch(Mine mine) {
        ItemStack clock = new ItemStack(Material.FURNACE);
        ItemMeta m = clock.getItemMeta();
        String s = org.bukkit.ChatColor.GREEN+"Start";
        if(mine.getStart()) s = org.bukkit.ChatColor.RED+"Stop";
        m.setDisplayName(s+" mine");
        clock.setItemMeta(m);

        return clock;
    }
}
