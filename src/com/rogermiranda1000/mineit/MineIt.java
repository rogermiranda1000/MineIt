package com.rogermiranda1000.mineit;

import com.rogermiranda1000.eventos.onUse;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class MineIt extends JavaPlugin {
    public static final String clearPrefix = ChatColor.GOLD+""+ChatColor.BOLD+"[MineIt] ", prefix=clearPrefix+ChatColor.RED;
    public static ItemStack item;

    public void onEnable() {
        getLogger().info("Plugin enabled.");

        //Crear herramienta
        item = new ItemStack(Material.STICK);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(ChatColor.GOLD+""+ChatColor.BOLD+"Mine creator");
        item.setItemMeta(m);
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        getServer().getPluginManager().registerEvents(new onUse(), this);
    }

    public void onDisable() {
        getLogger().info("Plugin disabled.");
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        if (cmd.getName().equalsIgnoreCase("mineit")) {
            if (player == null) {
                sender.sendMessage("Don't use this command in console.");
                return true;
            }
            if(!player.hasPermission("mineit.create")) {
                player.sendMessage(MineIt.prefix + "You don't have the permissions to do that.");
                return true;
            }

            if(args.length == 0) {
                getLogger().info("Giving Mine creator to "+player.getName()+"...");
                player.getInventory().addItem(item);
                return true;
            }

            player.sendMessage("Under consatruction...");
            return true;
        }
        return false;
    }
}
