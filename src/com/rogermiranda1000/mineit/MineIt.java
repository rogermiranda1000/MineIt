package com.rogermiranda1000.mineit;

import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.google.gson.JsonSyntaxException;
import com.rogermiranda1000.mineit.events.BreakEvent;
import com.rogermiranda1000.mineit.events.CommandEvent;
import com.rogermiranda1000.mineit.events.InteractEvent;
import com.rogermiranda1000.mineit.events.HintEvent;
import com.rogermiranda1000.mineit.file.FileManager;
import com.rogermiranda1000.mineit.file.InvalidLocationException;
import com.rogermiranda1000.mineit.inventories.BasicInventory;
import com.rogermiranda1000.mineit.inventories.MainInventory;
import com.rogermiranda1000.mineit.inventories.SelectMineInventory;
import com.rogermiranda1000.mineit.protections.ProtectionOverrider;
import com.rogermiranda1000.mineit.protections.ResidenceProtectionOverrider;
import com.rogermiranda1000.mineit.protections.WorldGuardProtectionOverrider;
import com.rogermiranda1000.versioncontroller.Version;
import com.rogermiranda1000.versioncontroller.VersionChecker;
import com.rogermiranda1000.versioncontroller.VersionController;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    //Inv
    public BasicInventory mainInventory;
    public BasicInventory selectMineInventory;

    public ArrayList<ProtectionOverrider> protectionOverrider = new ArrayList<>();

    public HashMap<String, ArrayList<Location>> selectedBlocks = new HashMap<>();

    public int rango;
    public boolean limit;
    public boolean overrideProtection;

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
        c.put("air_stage", Material.STONE_BUTTON.name());
        c.put("override_protections", true);
        FileConfiguration config = getConfig();
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
        this.rango = config.getInt("mine_creator_range");
        this.limit = config.getBoolean("limit_blocks_per_stage");
        this.overrideProtection = config.getBoolean("override_protections");
        String airStage = config.getString("air_stage");
        try {
            Mine.AIR_STAGE = Material.getMaterial(airStage);
        } catch (ClassCastException ex) {
            this.printConsoleErrorMessage("The air stage material '" + airStage + "' does not exist!");
        }


        // Protections
        PluginManager pm = getServer().getPluginManager();
        Plugin residence = pm.getPlugin("Residence");
        if (residence != null) {
            this.protectionOverrider.add(new ResidenceProtectionOverrider());
            this.getLogger().info("Residence plugin detected.");

            // BlockBreakEvent from Residence needs to be HIGH priority
            this.overridePriority(residence, ResidenceBlockListener.class, EventPriority.LOWEST, EventPriority.LOWEST);
        }

        if (pm.getPlugin("WorldGuard") != null) {
            this.protectionOverrider.add(new WorldGuardProtectionOverrider());
            this.getLogger().info("WorldGuard plugin detected.");
        }


        // Create tool
        // @pre before inventory creation
        item = new ItemStack(Material.STICK);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(ChatColor.GOLD.toString()+ChatColor.BOLD+"Mine creator");
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

        getServer().getPluginManager().registerEvents(new BreakEvent(), this);
        getServer().getPluginManager().registerEvents(new InteractEvent(), this);
        this.mainInventory.registerEvent(this);
        this.selectMineInventory.registerEvent(this);

        getCommand("mineit").setExecutor(new CommandEvent());
        if (VersionController.version.compareTo(Version.MC_1_10) >= 0) getCommand("mineit").setTabCompleter(new HintEvent());
    }

    @Override
    public void onDisable() {
        // close inventories (if it's a reboot the players may be able to keep the items)
        this.mainInventory.closeInventories();
        this.selectMineInventory.closeInventories();
        for (BasicInventory mine : ((SelectMineInventory)this.selectMineInventory).getMinesInventories()) mine.closeInventories();

        // undo selected blocks
        for(ArrayList<Location> locations : selectedBlocks.values()) {
            for(Location l: locations) l.getBlock().setType(Mine.SELECT_BLOCK);
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

    private void overridePriority(@NotNull Plugin plugin, Class<?> match, EventPriority find, EventPriority replace) {
        ArrayList<RegisteredListener> reload = new ArrayList<>();
        Set<Listener> listeners = new HashSet<>();
        for (RegisteredListener lis : HandlerList.getRegisteredListeners(plugin)) {
            listeners.add(lis.getListener());
            reload.add(lis);
        }

        for (Listener lis : listeners) HandlerList.unregisterAll(lis);

        try {
            for (RegisteredListener lis : reload) {
                Field executorField = RegisteredListener.class.getDeclaredField("executor");
                executorField.setAccessible(true);
                EventExecutor executor = (EventExecutor)executorField.get(lis);

                Class<? extends Event> event = executor.getClass().getMethod("execute", Listener.class, Event.class).getParameterTypes()[1].asSubclass(Event.class);

                Bukkit.getPluginManager().registerEvent(event, lis.getListener(), lis.getPriority().equals(find) ? replace : lis.getPriority(), executor, plugin, lis.isIgnoringCancelled());
            }
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException ex) {
            this.printConsoleErrorMessage("Unable to override " + plugin.getName() + " event priority");
            ex.printStackTrace();
        }
    }
}
