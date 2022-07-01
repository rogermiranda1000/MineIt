package com.rogermiranda1000.mineit.inventories;

import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.MineChangedEvent;
import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.Stage;
import com.rogermiranda1000.mineit.file.FileManager;
import com.rogermiranda1000.versioncontroller.Version;
import com.rogermiranda1000.versioncontroller.VersionController;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EditMineInventory extends BasicInventory implements MineChangedEvent {
    public static final ItemStack BACK_ITEM = new ItemStack(Material.ANVIL);
    public static final ItemStack REMOVE_ITEM = new ItemStack(Material.REDSTONE_BLOCK);
    public static final ItemStack glass = new ItemStack(Material.GLASS);
    public final ItemStack time;

    private final Mine listening;

    static {
        ItemMeta m = EditMineInventory.BACK_ITEM.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "Go back");
        EditMineInventory.BACK_ITEM.setItemMeta(m);

        m = EditMineInventory.REMOVE_ITEM.getItemMeta();
        m.setDisplayName(ChatColor.RED + "Remove mine");
        EditMineInventory.REMOVE_ITEM.setItemMeta(m);

        m = EditMineInventory.glass.getItemMeta();
        m.setDisplayName("-");
        EditMineInventory.glass.setItemMeta(m);
    }

    public EditMineInventory(@NotNull Mine m) {
        super();

        this.registerEvent(MineIt.instance); // listener

        this.listening = m;
        m.addMineListener(this);

        this.time = this.time();

        this.onMineChanged(); // force to create the inventory
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(this.inv)) return;
        if (!this.inv.equals(e.getClickedInventory())) return;

        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        // permisions
        if(!player.hasPermission("mineit.open")) {
            player.closeInventory();
            player.sendMessage(MineIt.errorPrefix + "You can't use this menu.");
            return;
        }

        ItemStack clicked = e.getCurrentItem();
        if(clicked==null) return;

        if(clicked.equals(EditMineInventory.BACK_ITEM)) MineIt.instance.selectMineInventory.openInventory(player);
        else if(clicked.equals(EditMineInventory.REMOVE_ITEM)) {
            if(!player.hasPermission("mineit.remove")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return;
            }

            Mine.removeMine(this.listening);
            try {
                FileManager.removeMine(this.listening);
            } catch (Exception ignored) {}
            player.sendMessage(MineIt.clearPrefix+"Mine '" + this.listening.getName() + "' removed.");
            // onMineRemoved event closes the inventories
        }
        else if(clicked.getType()==Material.FURNACE) {
            if(this.listening.getStart()) {
                player.sendMessage(MineIt.clearPrefix+"Mine '" + this.listening.getName() + "' stopped.");
                this.listening.setStart(false);
            }
            else {
                player.sendMessage(MineIt.clearPrefix+"Starting mine '" + this.listening.getName() + "'...");
                this.listening.setStart(true);
            }
            this.inv.setItem(this.getFurnaceIndex(), this.status());
        }
        else if (clicked.equals(this.time)) {
            // TODO
        }
        else {
            int x = e.getSlot();
            if (this.inv.getItem(x) == null || x >= this.getLastRowIndex()) return; // en ese slot no hay nada o estan en la última fila (no deberia pasar)

            ItemStack item = VersionController.get().cloneItemStack(player.getItemOnCursor());
            if(!item.getType().equals(Material.AIR) && !item.getType().isBlock()) return;

            int line = x/18,
                stageNum = line*9 + x%9; // we're editing the stage nºstageNum
            switch ((x - line*18)/9) {
                case 0:
                    // primera fila (la de stages)
                    if (item.getType().equals(Material.AIR)) {
                        if (stageNum >= this.listening.getStageCount()) return; // not enough stages

                        // remove stage
                        if (this.listening.getStageCount() == 1) {
                            player.sendMessage(MineIt.errorPrefix + "There can't be a mine without stages!");
                            return;
                        }
                        if (stageNum == 0) {
                            player.sendMessage(MineIt.errorPrefix + StringUtils.capitalize(Mine.STATE_ZERO.name()) + " stage can't be deleted.");
                            return;
                        }

                        this.listening.removeStage(stageNum);
                    }
                    else {
                        Object stageMaterial = VersionController.get().getObject(player.getItemOnCursor().getType().equals(Mine.AIR_STAGE) ? new ItemStack(Material.AIR) : player.getItemOnCursor());
                        boolean isBreakable = !player.getItemOnCursor().containsEnchantment(Enchantment.DURABILITY) && !player.getItemOnCursor().getType().equals(Mine.AIR_STAGE); // unbreakable not set, and not air
                        // already exists?
                        if (this.listening.getStage(stageMaterial) != null) {
                            player.sendMessage(MineIt.errorPrefix+"There's already a " + VersionController.get().getName(stageMaterial).toLowerCase() + " stage!");
                            return;
                        }

                        if (stageNum < this.listening.getStageCount()) {
                            // sobreescribir estado
                            // TODO sobreescribir
                            /*m.setLore(inventory.getItem(x).getItemMeta().getLore());
                            item.setItemMeta(m);

                            for (int y = 0; y<mine.getStages().size(); y++) {
                                if(mine.getStages().get(y).equalsIgnoreCase(inventory.getItem(x).getType().name())) {
                                    mine.getStages().set(y, item.getType().name());
                                    break;
                                }
                            }
                            mine.stages = mine.getStages().toArray(new String[mine.getStages().size()]);
                            if(MineIt.instance.limit) MineIt.instance.updateStages(mine);
                            inventory.setItem(x, item);*/
                        }
                        else {
                            // nuevo estado
                            this.listening.addStage(new Stage(stageMaterial, isBreakable));
                        }
                    }
                    break;

                case 1:
                    // segunda fila (la de on break go to X stage)
                    if (item.getType().equals(Material.AIR) || stageNum >= this.listening.getStageCount()) return; // no previous stage configuration

                    Stage match = this.listening.getStage(VersionController.get().getObject(item.getType().equals(Mine.AIR_STAGE) ? new ItemStack(Material.AIR) : item));
                    if(match == null) {
                        player.sendMessage(MineIt.errorPrefix+item.getType().name().toLowerCase()+" stage doesn't exists in this mine!");
                        return;
                    }

                    this.listening.getStage(stageNum).setPreviousStage(match);

                    // update view
                    ItemMeta m = item.getItemMeta();
                    List<String> str = new ArrayList<>();
                    str.add("On break, go to stage " + item.getType().name());
                    m.setLore(str);
                    item.setItemMeta(m);
                    this.inv.setItem(x, item);
                    break;

                default:
                    // ?
            }
        }
    }

    @Override
    public void onMineChanged() {
        /**
         * The menu shows the stages in gropus of two (current stage - stage on block break)
         * 'lines' indicates the number of pairs that must be shown
         */
        int lines = this.listening.getStageCount()/9 + 1;
        if (this.listening.getStageCount() == 18) lines = 2; // the '+1' caused the overflow
        else if (lines > 2) {
            MineIt.instance.printConsoleWarningMessage("There's too many stages, the plugin can't show them all!");
            return; // TODO fill more stages/show only the fist ones
        }
        Inventory newInventory = Bukkit.createInventory(null, (lines*2 + 1)*9, ChatColor.RED + "Edit mine " + ChatColor.LIGHT_PURPLE + this.listening.getName());

        for(int x = 0; x<lines*9; x++) {
            int actualLine = (x/9)*18 + (x%9);

            if(this.listening.getStageCount() <= x) {
                newInventory.setItem(actualLine, EditMineInventory.glass);
                newInventory.setItem(actualLine+9, EditMineInventory.glass);
                continue;
            }

            Stage current = this.listening.getStage(x);
            ItemStack block = current.getStageItemStack();
            ItemMeta meta = block.getItemMeta();
            if (Mine.AIR_STAGE != null && (block.getType().equals(Material.AIR) || meta == null)) {
                // AIR
                block = new ItemStack(Mine.AIR_STAGE);
                meta = block.getItemMeta();
                meta.setDisplayName("Air");
            }
            List<String> l = new ArrayList<>();
            l.add("Stage " + (x + 1));
            if (!current.isBreakable()) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                l.add("Unbreakable stage");
            }
            if(MineIt.instance.limit) l.add("Limit setted to " + current.getStageLimit() + " blocks");
            meta.setLore(l);
            block.setItemMeta(meta);
            newInventory.setItem(actualLine, block);

            Stage previousStage = current.getPreviousStage();
            if(previousStage != null && current.isBreakable() && !block.getType().equals(Mine.AIR_STAGE)) {
                ItemStack bottomBlock = current.getPreviousStage().getStageItemStack();
                meta = bottomBlock.getItemMeta();
                if (Mine.AIR_STAGE != null && (bottomBlock.getType().equals(Material.AIR) || meta == null)) {
                    // AIR
                    bottomBlock = new ItemStack(Mine.AIR_STAGE);
                    meta = bottomBlock.getItemMeta();
                    meta.setDisplayName("Air");
                }
                l = new ArrayList<>();
                l.add("On break, go to stage " + previousStage.getName());
                if (!previousStage.isBreakable()) {
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    l.add("Unbreakable stage");
                }
                meta.setLore(l);
                bottomBlock.setItemMeta(meta);

                newInventory.setItem(actualLine+9, bottomBlock);
            }
        }

        newInventory.setItem(this.getBackIndex(), EditMineInventory.BACK_ITEM);
        newInventory.setItem(this.getTimeIndex(), this.time);
        newInventory.setItem(this.getFurnaceIndex(), this.status());
        newInventory.setItem(this.getRemoveIndex(), EditMineInventory.REMOVE_ITEM);

        if (this.inv != null) this.newInventory(newInventory); // only if it's not the first time
        this.inv = newInventory;
    }

    @Override
    public void onMineRemoved() {
        this.closeInventories();
    }

    /**
     * This function returns the first index of the last inventory row
     * @return First index of the last inventory row
     */
    private int getLastRowIndex() {
        if (this.listening.getStageCount() == 18) return 2*2*9;
        return ((this.listening.getStageCount()/9) + 1)*2*9;
    }

    private int getFurnaceIndex() {
        return this.getLastRowIndex()+7;
    }

    private int getTimeIndex() {
        return this.getLastRowIndex()+6;
    }

    private int getBackIndex() {
        return this.getLastRowIndex();
    }

    private int getRemoveIndex() {
        return this.getLastRowIndex()+8;
    }

    /**
     * It generates the furnace to start and stop the mine
     * @return FURNACE item with name that depends
     */
    @SuppressWarnings("ConstantConditions")
    private ItemStack status() {
        ItemStack item = new ItemStack(Material.FURNACE);
        ItemMeta m = item.getItemMeta();
        if(this.listening.getStart()) m.setDisplayName(ChatColor.RED + "Stop mine");
        else m.setDisplayName(ChatColor.GREEN + "Start mine");
        item.setItemMeta(m);

        return item;
    }

    /**
     * It generates the clock to see how many delay per stage
     * @return CLOCK/WATCH item with the delay as name
     */
    @SuppressWarnings("ConstantConditions")
    private ItemStack time() {
        Material mat;
        if (VersionController.version.compareTo(Version.MC_1_12) > 0) mat = Material.getMaterial("CLOCK");
        else mat = Material.getMaterial("WATCH"); // <= 1.12 clock's name is "watch"
        ItemStack clock = new ItemStack(mat);
        ItemMeta m = clock.getItemMeta();
        m.setDisplayName("Mine time");
        List<String> lore = new ArrayList<>();
        lore.add(this.listening.getDelay() + "s per stage");
        m.setLore(lore);
        clock.setItemMeta(m);

        return clock;
    }
}
