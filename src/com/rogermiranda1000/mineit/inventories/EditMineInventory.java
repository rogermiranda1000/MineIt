package com.rogermiranda1000.mineit.inventories;

import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.MineChangedEvent;
import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.Stage;
import com.rogermiranda1000.mineit.file.FileManager;
import com.rogermiranda1000.versioncontroller.Version;
import com.rogermiranda1000.versioncontroller.VersionController;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EditMineInventory extends BasicInventory implements MineChangedEvent {
    public static final ItemStack anvil = new ItemStack(Material.ANVIL);
    public static final ItemStack redstone = new ItemStack(Material.REDSTONE_BLOCK);
    public static final ItemStack glass = new ItemStack(Material.GLASS);
    public final ItemStack time;

    private Mine listening;

    static {
        ItemMeta m = EditMineInventory.anvil.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "Go back");
        EditMineInventory.anvil.setItemMeta(m);

        m = EditMineInventory.redstone.getItemMeta();
        m.setDisplayName(ChatColor.RED + "Remove mine");
        EditMineInventory.redstone.setItemMeta(m);

        m = EditMineInventory.glass.getItemMeta();
        m.setDisplayName("-");
        EditMineInventory.glass.setItemMeta(m);
    }

    public EditMineInventory(Mine m) {
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

        if(clicked.equals(EditMineInventory.anvil)) MineIt.instance.selectMineInventory.openInventory(player);
        else if(clicked.equals(EditMineInventory.redstone)) {
            if(!player.hasPermission("mineit.remove")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return;
            }

            Mine mine = Mine.getMine(e.getView().getTitle().substring(14));
            if(mine==null) return;

            Mine.removeMine(mine);
            try {
                FileManager.removeMine(mine);
            } catch (Exception ignored) {}
            player.sendMessage(MineIt.clearPrefix+"Mine '"+mine.mineName +"' removed.");
            // onMineRemoved event closes the inventories
        }
        else if(clicked.getType()==Material.FURNACE) {
            Mine mine = Mine.getMine(e.getView().getTitle().substring(14));
            if(mine==null) return;

            if(mine.getStart()) player.sendMessage(MineIt.clearPrefix+"Mine '"+mine.mineName +"' stopped.");
            else player.sendMessage(MineIt.clearPrefix+"Starting mine '"+mine.mineName +"'...");
            mine.setStart(!mine.getStart());
            //inventory.setItem(16, MineIt.instance.watch(mine));
            this.inv.setItem(((((mine.getStages().size()/9) + 1)*2 + 1)*9)-2, this.status());
        }
        else if (clicked.equals(this.time)) {
            // TODO
        }
        else {
            // changing stages or blocks
            // TODO Hotfix: bucle for no funciona con elementos repetidos (ej: dos estados que al romperse van a BEDROCK)
            for(int x = 0; x<this.inv.getSize()-9; x++) {
                if(this.inv.getItem(x) == null) continue; // en ese slot no hay nada
                if(!clicked.equals(this.inv.getItem(x))) continue; // no es el elemento que ha pulsado

                ItemStack item = new ItemStack(player.getItemOnCursor().getType());
                if(!item.getType().equals(Material.AIR) && !item.getType().isBlock()) return;

                Mine mine = Mine.getMine(e.getView().getTitle().substring(14));
                if(mine==null) return;

                int stageNum = x%9; // we're editing the stage nºstageNum

                switch (x/9) {
                    case 0:
                        // primera fila (la de stages)
                        if (item.getType().equals(Material.AIR)) {
                            if (stageNum >= mine.getStages().size()) return; // not enough stages

                            // remove stage
                            if (mine.getStageCount() == 1) {
                                player.sendMessage(MineIt.errorPrefix + "There can't be a mine without stages!");
                                return;
                            }
                            if (stageNum == 0) {
                                player.sendMessage(MineIt.errorPrefix + "Bedrock can't be deleted.");
                                return;
                            }

                            mine.removeStage(stageNum);
                        }
                        else {
                            Object stageMaterial = VersionController.get().getObject(item);
                            ItemMeta m = item.getItemMeta();
                            // already exists?
                            String name = VersionController.get().getName(stageMaterial);
                            if (mine.getStage(name) != null) {
                                player.sendMessage(MineIt.errorPrefix+"There's already a " + name.toLowerCase() + " stage!");
                                return;
                            }

                            if (stageNum < mine.getStageCount()) {
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
                                mine.addStage(new Stage(name));
                            }
                        }
                        break;

                    case 1:
                        // segunda fila (la de on break go to X stage)
                        if (stageNum <= 1) return; // you can't edit the 1st nor 2nd stage

                        Stage match = mine.getStage(item.getType().name());
                        if(match == null) {
                            player.sendMessage(MineIt.errorPrefix+item.getType().name().toLowerCase()+" stage doesn't exists in this mine!");
                            return;
                        }

                        mine.getStages().get(stageNum).setPreviousStage(match);

                        // actualizar vista
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

                return;
            }
        }
    }

    private ItemStack status() {
        ItemStack item = new ItemStack(Material.FURNACE);
        ItemMeta m = item.getItemMeta();
        String s = ChatColor.GREEN + "Start";
        if(this.listening.getStart()) s = ChatColor.RED + "Stop";
        m.setDisplayName(s+" mine");
        item.setItemMeta(m);

        return item;
    }

    @Override
    public void onMineChanged() {
        int lin = this.listening.getStages().size()/9 + 1;
        if(lin>2) {
            if(this.listening.getStages().size() % 9 > 0) {
                MineIt.instance.printConsoleWarningMessage("There's too many stages, the plugin can't show them all!");
                return; // TODO fill more stages/show only the fist ones
            }
            lin = this.listening.getStages().size()/9;
        }
        Inventory newInventory = Bukkit.createInventory(null, (lin*2 + 1)*9, "§cEdit mine §d" + this.listening.mineName);

        for(int x = 0; x<lin*9; x++) {
            int actualLine = (x/9)*18 + (x%9);

            if(this.listening.getStages().size()>x) {
                Stage current = this.listening.getStages().get(x);
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
                newInventory.setItem(actualLine, block);

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

                    newInventory.setItem(actualLine+9, block);
                }
            }
            else {
                newInventory.setItem(actualLine, EditMineInventory.glass);
                newInventory.setItem(actualLine+9, EditMineInventory.glass);
            }
        }
        newInventory.setItem(lin*18, EditMineInventory.anvil);
        newInventory.setItem(((lin*2 + 1)*9)-3, this.time);
        newInventory.setItem(((lin*2 + 1)*9)-2, this.status());
        newInventory.setItem(((lin*2 + 1)*9)-1, EditMineInventory.redstone);

        if (this.inv != null) this.newInventory(newInventory); // only if it's not the first time
        this.inv = newInventory;
    }

    @Override
    public void onMineRemoved() {
        this.closeInventories();
    }

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
