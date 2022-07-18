package com.rogermiranda1000.mineit.inventories;

import com.rogermiranda1000.helper.BasicInventory;
import com.rogermiranda1000.mineit.MineIt;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainInventory extends BasicInventory {
    private final ItemStack mineCreatorTool;
    private final ItemStack createMine;
    private final ItemStack editMine;

    // create the inventory
    @SuppressWarnings("ConstantConditions")
    public MainInventory() {
        super(MineIt.instance, true);

        Inventory inv = Bukkit.createInventory(null, 9, "§6§lMineIt");

        this.mineCreatorTool = MineIt.item.clone();
        ItemMeta meta = this.mineCreatorTool.getItemMeta();
        List<String> l = new ArrayList<>();
        l.add("Get the Mine creator");
        meta.setLore(l);
        this.mineCreatorTool.setItemMeta(meta);
        inv.setItem(0, this.mineCreatorTool);

        this.createMine = new ItemStack(Material.DIAMOND_ORE);
        meta = this.createMine.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Create mine");
        l.clear();
        l.add("Create a new mine");
        meta.setLore(l);
        this.createMine.setItemMeta(meta);
        inv.setItem(4, this.createMine);

        this.editMine = new ItemStack(Material.COMPASS);
        meta = this.editMine.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Edit mine");
        l.clear();
        l.add("Edit current mines");
        meta.setLore(l);
        this.editMine.setItemMeta(meta);
        inv.setItem(8, this.editMine);

        this.setInventory(inv);
    }

    public void inventoryClickedEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        // permisions
        if(!player.hasPermission("mineit.open")) {
            player.closeInventory();
            player.sendMessage(MineIt.instance.errorPrefix + "You can't use this menu.");
            return;
        }

        ItemStack clicked = e.getCurrentItem();
        if(clicked==null) return;
        boolean toolClicked = (clicked.equals(this.mineCreatorTool) || clicked.equals(this.editMine) || clicked.equals(this.createMine));
        if (!toolClicked) return;

        if(!player.hasPermission("mineit.create")) {
            player.sendMessage(MineIt.instance.errorPrefix + "You can't use this action.");
            return;
        }

        // interaction
        if(clicked.equals(this.mineCreatorTool)) {
            player.closeInventory();
            player.getInventory().addItem(MineIt.item);
        }
        else if(clicked.equals(this.createMine)) {
            player.closeInventory();
            player.sendMessage(MineIt.instance.errorPrefix + "Under construction, use " + ChatColor.AQUA + "/mineit create [name]" + ChatColor.RED + " instead.");
        }
        else {
            // clicked.equals(this.editMine)
            MineIt.instance.selectMineInventory.openInventory(player);
        }
    }
}
