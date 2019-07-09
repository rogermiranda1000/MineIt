package com.rogermiranda1000.events;

import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.Mines;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class onClick implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        Inventory inventory = e.getInventory();

        if(!inventory.getName().equals(MineIt.inv.getName()) && !inventory.getName().contains("§cEdit mine")) return;
        e.setCancelled(true);
        if(!player.hasPermission("mineit.open")) {
            player.closeInventory();
            player.sendMessage(MineIt.prefix+"You can't use this menu.");
            return;
        }
        if(clicked==null) return;
        if((clicked.equals(MineIt.item2) || clicked.equals(MineIt.editar) || clicked.equals(MineIt.crear)) && !player.hasPermission("mineit.create")) {
            player.sendMessage(MineIt.prefix+"You can't use this action.");
            return;
        }

        if(clicked.equals(MineIt.item2)) {
            player.closeInventory();
            MineIt.instance.getLogger().info("Giving Mine creator to "+player.getName()+"...");
            player.getInventory().addItem(MineIt.item);
        }
        else if(clicked.equals(MineIt.crear)) {
            player.closeInventory();
            player.sendMessage(MineIt.prefix+"Under construction, use "+ ChatColor.AQUA+"/mineit create [name]"+ChatColor.RED+" instead.");
        }
        else if(clicked.equals(MineIt.editar)) editMine(player);
        else if(clicked.equals(MineIt.anvil)) {
            player.closeInventory();
            if(inventory.getName().contains("§cEdit mine") && isEditing(inventory)) editMine(player);
            else player.openInventory(MineIt.inv);
        }
        else if(inventory.getName().equals("§cEdit mine") && clicked.getType()==Material.STONE && !isEditing(inventory)) {
            for (Mines mine: MineIt.instance.minas) {
                if (mine.name.equalsIgnoreCase(clicked.getItemMeta().getDisplayName())) {player.closeInventory();
                    edintingMine(player, mine);
                    break;
                }
            }
        }

        if(inventory.getName().contains("§cEdit mine") && isEditing(inventory)) {
            for(int x = 0; x<9; x++) {
                if(inventory.getItem(x)==null) break;
                //if(player.getItemOnCursor().getType()==Material.AIR) break;
                if(!inventory.getItem(x).equals(clicked)) continue;
                /*if(inventory.getItem(x).hasItemMeta() && inventory.getItem(x).getItemMeta().hasDisplayName() &&
                        inventory.getItem(x).getItemMeta().getDisplayName().equalsIgnoreCase("-")) break;*/

                e.setCancelled(true);
                ItemStack item = new ItemStack(player.getItemOnCursor().getType());
                //if(item==null) return;
                if(item.getType()!=Material.AIR && !item.getType().isBlock()) return;

                for(Mines mine: MineIt.instance.minas) {
                    if(!mine.name.equalsIgnoreCase(inventory.getName().substring(14))) continue;
                    if(item.getType()==Material.AIR) {
                        List<String> s = new ArrayList<String>();
                        s.addAll(Arrays.asList(mine.stages));
                        s.remove(inventory.getItem(x).getType().name());
                        if(s.size()==0) {
                            player.sendMessage(MineIt.prefix+"There can't be a null mine.");
                            return;
                        }
                        mine.stages = s.toArray(new String[s.size()]);

                        player.closeInventory();
                        edintingMine(player, mine);
                        return;
                    }

                    //item.setAmount(1);
                    ItemMeta m = item.getItemMeta();
                    if (inventory.getItem(x).hasItemMeta() && inventory.getItem(x).getItemMeta().hasLore())
                        m.setLore(inventory.getItem(x).getItemMeta().getLore());
                    else {
                        List<String> s = new ArrayList<String>();
                        s.add("Stage "+String.valueOf(mine.stages.length+1));
                        m.setLore(s);
                    }
                    item.setItemMeta(m);

                    List<String> s = new ArrayList<String>();
                    s.addAll(Arrays.asList(mine.stages));
                    if(s.contains(item.getType().name())) {
                        player.sendMessage(MineIt.prefix+"There's already a "+item.getType().name().toLowerCase()+" stage!");
                        //player.closeInventory();
                        return;
                    }
                    s.add(item.getType().name());
                    mine.stages = s.toArray(new String[s.size()]);
                }

                inventory.setItem(x, item);
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

        Inventory i = Bukkit.createInventory(null, l*9, "§cEdit mine");
        int pos=0;
        for (Mines mine: MineIt.instance.minas) {
            ItemStack mina = new ItemStack(Material.STONE);
            ItemMeta meta = mina.getItemMeta();
            meta.setDisplayName(mine.name);
            meta.setLore(Arrays.asList(mine.stages));
            mina.setItemMeta(meta);

            i.setItem(pos++, mina);
        }
        i.setItem((l-1)*9, MineIt.anvil);

        player.openInventory(i);
    }

    void edintingMine(Player player, Mines mine) {
        Inventory i = Bukkit.createInventory(null, 18, "§cEdit mine §d"+mine.name);
        for(int x = 0; x<9; x++) {
            //if(x>=9) break;
            if(mine.stages.length>x) {
                ItemStack block = new ItemStack(Material.getMaterial(mine.stages[x]));
                ItemMeta meta = block.getItemMeta();
                List<String> l = new ArrayList<String>();
                l.add("Stage " + String.valueOf(x + 1));
                meta.setLore(l);
                block.setItemMeta(meta);
                i.setItem(x, block);
            }
            else {
                ItemStack gls = new ItemStack(Material.GLASS);
                ItemMeta meta = gls.getItemMeta();
                meta.setDisplayName("-");
                            /*List<String> l = new ArrayList<String>();
                            l.add("None");
                            meta.setLore(l);*/
                gls.setItemMeta(meta);
                i.setItem(x, gls);
            }
        }
        i.setItem(9, MineIt.anvil);
        player.openInventory(i);
    }

    boolean isEditing(Inventory i) {
        if(!i.getItem(0).hasItemMeta()) return false;
        if(!i.getItem(0).getItemMeta().hasLore()) return false;
        return i.getItem(0).getItemMeta().getLore().get(0).equalsIgnoreCase("Stage 1");
    }
}
