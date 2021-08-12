package com.rogermiranda1000.mineit.inventories;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SelectMineInventory extends BasicInventory {
    private final ItemStack back;

    @SuppressWarnings("ConstantConditions")
    public SelectMineInventory() {
        this.inv = Bukkit.createInventory(null, 18, "§cEdit mine");

        this.back = new ItemStack(Material.ANVIL);
        ItemMeta m = this.back.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "Go back");
        this.back.setItemMeta(m);

        /*
        player.closeInventory();
        int l = Mine.getMinesLength()/9;
        if(Mine.getMinesLength()%9>0) l++;
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
        for (Mine mine: Mine.getMines()) {
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
         */
    }

    @EventHandler(priority = EventPriority.HIGH)
    @SuppressWarnings("ConstantConditions")
    public void onClick(InventoryClickEvent e) {

    }
}
