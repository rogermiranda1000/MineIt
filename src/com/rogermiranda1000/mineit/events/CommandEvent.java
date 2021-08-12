package com.rogermiranda1000.mineit.events;

import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.MineIt;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public class CommandEvent implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        // TODO unselect
        // TODO append to existing mine
        // TODO mine list
        Player player = (sender instanceof Player) ? (Player) sender : null;
        if (player == null) {
            sender.sendMessage("Don't use this command in console.");
            return true;
        }

        if(args.length == 0) {
            if(!player.hasPermission("mineit.open")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return true;
            }
            player.openInventory(MineIt.instance.mainInventory.getInventory());
            return true;
        }

        if(args[0].equalsIgnoreCase("?")) {
            player.sendMessage(ChatColor.GOLD+"--Mine It--");
            player.sendMessage(ChatColor.GOLD+"/mineit create [name]");
            player.sendMessage(ChatColor.GOLD+"/mineit remove [name]");
            player.sendMessage(ChatColor.GOLD+"/mineit start [name]");
            player.sendMessage(ChatColor.GOLD+"/mineit stop [name]");
            player.sendMessage(ChatColor.GOLD+"/mineit edit mine [name]");
            player.sendMessage(ChatColor.GOLD+"/mineit edit stagelimit [name] [stage number] [limit blocks number]");
            player.sendMessage(ChatColor.GOLD+"/mineit edit time [name] [time]" + ChatColor.GREEN + ": it changes the time that must pass to change to the next stage");
            player.sendMessage(ChatColor.GOLD+"/mineit reset [name]" + ChatColor.GREEN + ": it sets all the mine's block to " + Mine.STATE_ZERO.toString().toLowerCase());
            return true;
        }
        if(args[0].equalsIgnoreCase("create")) {
            if(!player.hasPermission("mineit.create")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return true;
            }
            if(!MineIt.instance.selectedBlocks.containsKey(player.getName()) || MineIt.instance.selectedBlocks.get(player.getName()).size()==0) {
                player.sendMessage(MineIt.errorPrefix +"Please, select the mine's blocks first.");
                return true;
            }
            if(args.length!=2) {
                player.sendMessage(MineIt.errorPrefix +"Command error, use /mineit create [name].");
                return true;
            }
            if(Mine.getMinesLength()>=45) {
                player.sendMessage(MineIt.errorPrefix +"You've reached the current mines limit!");
                return true;
            }
            if(Mine.getMine(args[1]) != null) {
                player.sendMessage(MineIt.errorPrefix +"There's already a mine named '"+args[1]+"'.");
                return true;
            }

            ArrayList<Location> locations = new ArrayList<>();
            for(Location loc : MineIt.instance.selectedBlocks.get(player.getName())) {
                locations.add(loc);
                loc.getBlock().setType(Mine.STATE_ZERO);
            }
            Mine m = new Mine(args[1], locations);
            Mine.addMine(m);
            MineIt.instance.selectedBlocks.remove(player.getName());

            player.sendMessage(MineIt.clearPrefix+ChatColor.GREEN+"Mine created successfully.");
            player.sendMessage(MineIt.clearPrefix+ChatColor.RED+"The mine it's stopped. Configure it with " + ChatColor.GREEN + "/mineit edit mine " + args[1] + ChatColor.RED + " and then enable it with " + ChatColor.GREEN + "/mineit start " + args[1]);
            return true;
        }
        if(args[0].equalsIgnoreCase("remove")) {
            if(!player.hasPermission("mineit.remove")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return true;
            }
            if(args.length!=2) {
                player.sendMessage(MineIt.errorPrefix +"Command error, use /mineit create [name].");
                return true;
            }
            Mine m = Mine.getMine(args[1]);
            if(m == null) {
                player.sendMessage(MineIt.errorPrefix +"The mine '"+args[1]+"' doesn't exist.");
                return true;
            }

            Mine.removeMine(m);
            try {
                File f = new File(MineIt.instance.getDataFolder(), args[1] + ".yml");
                if (f.exists()) f.delete();
            }
            catch (Exception e) { e.printStackTrace(); }
            player.sendMessage(MineIt.clearPrefix+"Mine '"+args[1]+"' removed.");
            return true;
        }
        if (args[0].equalsIgnoreCase("start")) {
            if(!player.hasPermission("mineit.start")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return true;
            }
            if(args.length!=2) {
                player.sendMessage(MineIt.errorPrefix +"Command error, use /mineit start [name].");
                return true;
            }
            Mine m = Mine.getMine(args[1]);
            if(m == null) {
                player.sendMessage(MineIt.errorPrefix +"The mine '"+args[1]+"' doesn't exist.");
                return true;
            }

            m.setStart(true);
            player.sendMessage(MineIt.clearPrefix+"Mine "+args[1]+" started.");
            return true;
        }
        if (args[0].equalsIgnoreCase("stop")) {
            if(!player.hasPermission("mineit.stop")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return true;
            }
            if(args.length!=2) {
                player.sendMessage(MineIt.errorPrefix +"Command error, use /mineit stop [name].");
                return true;
            }
            Mine m = Mine.getMine(args[1]);
            if(m == null) {
                player.sendMessage(MineIt.errorPrefix +"The mine '"+args[1]+"' doesn't exist.");
                return true;
            }

            m.setStart(false);
            player.sendMessage(MineIt.clearPrefix+"Mine "+args[1]+" stopped.");
            return true;
        }
        if(args[0].equalsIgnoreCase("edit")) {
            if (args.length == 1) {
                player.sendMessage(MineIt.errorPrefix +"Invalid syntax, use /mineit ?");
                return true;
            }

            if(args[1].equalsIgnoreCase("mine")) {
                if(args.length!=3) {
                    player.sendMessage(MineIt.errorPrefix +"Use /mineit edit mine [name]");
                    return true;
                }
                if(!player.hasPermission("mineit.open")) {
                    player.closeInventory();
                    player.sendMessage(MineIt.errorPrefix +"You can't use MineIt menus.");
                    return true;
                }

                Mine m = Mine.getMine(args[2]);
                if (m == null) {
                    player.sendMessage(MineIt.errorPrefix +"Mine '"+args[2]+"' not found.");
                    return true;
                }

                // TODO
                //MineIt.instance.edintingMine(player, m);
            }
            else if (args[1].equalsIgnoreCase("time")) {
                if (args.length != 4) {
                    player.sendMessage(MineIt.errorPrefix + "Command error, use /mineit edit time [name] [time].");
                    player.sendMessage(MineIt.clearPrefix + "Ex. /mineit edit time Gold 5");
                    return true;
                }
                if (!player.hasPermission("mineit.time")) {
                    player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                    return true;
                }

                Mine m = Mine.getMine(args[2]);
                if (m == null) {
                    player.sendMessage(MineIt.errorPrefix + "Mine '" + args[2] + "' not found.");
                    return true;
                }

                int time;
                try {
                    time = Integer.parseInt(args[3]);
                    if (time < 1) {
                        player.sendMessage(MineIt.errorPrefix + "The time can't be lower to 1.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(MineIt.errorPrefix + "'" + args[3] + "' is not a number!");
                    return true;
                }

                m.setDelay(time);
                player.sendMessage(MineIt.clearPrefix + "Set " + args[2] + "'s time to " + args[3] + ".");
                return true;
            }
            else if (args[1].equalsIgnoreCase("stagelimit")) {
                if (args.length != 5) {
                    player.sendMessage(MineIt.errorPrefix + "Command error, use /mineit edit stagelimit [name] [stage number] [limit blocks number].");
                    player.sendMessage(MineIt.clearPrefix + "Ex. /mineit edit stagelimit Gold 2 30");
                    return true;
                }
                if (!player.hasPermission("mineit.stagelimit")) {
                    player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                    return true;
                }

                Mine m = Mine.getMine(args[2]);
                if (m == null) {
                    player.sendMessage(MineIt.errorPrefix + "Mine '" + args[2] + "' not found.");
                    return true;
                }

                int num;
                int lim;
                try {
                    num = Integer.parseInt(args[3]) - 1;
                    if (num <= 0) {
                        player.sendMessage(MineIt.errorPrefix + "The stage number can't be lower to 1.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(MineIt.errorPrefix + "'" + args[3] + "' is not a number!");
                    return true;
                }
                try {
                    lim = Integer.parseInt(args[4]);
                    if (lim < 0) {
                        player.sendMessage(MineIt.errorPrefix + "The limit number can't be lower to 0.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(MineIt.errorPrefix + "'" + args[4] + "' is not a number!");
                    return true;
                }
                if (m.getStages().size() <= num) {
                    player.sendMessage(MineIt.errorPrefix + "There's only " + m.getStages().size() + " stages!");
                    return true;
                }

                m.getStages().get(num).setStageLimit(lim);
                player.sendMessage(MineIt.clearPrefix + "Set " + args[2] + "'s stage " + args[3] + " limit to " + args[4] + ".");
            }
            return true;
        }
        if(args[0].equalsIgnoreCase("reset")) {
            if(args.length!=2) {
                player.sendMessage(MineIt.errorPrefix +"Use /mineit reset [name]");
                return true;
            }
            if(!player.hasPermission("mineit.reset")) {
                player.sendMessage(MineIt.errorPrefix +"You can't reset mines!");
                return true;
            }

            Mine m = Mine.getMine(args[1]);
            if (m == null) {
                player.sendMessage(MineIt.errorPrefix +"Mine '"+args[1]+"' not found.");
                return true;
            }

            m.resetBlocksMine();
            return true;
        }

        player.sendMessage(MineIt.errorPrefix +"Use "+ChatColor.GOLD+"/mineit ?"+ChatColor.RED+".");
        return true;
    }
}
