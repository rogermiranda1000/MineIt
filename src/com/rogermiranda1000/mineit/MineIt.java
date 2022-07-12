package com.rogermiranda1000.mineit;

import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.helper.reflection.OnServerEvent;
import com.rogermiranda1000.helper.reflection.SpigotEventOverrider;
import com.rogermiranda1000.mineit.blocks.Mines;
import com.rogermiranda1000.mineit.blocks.SelectedBlocks;
import com.rogermiranda1000.mineit.events.InteractEvent;
import com.rogermiranda1000.mineit.file.FileManager;
import com.rogermiranda1000.mineit.file.InvalidLocationException;
import com.rogermiranda1000.mineit.inventories.BasicInventory;
import com.rogermiranda1000.mineit.inventories.MainInventory;
import com.rogermiranda1000.mineit.inventories.SelectMineInventory;
import com.sk89q.worldguard.bukkit.listener.EventAbstractionListener;
import com.sk89q.worldguard.bukkit.listener.WorldGuardBlockListener;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MineIt extends RogerPlugin {
    public static ItemStack item, mimicBlock;
    public static MineIt instance;

    //Inv
    public BasicInventory mainInventory;
    public BasicInventory selectMineInventory;

    public ArrayList<OnServerEvent<BlockBreakEvent>> protectionOverrider;

    public int rango;
    public boolean limit;
    public boolean overrideProtection;

    @Override
    public String getPluginID() {
        return "69161";
    }

    public MineIt() {
        super(CustomMineItCommand.commands, new InteractEvent());

        this.addCustomBlock(Mines.setInstance(new Mines(this)));
        this.addCustomBlock(SelectedBlocks.setInstance(new SelectedBlocks(this)));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onEnable() {
        super.onEnable();

        MineIt.instance = this;

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

        // Protections [done by a higher priority]
        // TODO allow more event types
        // TODO save priorities (sorted list) & ignoreCancelled
        PluginManager pm = getServer().getPluginManager();
        this.protectionOverrider = new ArrayList<>();
        Plugin residence = pm.getPlugin("Residence");
        if (residence != null) {
            this.getLogger().info("Residence plugin detected.");
            this.protectionOverrider.add(SpigotEventOverrider.overrideListener(residence, ResidenceBlockListener.class, BlockBreakEvent.class));
        }

        Plugin worldguard = pm.getPlugin("WorldGuard");
        if (worldguard != null) {
            this.getLogger().info("WorldGuard plugin detected.");
            this.protectionOverrider.add(SpigotEventOverrider.overrideListener(worldguard, WorldGuardBlockListener.class, BlockBreakEvent.class));
            this.protectionOverrider.add(SpigotEventOverrider.overrideListener(worldguard, EventAbstractionListener.class, BlockBreakEvent.class));
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
                    Mines.getInstance().addMine(mine);
                } catch (IOException | IllegalArgumentException ex) {
                    this.printConsoleErrorMessage("Invalid file format, the mine '" + mineName + "' can't be loaded. If you have updated the plugin delete the file and create the mine again.");
                } catch (InvalidLocationException ex) {
                    this.printConsoleErrorMessage("Error, the mine '" + mineName + "' can't be loaded. " + ex.getMessage());
                }
            }
        } catch (ClassNotFoundException ex) {
            this.printConsoleErrorMessage( "MineIt needs Gson in order to work.");
        }

        this.mainInventory.registerEvent(this);
        this.selectMineInventory.registerEvent(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // close inventories (if it's a reboot the players may be able to keep the items)
        this.mainInventory.closeInventories();
        this.selectMineInventory.closeInventories();
        for (BasicInventory mine : ((SelectMineInventory)this.selectMineInventory).getMinesInventories()) mine.closeInventories();

        // undo selected blocks
        SelectedBlocks.getInstance().getAllBlocks(e -> e.getValue().getBlock().setType(Mine.SELECT_BLOCK));

        // save mines
        Mines.getInstance().getAllBlocks(e -> {
            Mine m = e.getKey();
            try {
                File file = new File(getDataFolder(), m.getName() +".yml");
                FileManager.saveMine(file, m);
            } catch(IOException ex){
                ex.printStackTrace();
            }
        });
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
}
