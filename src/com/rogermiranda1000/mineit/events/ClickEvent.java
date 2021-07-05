package com.rogermiranda1000.mineit.events;

import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.Stage;
import com.rogermiranda1000.versioncontroller.VersionController;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClickEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        Inventory inventory = e.getInventory();
        // TODO use getClickedInventory to discard events?
        // TODO objects extends Inventory?

        if(!e.getView().getTitle()/*inventory.getName()*/.equalsIgnoreCase("§6§lMineIt") && !e.getView().getTitle()/*inventory.getName()*/.contains("§cEdit mine")) return;
        e.setCancelled(true);
        if(!player.hasPermission("mineit.open")) {
            player.closeInventory();
            player.sendMessage(MineIt.errorPrefix+"You can't use this menu.");
            return;
        }
        if(clicked==null) return;
        if((clicked.equals(MineIt.item2) || clicked.equals(MineIt.editar) || clicked.equals(MineIt.crear)) && !player.hasPermission("mineit.create")) {
            player.sendMessage(MineIt.errorPrefix+"You can't use this action.");
            return;
        }

        if(clicked.equals(MineIt.item2)) {
            player.closeInventory();
            MineIt.instance.getLogger().info("Giving Mine creator to "+player.getName()+"...");
            player.getInventory().addItem(MineIt.item);
        }
        else if(clicked.equals(MineIt.crear)) {
            player.closeInventory();
            player.sendMessage(MineIt.errorPrefix+"Under construction, use "+ ChatColor.AQUA+"/mineit create [name]"+ChatColor.RED+" instead.");
        }
        else if(clicked.equals(MineIt.editar)) editMine(player);
        else if(clicked.equals(MineIt.anvil)) {
            player.closeInventory();
            if(e.getView().getTitle()/*inventory.getName()*/.contains("§cEdit mine") && isEditing(inventory)) editMine(player);
            else player.openInventory(MineIt.inv);
            return;
        }
        else if(clicked.equals(MineIt.redstone)) {
            if(!player.hasPermission("mineit.remove")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return;
            }

            Mine mine = Mine.getMine(MineIt.instance.minas, e.getView().getTitle().substring(14));
            if(mine==null) return;

            MineIt.instance.minas.remove(mine);
            try {
                File f = new File(MineIt.instance.getDataFolder(), mine.mineName + ".yml");
                if (f.exists()) f.delete();
            }
            catch (Exception ex) {}
            player.sendMessage(MineIt.clearPrefix+"Mine '"+mine.mineName +"' removed.");
            player.closeInventory();
            return;
        }
        else if(clicked.getType()==Material.FURNACE) {
            Mine mine = Mine.getMine(MineIt.instance.minas, e.getView().getTitle().substring(14));
            if(mine==null) return;

            if(mine.getStart()) player.sendMessage(MineIt.clearPrefix+"Mine '"+mine.mineName +"' stopped.");
            else player.sendMessage(MineIt.clearPrefix+"Starting mine '"+mine.mineName +"'...");
            mine.setStart(!mine.getStart());
            //inventory.setItem(16, MineIt.instance.watch(mine));
            inventory.setItem(((((mine.getStages().size()/9) + 1)*2 + 1)*9)-2, MineIt.instance.watch(mine));
            return;
        }
        else if(e.getView().getTitle()/*inventory.getName()*/.equals("§cEdit mine") && clicked.getType()==Material.STONE && !isEditing(inventory)) {
            Mine mine = Mine.getMine(MineIt.instance.minas, clicked.getItemMeta().getDisplayName());
            if(mine==null) return;

            player.closeInventory();
            MineIt.instance.edintingMine(player, mine);
            return;
        }

        if(e.getView().getTitle()/*inventory.getName()*/.contains("§cEdit mine") && isEditing(inventory)) {
            // TODO Hotfix: bucle for no funciona con elementos repetidos (ej: dos estados que al romperse van a BEDROCK)
            for(int x = 0; x<inventory.getSize()-9; x++) {
                if(inventory.getItem(x) == null) continue; // en ese slot no hay nada
                if(!clicked.equals(inventory.getItem(x))) continue; // no es el elemento que ha pulsado

                e.setCancelled(true);

                ItemStack item = new ItemStack(player.getItemOnCursor().getType());
                if(!item.getType().equals(Material.AIR) && !item.getType().isBlock()) return;

                Mine mine = Mine.getMine(MineIt.instance.minas, e.getView().getTitle().substring(14));
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

                            // TODO delete
                            mine.removeStage(stageNum);

                            // reload inventory
                            player.closeInventory();
                            MineIt.instance.edintingMine(player, mine);
                        }
                        else {
                            String stageMaterial = item.getType().name();
                            ItemMeta m = item.getItemMeta();
                            // already exists?
                            if (mine.getStage(item.getType().name()) != null) {
                                player.sendMessage(MineIt.errorPrefix+"There's already a "+stageMaterial.toLowerCase()+" stage!");
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
                                mine.addStage(new Stage(stageMaterial));

                                List<String> st = new ArrayList<>();
                                st.add("Stage " + mine.getStageCount());
                                if (MineIt.instance.limit) st.add("Limit at " + Integer.MAX_VALUE + " blocks");
                                m.setLore(st);
                                item.setItemMeta(m);
                                inventory.setItem(mine.getStageCount()-1, item);

                                item = mine.getStage(mine.getStageCount()-2).getStageItemStack();
                                m = item.getItemMeta();
                                st = new ArrayList<>();
                                st.add("On break, go to stage " + item.getType().name());
                                m.setLore(st);
                                item.setItemMeta(m);
                                inventory.setItem(mine.getStageCount()-1+9, item);
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
                        inventory.setItem(x, item);
                        break;

                    default:
                        // ?
                }

                return;
            }
            e.setCancelled(false);
        }
    }

    void editMine(Player player) {
        player.closeInventory();
        int l = (int) (MineIt.instance.minas.size()/9);
        if(MineIt.instance.minas.size()%9>0) l++;
        if(l==0) {
            Inventory i = Bukkit.createInventory(null, 18, "§cEdit mine");
            ItemStack none = new ItemStack(Material.COBBLESTONE);
            ItemMeta m = none.getItemMeta();
            m.setDisplayName("-");
            none.setItemMeta(m);
            for(int x=0; x<9; x++) i.setItem(x, none);
            i.setItem(9, MineIt.anvil);
            player.openInventory(i);
            return;
        }
        l++;
        if(l>6) {
            player.sendMessage(MineIt.errorPrefix+"Error, too many mines. Please remove some mines.");
            return;
        }

        Inventory i = Bukkit.createInventory(null, l*9, "§cEdit mine");
        int pos=0;
        for (Mine mine: MineIt.instance.minas) {
            ItemStack mina = new ItemStack(Material.STONE);
            ItemMeta meta = mina.getItemMeta();
            meta.setDisplayName(mine.mineName);
            ArrayList<String> print = new ArrayList<>();
            for (Stage s : mine.getStages()) print.add(s.toString());
            meta.setLore(print);
            mina.setItemMeta(meta);

            i.setItem(pos++, mina);
        }
        i.setItem((l-1)*9, MineIt.anvil);

        player.openInventory(i);
    }

    boolean isEditing(Inventory i) {
        if(!i.getItem(0).hasItemMeta()) return false;
        if(!i.getItem(0).getItemMeta().hasLore()) return false;
        return i.getItem(0).getItemMeta().getLore().get(0).equalsIgnoreCase("Stage 1");
    }
}
