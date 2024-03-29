package com.rogermiranda1000.mineit;

import com.rogermiranda1000.helper.BasicInventory;
import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.helper.metrics.charts.CustomChart;
import com.rogermiranda1000.helper.metrics.charts.SimplePie;
import com.rogermiranda1000.helper.metrics.charts.SingleLineChart;
import com.rogermiranda1000.mineit.mine.placer.AsyncBlockPlacer;
import com.rogermiranda1000.mineit.mine.blocks.Mines;
import com.rogermiranda1000.mineit.mine.blocks.SelectedBlocks;
import com.rogermiranda1000.mineit.events.InteractEvent;
import com.rogermiranda1000.mineit.file.FileManager;
import com.rogermiranda1000.mineit.inventories.MainInventory;
import com.rogermiranda1000.mineit.inventories.MinesInventory;
import com.rogermiranda1000.mineit.inventories.SelectMineInventory;
import com.rogermiranda1000.mineit.inventories.TpMineInventory;
import com.rogermiranda1000.mineit.mine.Mine;
import io.sentry.Attachment;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private int blockPlacerThread;


    private List<Mine> tmp;

    @Override
    public String getPluginID() {
        return "69161";
    }

    @Override
    public Integer getMetricsID() {
        return 15679;
    }

    @Nullable
    public String getSentryDsn() {
        return "https://d9d4e80c95d14929b764e0368ed63010@o1339981.ingest.sentry.io/6625896";
    }

    public MineIt() {
        super(CustomMineItCommand.commands, new CustomChart[]{
                new SingleLineChart("mines", ()->MineItApi.getInstance().getMineCount()),
                new SingleLineChart("blocks", ()->{
                    int blocks = 0;
                    for (Mine mine : MineItApi.getInstance().getMines()) blocks += mine.getTotalBlocks();
                    return blocks;
                }),
                new SimplePie("protections", ()->{
                    if (!MineIt.instance.overrideProtection) return "Disabled";

                    PluginManager pm = Bukkit.getPluginManager();
                    boolean residence = (pm.getPlugin("Residence") != null),
                            worldguard = (pm.getPlugin("WorldGuard") != null);

                    if (residence) {
                        return (worldguard) ? "Residence & WorldGuard" : "Residence";
                    }
                    if (worldguard) return "WorldGuard";
                    return "None";
                }),
                new SimplePie("mineablegems", ()->String.valueOf(Bukkit.getPluginManager().isPluginEnabled("MineableGems")))
        }, new InteractEvent(), AsyncBlockPlacer.getInstance());

        // Mines added on `preOnEnable`, after reading the configuration (as I need the property "overrideProtections")
        this.addCustomBlock(SelectedBlocks.setInstance(new SelectedBlocks(this)));
    }

    @Override
    public void preCustomBlocks() {
        // we need first the configuration
        MineIt.instance = this;

        //Config
        HashMap<String, Object> c = new HashMap<>();
        c.put("mine_creator_range", 5);
        c.put("limit_blocks_per_stage", false);
        c.put("air_stage", Material.STONE_BUTTON.name());
        c.put("override_protections", true);
        FileConfiguration config = getConfig();
        //Create/update config file
        try {
            if (!getDataFolder().exists()) getDataFolder().mkdirs();
            File file = new File(getDataFolder(), "config.yml");
            boolean need = false;

            if (!file.exists()) {
                getLogger().info("Creating config.yml...");
                file.createNewFile();
                need = true;
            }

            for (Map.Entry<String, Object> entry : c.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (!getConfig().isSet(key)) {
                    getConfig().set(key, value);
                    need = true;
                }
            }
            if (need) saveConfig();
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
        m.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "Mine creator");
        item.setItemMeta(m);
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        mimicBlock = new ItemStack(Material.STONE);
        m = mimicBlock.getItemMeta();
        m.setDisplayName(ChatColor.GOLD + "Mimic block");
        ArrayList<String> l = new ArrayList<>();
        l.add("Click the right mouse button");
        l.add("while holding this block and");
        l.add("looking the desired block.");
        m.setLore(l);
        m.addEnchant(Enchantment.DURABILITY, 1, true);
        mimicBlock.setItemMeta(m);

        // @pre `overrideProtection` variable set
        this.addCustomBlock(Mines.setInstance(new Mines(this)));
    }

    @Override
    public void preOnEnable() {
        // @pre before mine import
        this.mainInventory = new MainInventory();
        this.selectMineInventory = new SelectMineInventory();
        this.tpInventory = new TpMineInventory();

        // mines
        this.tmp = new ArrayList<>();
        File minesDirectory = new File(getDataFolder().getPath() + File.separatorChar + "Mines");
        if (minesDirectory.exists()) {
            for (File archivo : minesDirectory.listFiles()) {
                if (archivo.getName().equalsIgnoreCase("config.yml") || archivo.isDirectory()) continue;

                String mineName = archivo.getName().replaceAll("\\.json$", "");
                try {
                    getLogger().info("Loading mine " + mineName + "...");
                    Mine mine = FileManager.loadMine(archivo);
                    Mines.getInstance().addMine(mine);
                    this.tmp.add(mine); // before enabling it we need to update the stages, but we first need the mines blocks
                } catch (IOException ex) {
                    this.reportException("Invalid file format, the mine '" + mineName + "' can't be loaded.", new Attachment(archivo.getPath()));
                }
            }
        }
    }
    @Override
    public void postOnEnable() {
        // now we have the mine blocks
        for (Mine m : this.tmp) {
            m.setStart(m.isStarted());
        }
        this.tmp = null; // save resources!

        this.mainInventory.registerEvent();
        this.selectMineInventory.registerEvent();
        this.tpInventory.registerEvent();

        // TODO CustomAttribute were never implemented; that's why MineIt-MineableGems exists
        /*if (Bukkit.getPluginManager().isPluginEnabled("MineableGems")) {
            getLogger().info("Found MineableGems, loading mine drops...");
            try {
                // @pre After loading mines
                Main.getInstance().addCustomAttributes((CustomAttribute) new MineableGemsMine());
            } catch (Throwable ex) {
                this.printConsoleErrorMessage("Error while loading MineableGems");
                this.reportException(ex);
            }
        }*/

        this.blockPlacerThread = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, AsyncBlockPlacer.getInstance(), 1, 1);
    }

    /**
     * @pre dir must be a directory
     */
    private static void removeDirectoryContents(File dir) {
        for (File file: dir.listFiles()) file.delete();
    }

    @Override
    public void postOnDisable() {
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

        for (Mine m : Mines.getInstance().getAllCValues()) {
            try {
                File file = new File(minesDirectory, m.getName() +".json");
                FileManager.saveMine(file, m);
            } catch(IOException ex){
                ex.printStackTrace();
            }
        }

        Bukkit.getServer().getScheduler().cancelTask(this.blockPlacerThread);
    }
}
