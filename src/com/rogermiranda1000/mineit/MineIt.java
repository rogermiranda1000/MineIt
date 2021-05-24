package com.rogermiranda1000.mineit;

import com.rogermiranda1000.mineit.events.onBlockBreak;
import com.rogermiranda1000.mineit.events.onClick;
import com.rogermiranda1000.mineit.events.onInteract;
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
    public static final String clearPrefix = ChatColor.GOLD+""+ChatColor.BOLD+"[MineIt] "+ChatColor.GREEN, prefix=clearPrefix+ChatColor.RED;
    public static ItemStack item;
    public static MineIt instance;
    public static FileConfiguration config;

    //Inv
    public static Inventory inv = Bukkit.createInventory(null, 9, "§6§lMineIt");
    public static ItemStack item2;
    public static ItemStack crear;
    public static ItemStack editar;
    public static ItemStack anvil = new ItemStack(Material.ANVIL);
    public static ItemStack redstone = new ItemStack(Material.REDSTONE_BLOCK);

    public List<Mine> minas = new ArrayList<Mine>();
    public HashMap<String, Location[]> bloques = new HashMap<>();
    public String version = "";

    public int rango;
    public int delay;
    public boolean limit;
    public boolean start;

    @Override
    public void onEnable() {
        getLogger().info("Plugin enabled.");
        version = Bukkit.getBukkitVersion();
        if(version.charAt(3)=='.') version = version.substring(0, 3);
        else version = version.substring(0,4);

        getLogger().info("Running in "+version);

        instance = this;

        //Config
        HashMap<String,String> c = new HashMap<String, String>();
        c.put("mine_creator_range", "5");
        c.put("seconds_per_block", "80");
        c.put("limit_blocks_per_stage", "false");
        c.put("enabled_mine_on_create", "false");
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
        Mine.setMineDelay(this.delay);
        limit = config.getBoolean("limit_blocks_per_stage");
        start = config.getBoolean("enabled_mine_on_create");

        //Minas
        for(File archivo: getDataFolder().listFiles()) {
            if(archivo.getName().equalsIgnoreCase("config.yml")) continue;

            try {
                getLogger().info("Loading mine " + archivo.getName().replace(".yml", "") + "...");
                minas.add(FileManager.loadMines(archivo));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
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
        m = redstone.getItemMeta();
        m.setDisplayName(ChatColor.RED+"Remove mine");
        redstone.setItemMeta(m);

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

        for(Mine mina: this.minas) Bukkit.getScheduler().scheduleSyncRepeatingTask(this, mina, 1, 1);
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled.");

        for(Map.Entry<String, Location[]> entry : bloques.entrySet()) {
            for(Location l: entry.getValue()) l.getBlock().setType(Material.STONE);
        }

        for (Mine mina : minas) {
            try {
                File file = new File(getDataFolder(), mina.name+".yml");
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
                player.sendMessage(MineIt.prefix + "You don't have the permissions to do that.");
                return true;
            }
            player.openInventory(MineIt.inv);
            return true;
        }

        if(args[0].equalsIgnoreCase("?")) {
            player.sendMessage(ChatColor.GOLD+"--Mine It--");
            player.sendMessage(ChatColor.GOLD+"/mineit create [name]");
            player.sendMessage(ChatColor.GOLD+"/mineit remove [name]");
            player.sendMessage(ChatColor.GOLD+"/mineit edit mine [name]");
            player.sendMessage(ChatColor.GOLD+"/mineit edit stagelimit [name] [stage number] [limit blocks number]");
            return true;
        }
        if(args[0].equalsIgnoreCase("create")) {
            if(!player.hasPermission("mineit.create")) {
                player.sendMessage(MineIt.prefix + "You don't have the permissions to do that.");
                return true;
            }
            if(!bloques.containsKey(player.getName()) || bloques.get(player.getName()).length==0) {
                player.sendMessage(prefix+"Please, select the mine's blocks first.");
                return true;
            }
            if(args.length!=2) {
                player.sendMessage(prefix+"Command error, use /mineit create [name].");
                return true;
            }
            if(minas.size()>=45) {
                player.sendMessage(prefix+"You've reached the current mines limit!");
                return true;
            }
            for (Mine mina: minas) {
                if(mina.name.equalsIgnoreCase(args[1])) {
                    player.sendMessage(prefix+"There's already a mine named '"+args[1]+"'.");
                    return true;
                }
            }

            Mine m = new Mine();
            m.name = args[1];
            for(Location loc : bloques.get(player.getName())) {
                m.add(loc);
                loc.getBlock().setType(m.getStages().get(0).getStageMaterial());
            }
            if(limit) updateStages(m);
            m.start = start;
            minas.add(m);
            bloques.remove(player.getName());

            player.sendMessage(clearPrefix+ChatColor.GREEN+"Mine created successfully.");
            return true;
        }
        if(args[0].equalsIgnoreCase("remove")) {
            if(!player.hasPermission("mineit.remove")) {
                player.sendMessage(MineIt.prefix + "You don't have the permissions to do that.");
                return true;
            }
            if(args.length!=2) {
                player.sendMessage(prefix+"Command error, use /mineit create [name].");
                return true;
            }
            Mine m = null;
            for (Mine mina: minas) {
                if(mina.name.equalsIgnoreCase(args[1])) m = mina;
            }
            if(m==null) {
                player.sendMessage(prefix+"The mine '"+args[1]+"' doesn't exist.");
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
        if(args[0].equalsIgnoreCase("edit")) {
            if(args[1].equalsIgnoreCase("mine")) {
                if(args.length!=3) {
                    player.sendMessage(prefix+"Use /mineit edit mine [name]");
                    return true;
                }
                if(!player.hasPermission("mineit.open")) {
                    player.closeInventory();
                    player.sendMessage(prefix+"You can't use mine's menus.");
                    return true;
                }
                for(Mine m: minas) {
                    if(m.name.equalsIgnoreCase(args[2])) {
                        edintingMine(player, m);
                        return true;
                    }
                }

                player.sendMessage(MineIt.prefix+"Mine '"+args[2]+"' not found.");
                return true;
            }

            if(!player.hasPermission("mineit.stagelimit")) {
                player.sendMessage(MineIt.prefix + "You don't have the permissions to do that.");
                return true;
            }
            if(args.length!=5 || !args[1].equalsIgnoreCase("stagelimit")) {
                player.sendMessage(prefix+"Command error, use /mineit edit stagelimit [name] [stage number] [limit blocks number].");
                player.sendMessage(clearPrefix+"Ex. /mineit edit stagelimit Gold 2 30");
                return true;
            }
            for(Mine m: minas) {
                if(m.name.equalsIgnoreCase(args[2])) {
                    int num = -1;
                    int lim = -1;
                    try {
                        num = Integer.valueOf(args[3])-1;
                        if(num<=0) {
                            player.sendMessage(prefix+"The stage number can't be lower to 1.");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(prefix+"'"+args[3]+"' is not a number!");
                        return true;
                    }
                    try {
                        lim = Integer.valueOf(args[4]);
                        if(lim<0) {
                            player.sendMessage(prefix+"The limit number can't be lower to 0.");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(prefix+"'"+args[4]+"' is not a number!");
                        return true;
                    }
                    if(m.getStages().size()<=num) {
                        player.sendMessage(prefix+"There's only " + m.getStages().size() + " stages!");
                        return true;
                    }

                    m.getStages().get(num).setStageLimit(lim);
                    player.sendMessage(clearPrefix+"Set "+args[2]+"'s stage "+args[3]+" limit to "+args[4]+".");
                    return true;
                }
            }

            player.sendMessage(prefix+"Mine '"+args[2]+"' not found.");
            return true;
        }

        player.sendMessage(MineIt.prefix+"Use "+ChatColor.GOLD+"/mineit ?"+ChatColor.RED+".");
        return true;
    }

    /**
     * Recalculates the number of blocks for each stage in the mine
     * @param mina Mine to recalculate the blocks
     */
    public void updateStages(Mine mina) {
        mina.resetStagesCount();

        for(Location loc: mina.getMineBlocks()) {
            Material mat = loc.getBlock().getType();
            Stage match = Stage.getMatch(mina.getStages(), mat.name());
            if (match != null) match.incrementStageBlocks();
        }
    }

    public void edintingMine(Player player, Mine mine) {
        int lin = mine.getStages().size()/9 + 1;
        if(lin>2) {
            if(mine.getStages().size() % 9 > 0) {
                player.sendMessage(MineIt.prefix + "You've reached the max mines stages! Please, remove some in the mine's config or delete the mine.");
                return;
            }
            lin = mine.getStages().size()/9;
        }
        Inventory i = Bukkit.createInventory(null, (lin*2 + 1)*9, "§cEdit mine §d"+mine.name);

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
                ItemStack gls = new ItemStack(Material.GLASS);
                ItemMeta meta = gls.getItemMeta();
                meta.setDisplayName("-");
                gls.setItemMeta(meta);
                i.setItem(actualLine, gls);
                i.setItem(actualLine+9, gls);
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
        if(mine.start) s = org.bukkit.ChatColor.RED+"Stop";
        m.setDisplayName(s+" mine");
        clock.setItemMeta(m);

        return clock;
    }
}
