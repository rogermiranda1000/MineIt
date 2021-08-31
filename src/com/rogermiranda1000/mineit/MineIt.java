package com.rogermiranda1000.mineit;

import com.google.gson.JsonSyntaxException;
import com.rogermiranda1000.mineit.events.BlockBreakEvent;
import com.rogermiranda1000.mineit.events.CommandEvent;
import com.rogermiranda1000.mineit.events.InteractEvent;
import com.rogermiranda1000.mineit.events.HintEvent;
import com.rogermiranda1000.mineit.file.FileManager;
import com.rogermiranda1000.mineit.file.InvalidLocationException;
import com.rogermiranda1000.mineit.inventories.BasicInventory;
import com.rogermiranda1000.mineit.inventories.MainInventory;
import com.rogermiranda1000.mineit.inventories.SelectMineInventory;
import com.rogermiranda1000.versioncontroller.VersionChecker;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
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
    public BasicInventory mainInventory;
    public BasicInventory selectMineInventory;

    public HashMap<String, ArrayList<Location>> selectedBlocks = new HashMap<>();

    public int rango;
    public boolean limit;

    public void printConsoleErrorMessage(String msg) {
        this.getLogger().warning(MineIt.ERROR_COLOR + msg + MineIt.NO_COLOR);
    }

    public void printConsoleWarningMessage(String msg) {
        this.getLogger().info(MineIt.WARNING_COLOR + msg + MineIt.NO_COLOR);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onEnable() {
        MineIt.instance = this;

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
        limit = config.getBoolean("limit_blocks_per_stage");

        // Create tool
        // @pre before inventory creation
        item = new ItemStack(Material.STICK);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(ChatColor.GOLD+""+ChatColor.BOLD+"Mine creator");
        item.setItemMeta(m);
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        // @pre before mine import
        this.mainInventory = new MainInventory();
        this.selectMineInventory = new SelectMineInventory();

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

        getServer().getPluginManager().registerEvents(new BlockBreakEvent(), this);
        getServer().getPluginManager().registerEvents(new InteractEvent(), this);
        getServer().getPluginManager().registerEvents(new HintEvent(), this);
        this.mainInventory.registerEvent(this);
        this.selectMineInventory.registerEvent(this);

        getCommand("mineit").setExecutor(new CommandEvent());
    }

    @Override
    public void onDisable() {
        // close inventories (if it's a reboot the players may be able to keep the items)
        this.mainInventory.closeInventories();
        this.selectMineInventory.closeInventories();
        for (BasicInventory mine : ((SelectMineInventory)this.selectMineInventory).getMinesInventories()) mine.closeInventories();

        // undo selected blocks
        for(ArrayList<Location> locations : selectedBlocks.values()) {
            for(Location l: locations) l.getBlock().setType(Material.STONE);
        }

        // save mines
        for (Mine mina : Mine.getMines()) {
            try {
                File file = new File(getDataFolder(), mina.mineName +".yml");
                FileManager.saveMine(file, mina);
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
