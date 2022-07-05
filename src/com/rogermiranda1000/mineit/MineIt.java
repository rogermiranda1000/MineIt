package com.rogermiranda1000.mineit;

import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.rogermiranda1000.mineit.events.BreakEvent;
import com.rogermiranda1000.mineit.events.CommandEvent;
import com.rogermiranda1000.mineit.events.InteractEvent;
import com.rogermiranda1000.mineit.events.HintEvent;
import com.rogermiranda1000.mineit.file.FileManager;
import com.rogermiranda1000.mineit.file.InvalidLocationException;
import com.rogermiranda1000.mineit.inventories.BasicInventory;
import com.rogermiranda1000.mineit.inventories.MainInventory;
import com.rogermiranda1000.mineit.inventories.SelectMineInventory;
import com.rogermiranda1000.mineit.protections.OnEvent;
import com.rogermiranda1000.versioncontroller.Version;
import com.rogermiranda1000.versioncontroller.VersionChecker;
import com.rogermiranda1000.versioncontroller.VersionController;
import com.sk89q.worldguard.bukkit.listener.EventAbstractionListener;
import com.sk89q.worldguard.bukkit.listener.WorldGuardBlockListener;
import net.md_5.bungee.api.ChatColor;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.MultiLineChart;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MineIt extends JavaPlugin {
    public static final String PLUGIN_ID = "69161";
    public static final int PLUGIN_BSTATS_ID = 15679;
    public static final String clearPrefix = ChatColor.GOLD.toString() + ChatColor.BOLD + "[MineIt] " + ChatColor.GREEN,
            errorPrefix = ChatColor.GOLD.toString() + ChatColor.BOLD + "[MineIt] " + ChatColor.RED;
    public static ItemStack item, mimicBlock;
    public static MineIt instance;

    //Inv
    public BasicInventory mainInventory;
    public BasicInventory selectMineInventory;

    public ArrayList<OnEvent> protectionOverrider;

    private final HashMap<String, Stack<ArrayList<Location>>> selectedBlocksHistory = new HashMap<>();

    public int rango;
    public boolean limit;
    public boolean overrideProtection;

    public void printConsoleErrorMessage(String msg) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] " + msg);
    }

    public void printConsoleWarningMessage(String msg) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[" + this.getName() + "] " + msg);
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
        c.put("report_data", true);
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

        // Protections [done by a higher priority]
        // TODO allow more event types
        // TODO save priorities (sorted list) & ignoreCancelled
        PluginManager pm = getServer().getPluginManager();
        this.protectionOverrider = new ArrayList<>();
        Plugin residence = pm.getPlugin("Residence");
        if (residence != null) {
            this.getLogger().info("Residence plugin detected.");
            Listener lis = getListener(residence, ResidenceBlockListener.class);
            this.protectionOverrider.add(MineIt.getOnEventFunction(residence.getName(), MineIt.overrideListener(residence, ResidenceBlockListener.class, "onBlockBreak"), lis));
        }

        Plugin worldguard = pm.getPlugin("WorldGuard");
        if (worldguard != null) {
            this.getLogger().info("WorldGuard plugin detected.");
            Listener lis = getListener(worldguard, WorldGuardBlockListener.class);
            this.protectionOverrider.add(MineIt.getOnEventFunction(worldguard.getName(), MineIt.overrideListener(worldguard, WorldGuardBlockListener.class, "onBlockBreak"), lis));
            lis = getListener(worldguard, EventAbstractionListener.class);
            this.protectionOverrider.add(MineIt.getOnEventFunction(worldguard.getName(), MineIt.overrideListener(worldguard, EventAbstractionListener.class, "onBlockBreak"), lis));
        }


        // Create tool
        // @pre before inventory creation
        item = new ItemStack(Material.STICK);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(ChatColor.GOLD.toString()+ChatColor.BOLD+"Mine creator");
        item.setItemMeta(m);
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        mimicBlock = new ItemStack(Material.STONE);
        m = mimicBlock.getItemMeta();
        m.setDisplayName(ChatColor.GOLD+"Mimic block");
        ArrayList<String> l = new ArrayList<>();
        l.add("Click the right mouse button");
        l.add("while holding this block and");
        l.add("looking the desired block.");
        m.setLore(l);
        m.addEnchant(Enchantment.DURABILITY, 1, true);
        mimicBlock.setItemMeta(m);

        // @pre before mine import
        this.mainInventory = new MainInventory();
        this.selectMineInventory = new SelectMineInventory();

        //Minas
        try {
            Class.forName("com.google.gson.JsonSyntaxException");
            for (File archivo : getDataFolder().listFiles()) {
                if (archivo.getName().equalsIgnoreCase("config.yml")) continue;

                String mineName = archivo.getName().replaceAll("\\.yml$", "");
                try {
                    getLogger().info("Loading mine " + mineName + "..."); // TODO .json
                    Mine mine = FileManager.loadMine(archivo);
                    Mine.addMine(mine);
                } catch (IOException | IllegalArgumentException ex) {
                    this.printConsoleErrorMessage("Invalid file format, the mine '" + mineName + "' can't be loaded. If you have updated the plugin delete the file and create the mine again.");
                } catch (InvalidLocationException ex) {
                    this.printConsoleErrorMessage("Error, the mine '" + mineName + "' can't be loaded. " + ex.getMessage());
                }
            }
        } catch (ClassNotFoundException ex) {
            this.printConsoleErrorMessage( "MineIt needs Gson in order to work.");
        }

        getServer().getPluginManager().registerEvents(new BreakEvent(), this);
        getServer().getPluginManager().registerEvents(new InteractEvent(), this);
        this.mainInventory.registerEvent(this);
        this.selectMineInventory.registerEvent(this);

        getCommand("mineit").setExecutor(new CommandEvent());
        if (VersionController.version.compareTo(Version.MC_1_10) >= 0) getCommand("mineit").setTabCompleter(new HintEvent());

        if (config.getBoolean("report_data")) {
            Metrics metrics = new Metrics(this, MineIt.PLUGIN_BSTATS_ID);
            metrics.addCustomChart(new MultiLineChart("mines", ()->{
                Map<String, Integer> minesAndBlocks = new HashMap<>();

                minesAndBlocks.put("mines", MineItApi.getInstance().getMineCount());

                int blocks = 0;
                for (Mine mine : MineItApi.getInstance().getMines()) blocks += mine.getMineBlocks().size();
                minesAndBlocks.put("blocks", blocks);

                return minesAndBlocks;
            }));
            metrics.addCustomChart(new SimplePie("protections", ()->{
                if (overrideProtection) return "None";
                if (residence != null) {
                    return (worldguard != null) ? "Residence & WorldGuard" : "Residence";
                }
                if (worldguard != null) return "WorldGuard";
                return "None";
            }));
        }
    }

    private static OnEvent getOnEventFunction(String plugin, Method m, Listener lis) {
        return (e) -> {
            try {
                m.invoke(lis, e);
                return false;
            } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                MineIt.instance.printConsoleErrorMessage("Error while overriding " + plugin + " event (" + lis.getClass().getName() + "#" + m.getName() + ")");
                ex.printStackTrace();
                return true;
            }
        };
    }

    @Override
    public void onDisable() {
        // close inventories (if it's a reboot the players may be able to keep the items)
        this.mainInventory.closeInventories();
        this.selectMineInventory.closeInventories();
        for (BasicInventory mine : ((SelectMineInventory)this.selectMineInventory).getMinesInventories()) mine.closeInventories();

        // undo selected blocks
        for(Location l : this.getAllSelectedBlocks()) l.getBlock().setType(Mine.SELECT_BLOCK);

        // save mines
        for (Mine mina : Mine.getMines()) {
            try {
                File file = new File(getDataFolder(), mina.getName() +".yml");
                FileManager.saveMine(file, mina);
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void addSelectionBlocks(String name, ArrayList<Location> locations) {
        this.selectedBlocksHistory.computeIfAbsent(name, k -> new Stack<>())
                .add(locations);
    }

    @Nullable
    public ArrayList<Location> getSelectedBlocks(String name) {
        return MineIt.merge(this.selectedBlocksHistory.get(name));
    }

    public ArrayList<Location> getAllSelectedBlocks() {
        ArrayList<Location> r = new ArrayList<>();
        for (Stack<ArrayList<Location>> e : this.selectedBlocksHistory.values()) r.addAll(MineIt.merge(e));
        return r;
    }

    @Nullable
    public ArrayList<Location> getLastSelectedBlocksAndRemove(String name) {
        Stack<ArrayList<Location>> b = this.selectedBlocksHistory.get(name);
        if (b == null || b.empty()) return null;
        return b.pop();
    }

    @Nullable
    public ArrayList<Location> removeSelectionBlocks(String name) {
        return MineIt.merge(this.selectedBlocksHistory.remove(name));
    }

    @Nullable
    private static <T> ArrayList<T> merge(@Nullable Stack<ArrayList<T>> list) {
        if (list == null) return null;

        ArrayList<T> r = new ArrayList<>();
        for (List<T> loc : list) {
            r.addAll(loc);
        }
        return r;
    }

    public boolean isSelected(Location loc) {
        return this.getAllSelectedBlocks().contains(loc);
    }

    private static Listener getListener(@NotNull Plugin plugin, Class<?> match) {
        Listener lis = null;
        for (RegisteredListener l : HandlerList.getRegisteredListeners(plugin)) {
            if (l.getListener().getClass().equals(match)) {
                lis = l.getListener();
                break;
            }
        }
        return lis;
    }

    /**
     * Finds the desired listener and remove it and returns it
     * @param plugin Plugin registering the listener
     * @param match Class registering the listener
     * @param name Listener name
     * @return Method to call (if any match)
     */
    private static Method overrideListener(final @NotNull Plugin plugin, Class<?> match, String name) throws ListenerNotFoundException {
        final Listener lis = getListener(plugin, match);
        if (lis == null) throw new ListenerNotFoundException("Unable to override " + plugin.getName() + " event priority: Listener not found");

        HandlerList.unregisterAll(lis); // all the RegisteredListener on reload are the same Listener

        Method r = null;
        for (final Method m: match.getDeclaredMethods()) {
            // is it an event?
            if (m.getParameterCount() != 1) continue;
            if (!Event.class.isAssignableFrom(m.getParameterTypes()[0])) continue;
            EventHandler eventHandler = m.getAnnotation(EventHandler.class);
            if (eventHandler == null) continue;

            // register again the event, but with the desired priority
            if (m.getName().equals(name)) r = m;
            else {
                final Class<? extends Event> type = m.getParameterTypes()[0].asSubclass(Event.class);
                Bukkit.getPluginManager().registerEvent(type, lis, eventHandler.priority(), (l, e) -> {
                    try {
                        try {
                            m.invoke(l, type.cast(e));
                        } catch (ClassCastException ignore) {}
                    } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        MineIt.instance.printConsoleErrorMessage("Error while overriding " + plugin + " event (" + lis.getClass().getName() + "#" + m.getName() + ")");
                        ex.printStackTrace();
                        MineIt.instance.printConsoleErrorMessage("Protection override failure. Notice this may involve players being able to remove protected regions, so report this error immediately and use an older version of MineIt.");
                    }
                }, plugin, eventHandler.ignoreCancelled());
            }
        }

        if (r == null) throw new ListenerNotFoundException();
        return r;
    }
}
