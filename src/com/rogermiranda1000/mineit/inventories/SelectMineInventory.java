package com.rogermiranda1000.mineit.inventories;

import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.MinesChangedEvent;
import com.rogermiranda1000.mineit.Stage;
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

public class SelectMineInventory extends BasicInventory implements MinesChangedEvent {
    private static final String INVENTORY_NAME = "§cEdit mine";
    private final ItemStack back;

    @SuppressWarnings("ConstantConditions")
    public SelectMineInventory() {
        super();

        this.back = new ItemStack(Material.ANVIL);
        ItemMeta m = this.back.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "Go back");
        this.back.setItemMeta(m);

        this.onMineChanged(); // create the inventory for the first time

        Mine.addMinesListener(this);
    }

    @EventHandler(priority = EventPriority.HIGH)
    @SuppressWarnings("ConstantConditions")
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(this.inv)) return;

        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        // permisions
        if(!player.hasPermission("mineit.open")) {
            player.closeInventory();
            player.sendMessage(MineIt.errorPrefix + "You can't use this menu.");
            return;
        }

        ItemStack clicked = e.getCurrentItem();
        if (!this.inv.equals(e.getClickedInventory())) return;
        if(clicked==null) return;

        if(clicked.equals(this.back)) MineIt.instance.mainInventory.openInventory(player);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onMineChanged() {
        Inventory newInventory;

        int l = (int)Math.ceil(Mine.getMinesLength()/9.0);
        if(l==0) {
            newInventory = Bukkit.createInventory(null, 18, SelectMineInventory.INVENTORY_NAME);

            // fill the first row with "null" mines
            ItemStack none = new ItemStack(Material.COBBLESTONE);
            ItemMeta m = none.getItemMeta();
            m.setDisplayName("-");
            none.setItemMeta(m);
            for(int x=0; x<9; x++) newInventory.setItem(x, none);

            newInventory.setItem(9, this.back);
        }
        else {
            l++; // the last row is for the back button

            if (l > 6) {
                MineIt.instance.printConsoleWarningMessage("There's too many mines, the plugin can't show them all!");
                l = 6; // just show the first mines
            }

            newInventory = Bukkit.createInventory(null, l * 9, SelectMineInventory.INVENTORY_NAME);
            int pos = 0, backPos = (l - 1) * 9;
            for (Mine mine : Mine.getMines()) {
                ItemStack mina = new ItemStack(Material.STONE); // TODO mine block
                ItemMeta meta = mina.getItemMeta();
                meta.setDisplayName(mine.mineName);
                ArrayList<String> lore = new ArrayList<>();
                for (Stage s : mine.getStages()) lore.add(s.toString());
                meta.setLore(lore);
                mina.setItemMeta(meta);

                newInventory.setItem(pos++, mina);
                if (pos == backPos) break;
            }

            newInventory.setItem(backPos, this.back);
        }

        if (this.inv != null) this.newInventory(newInventory); // only if it's not the first time
        this.inv = newInventory;
    }
}
