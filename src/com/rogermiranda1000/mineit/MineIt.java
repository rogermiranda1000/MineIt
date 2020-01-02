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
import java.util.stream.IntStream;

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

    public List<Mines> minas = new ArrayList<Mines>();
    public HashMap<String, Location[]> bloques = new HashMap<>();
    public String version = "";

    public int rango;
    public int delay;
    public boolean limit;
    public boolean start;

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
        limit = config.getBoolean("limit_blocks_per_stage");
        start = config.getBoolean("enabled_mine_on_create");

        //Minas
        for(File archivo: getDataFolder().listFiles()) {
            if(archivo.getName().equalsIgnoreCase("config.yml")) continue;

            try {
                getLogger().info("Loading mine "+archivo.getName().replace(".yml","")+"...");
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(archivo)));
                String l;
                Mines mina = new Mines();
                l = br.readLine();
                String[] args2 = l.split(";");
                l = br.readLine();

                //Last file version 0.2-0.1?
                if(args2[0].equalsIgnoreCase(archivo.getName().replace(".yml",""))) {
                    args2[0] = "true";
                }

                if(args2.length<3) continue;
                if(limit && args2.length!=4) {
                    String s = "";
                    for(int y = 0; y<args2[1].split(",").length; y++) s+="9999,";
                    args2 = new String[]{args2[0], args2[1], args2[2], s.substring(0, s.length()-1)};
                }
                mina.name = archivo.getName().replace(".yml","");
                mina.start = Boolean.valueOf(args2[0]);
                List<String> stages = new ArrayList<>();
                for(String s: args2[1].split(",")) stages.add(s);
                mina.stages = stages.toArray(new String[stages.size()]);
                String world = args2[2];
                mina.stageGo = IntStream.range(1, mina.stages.length-1).toArray();
                //Last file version 0.5-0.1?
                if(l.contains(">")) {
                    for (int x = 0; x<l.split(">").length; x++) mina.stageGo[x] = Integer.valueOf(l.split(">")[x]);
                }
                else if(!l.contains(",")) {
                    mina.stageGo[0] = Integer.valueOf(l);
                }
                else {
                    String[] args = l.split(",");
                    if(args.length!=3) continue;
                    mina.add(world,Double.valueOf(args[0]),Double.valueOf(args[1]),Double.valueOf(args[2]));
                }


                while ((l=br.readLine())!=null) {
                    String[] args = l.split(",");
                    if(args.length!=3) continue;
                    mina.add(world,Double.valueOf(args[0]),Double.valueOf(args[1]),Double.valueOf(args[2]));
                }
                if(limit) {
                    updateStages(mina);
                    for(int x = 0; x<mina.stageLimit.length; x++) {
                        if(args2[3].split(",").length<x+1) mina.stageLimit[x] = 0;
                        else mina.stageLimit[x] = Integer.valueOf(args2[3].split(",")[x]);
                    }
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

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                for(Mines m: minas) {
                    if(!m.start) continue;
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
                    if(fase!=-1 && m.stages.length>fase+1 && (!limit || (m.stageBlocks[fase+1]+1<=m.stageLimit[fase+1]))) {
                        if(limit) {
                            m.stageBlocks[fase]--;
                            m.stageBlocks[fase+1]++;
                        }
                        loc.getBlock().setType(Material.getMaterial(m.stages[fase+1]));
                    }
                }
            }
        },1,1);
    }

    public void onDisable() {
        getLogger().info("Plugin disabled.");

        for(Map.Entry<String, Location[]> entry : bloques.entrySet()) {
            for(Location l: entry.getValue()) l.getBlock().setType(Material.STONE);
        }

        for (Mines mina : minas) {
            try {
                File file = new File(getDataFolder(), mina.name+".yml");
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));

                String txt = String.valueOf(mina.start)+";";
                for(String st: mina.stages) txt += st+",";
                txt = txt.substring(0, txt.length()-1)+";"+mina.loc()[0].split(",")[0];
                if(limit) {
                    txt += ";";
                    for (int stL : mina.stageLimit) txt += String.valueOf(stL) + ",";
                    txt = txt.substring(0, txt.length() - 1);
                }
                bw.write(txt);
                bw.newLine();

                txt = "";
                for (int st: mina.stageGo) txt += String.valueOf(st)+">";
                bw.write(txt.substring(0, txt.length()-1));
                bw.newLine();

                for (String n : mina.loc()) {
                    String[] args = n.split(",");
                    if(args.length!=4) continue;
                    bw.write(args[1].substring(0, args[1].length()-2)+","+args[2].substring(0, args[2].length()-2)+","+args[3].substring(0, args[3].length()-2));
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
            for (Mines mina: minas) {
                if(mina.name.equalsIgnoreCase(args[1])) {
                    player.sendMessage(prefix+"There's already a mine named '"+args[1]+"'.");
                    return true;
                }
            }

            Mines m = new Mines();
            m.name = args[1];
            for(Location loc : bloques.get(player.getName())) {
                m.add(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
                loc.getBlock().setType(Material.getMaterial(m.stages[0]));
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
            Mines m = null;
            for (Mines mina: minas) {
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
                for(Mines m: minas) {
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
            for(Mines m: minas) {
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
                    if(m.stages.length<=num) {
                        player.sendMessage(prefix+"There's only "+String.valueOf(m.stages.length)+" stages!");
                        return true;
                    }

                    m.stageLimit[num] = lim;
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

    public void updateStages(Mines mina) {
        mina.stageBlocks = new int[mina.stages.length];
        for(com.rogermiranda1000.mineit.Location loc: mina.bloques) {
            Material mat = new Location(Bukkit.getWorld(loc.world),loc.x,loc.y,loc.z).getBlock().getType();
            for(int x = 0; x<mina.stages.length; x++) {
                if(mat.name().equalsIgnoreCase(mina.stages[x])) {
                    mina.stageBlocks[x]++;
                    break;
                }
            }
        }

        int[] s = new int[mina.stages.length];
        int dif = s.length-mina.stageLimit.length;
        if(dif==0) return;

        for(int x = 0; x<mina.stageLimit.length; x++) {
            if(s.length<x+1) break;
            s[x] = mina.stageLimit[x];
        }
        if(dif>0) {
            for(int x = 0; x<dif; x++) {
                s[x+mina.stageLimit.length] = 9999;
            }
        }
        mina.stageLimit = s;
    }

    public void edintingMine(Player player, Mines mine) {
        int lin = ((int) (mine.stages.length/9)) + 1;
        if(lin>2) {
            if(mine.stages.length%9>0) {
                player.sendMessage(MineIt.prefix + "You've reached the max mines stages! Please, remove some in the mine's config or delete the mine.");
                return;
            }
            lin = (int) (mine.stages.length/9);
        }
        Inventory i = Bukkit.createInventory(null, (lin*2 + 1)*9, "§cEdit mine §d"+mine.name);

        for(int x = 0; x<lin*9; x++) {
            int actualLine = ((int)(x/9))*18 + (x%9);

            if(mine.stages.length>x) {
                ItemStack block = new ItemStack(Material.getMaterial(mine.stages[x]));
                ItemMeta meta = block.getItemMeta();
                List<String> l = new ArrayList<String>();
                l.add("Stage " + String.valueOf(x + 1));
                if(MineIt.instance.limit) l.add("Limit setted to "+String.valueOf(mine.stageLimit[x])+" blocks");
                meta.setLore(l);
                block.setItemMeta(meta);
                i.setItem(actualLine, block);

                if(x>1) {
                    block = new ItemStack(Material.getMaterial(mine.stages[mine.stageGo[x-2]]));
                    meta = block.getItemMeta();
                    l = new ArrayList<String>();
                    l.add("On break, go to stage " + String.valueOf(mine.stageGo[x-2]+1));
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

    public ItemStack watch(Mines mine) {
        ItemStack clock = new ItemStack(Material.FURNACE);
        ItemMeta m = clock.getItemMeta();
        String s = org.bukkit.ChatColor.GREEN+"Start";
        if(mine.start) s = org.bukkit.ChatColor.RED+"Stop";
        m.setDisplayName(s+" mine");
        clock.setItemMeta(m);

        return clock;
    }
}
