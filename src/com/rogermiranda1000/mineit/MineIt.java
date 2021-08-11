package com.rogermiranda1000.mineit;

import com.google.gson.JsonSyntaxException;
import com.rogermiranda1000.mineit.events.BlockBreakEvent;
import com.rogermiranda1000.mineit.events.ClickEvent;
import com.rogermiranda1000.mineit.events.CommandEvent;
import com.rogermiranda1000.mineit.events.InteractEvent;
import com.rogermiranda1000.mineit.file.FileManager;
import com.rogermiranda1000.mineit.file.InvalidLocationException;
import com.rogermiranda1000.versioncontroller.VersionChecker;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
    public static final String PLUGIN_ID = "69161";
    private static final String ERROR_COLOR = Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString(),
            WARNING_COLOR = Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString(),
            NO_COLOR = Ansi.ansi().fg(Ansi.Color.WHITE).boldOff().toString();
    public static final String clearPrefix = ChatColor.GOLD.toString() + ChatColor.BOLD + "[MineIt] " + ChatColor.GREEN,
            errorPrefix = ChatColor.GOLD.toString() + ChatColor.BOLD + "[MineIt] " + ChatColor.RED;
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

    public HashMap<String, ArrayList<Location>> selectedBlocks = new HashMap<>();

    public int rango;
    public int delay;
    public boolean limit;

    public void printConsoleErrorMessage(String msg) {
        this.getLogger().warning(MineIt.ERROR_COLOR + msg + MineIt.NO_COLOR);
    }

    public void printConsoleWarningMessage(String msg) {
        this.getLogger().info(MineIt.WARNING_COLOR + msg + MineIt.NO_COLOR);
    }

    @Override
    public void onEnable() {
        instance = this;

        Bukkit.getScheduler().runTaskAsynchronously(this,()->{
            try {
                String version = VersionChecker.getVersion(MineIt.PLUGIN_ID);
                if (VersionChecker.isLower(this.getDescription().getVersion(), version)) this.printConsoleWarningMessage("v" + version + " is now available! You should consider updating the plugin.");
            } catch (IOException e) {
                this.printConsoleWarningMessage("Can't check for updates.");
            }
        });

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
                Mine mine = FileManager.loadMines(archivo);
                Mine.addMine(mine);
            } catch (IOException | JsonSyntaxException ex) {
                this.printConsoleErrorMessage( "Invalid file format, the mine '" + mineName + "' can't be loaded. If you have updated the plugin delete the file and create the mine again.");
            } catch (InvalidLocationException ex) {
                this.printConsoleErrorMessage( "Error, the mine '" + mineName + "' can't be loaded. " + ex.getMessage());
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
        getCommand("mineit").setExecutor(new CommandEvent());
    }

    @Override
    public void onDisable() {
        // undo selected blocks
        for(ArrayList<Location> locations : selectedBlocks.values()) {
            for(Location l: locations) l.getBlock().setType(Material.STONE);
        }

        for (Mine mina : Mine.getMines()) {
            try {
                File file = new File(getDataFolder(), mina.mineName +".yml");
                FileManager.saveMines(file, mina);
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
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
                ItemStack block = current.getStageItemStack();
                ItemMeta meta = block.getItemMeta();
                if (meta == null) {
                    // AIR
                    block = new ItemStack(Mine.AIR_STAGE);
                    meta = block.getItemMeta();
                    meta.setDisplayName("Air");
                }
                List<String> l = new ArrayList<>();
                l.add("Stage " + (x + 1));
                if(MineIt.instance.limit) l.add("Limit setted to " + current.getStageLimit() + " blocks");
                meta.setLore(l);
                block.setItemMeta(meta);
                i.setItem(actualLine, block);

                if(current.getPreviousStage() != null) {
                    block = current.getPreviousStage().getStageItemStack();
                    meta = block.getItemMeta();
                    if (meta == null) {
                        // AIR
                        block = new ItemStack(Mine.AIR_STAGE);
                        meta = block.getItemMeta();
                        meta.setDisplayName("Air");
                    }
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
