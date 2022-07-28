package com.rogermiranda1000.mineit;

import com.rogermiranda1000.helper.BasicInventory;
import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.helper.metrics.Metrics;
import com.rogermiranda1000.mineit.blocks.Mines;
import com.rogermiranda1000.mineit.blocks.SelectedBlocks;
import com.rogermiranda1000.mineit.events.InteractEvent;
import com.rogermiranda1000.mineit.file.FileManager;
import com.rogermiranda1000.mineit.inventories.MainInventory;
import com.rogermiranda1000.mineit.inventories.MinesInventory;
import com.rogermiranda1000.mineit.inventories.SelectMineInventory;
import com.rogermiranda1000.mineit.inventories.TpMineInventory;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MineIt extends RogerPlugin {
    public static ItemStack item, mimicBlock;
    public static MineIt instance;

    //Inv
    public BasicInventory mainInventory;
    public MinesInventory selectMineInventory,
                        tpInventory;

    public int rango;
    public boolean limit;
    public boolean overrideProtection;

    @Override
    public String getPluginID() {
        return "69161";
    }

    @Override
    public Integer getMetricsID() {
        return 15679;
    }

    public MineIt() {
        super(CustomMineItCommand.commands, new Metrics.CustomChart[]{
                new Metrics.MultiLineChart("mines", ()->{
                    Map<String, Integer> minesAndBlocks = new HashMap<>();

                    minesAndBlocks.put("mines", MineItApi.getInstance().getMineCount());

                    int blocks = 0;
                    for (Mine mine : MineItApi.getInstance().getMines()) blocks += mine.getTotalBlocks();
                    minesAndBlocks.put("blocks", blocks);

                    return minesAndBlocks;
                }),
                new Metrics.SimplePie("protections", ()->{
                    if (MineIt.instance.overrideProtection) return "None";

                    PluginManager pm = MineIt.instance.getServer().getPluginManager();
                    boolean residence = (pm.getPlugin("Residence") != null),
                            worldguard = (pm.getPlugin("WorldGuard") != null);

                    if (residence) {
                        return (worldguard) ? "Residence & WorldGuard" : "Residence";
                    }
                    if (worldguard) return "WorldGuard";
                    return "None";
                })
        },new InteractEvent());

        this.addCustomBlock(Mines.setInstance(new Mines(this)));
        this.addCustomBlock(SelectedBlocks.setInstance(new SelectedBlocks(this)));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onEnable() {
        // we need first the configuration
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
        this.tpInventory = new TpMineInventory();

        this.clearCustomBlocks();

        // mines
        File minesDirectory = new File(getDataFolder().getPath() + File.separatorChar + "Mines");
        if (minesDirectory.exists()) {
            for (File archivo : minesDirectory.listFiles()) {
                if (archivo.getName().equalsIgnoreCase("config.yml") || archivo.isDirectory()) continue;

                String mineName = archivo.getName().replaceAll("\\.json$", "");
                try {
                    getLogger().info("Loading mine " + mineName + "...");
                    Mine mine = FileManager.loadMine(archivo);
                    Mines.getInstance().addMine(mine);
                } catch (IOException ex) {
                    this.printConsoleErrorMessage("Invalid file format, the mine '" + mineName + "' can't be loaded. If you have updated the plugin delete the file and create the mine again.");
                }
            }
        }

        super.onEnable();

        this.mainInventory.registerEvent();
        this.selectMineInventory.registerEvent();
        this.tpInventory.registerEvent();
    }

    /**
     * @pre dir must be a directory
     */
    private static void removeDirectoryContents(File dir) {
        for (File file: dir.listFiles()) file.delete();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // close inventories (if it's a reboot the players may be able to keep the items)
        this.mainInventory.closeInventories();
        this.selectMineInventory.closeInventories();
        this.tpInventory.closeInventories();
        for (BasicInventory mine : ((SelectMineInventory)this.selectMineInventory).getMinesInventories()) mine.closeInventories();

        // undo selected blocks
        SelectedBlocks.getInstance().getAllBlocks(e -> e.getValue().getBlock().setType(Mine.SELECT_BLOCK));

        // save mines
        File minesDirectory = new File(getDataFolder().getPath() + File.separatorChar + "Mines");
        if (!minesDirectory.exists()) minesDirectory.mkdir();
        else MineIt.removeDirectoryContents(minesDirectory);

        for (Mine m : Mines.getInstance().getAllValues()) {
            try {
                File file = new File(minesDirectory, m.getName() +".json");
                FileManager.saveMine(file, m);
            } catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }
}
