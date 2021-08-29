package com.rogermiranda1000.mineit.events;

import com.rogermiranda1000.mineit.CustomCommand;
import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.MineIt;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CommandEvent implements CommandExecutor {
    /**
     * Regex command > Permision to run
     */
    public static final CustomCommand[] commands = {
        new CustomCommand("mineit \\?", null, true, ChatColor.GOLD+"/mineit ?", (sender, args) -> {
            sender.sendMessage(ChatColor.GOLD+"--Mine It--");
            for (CustomCommand command : CommandEvent.commands) sender.sendMessage(command.getDescription());
            /*sender.sendMessage(ChatColor.GOLD+"/mineit create [name]");
            sender.sendMessage(ChatColor.GOLD+"/mineit remove [name]");
            sender.sendMessage(ChatColor.GOLD+"/mineit start [name]");
            sender.sendMessage(ChatColor.GOLD+"/mineit stop [name]");
            sender.sendMessage(ChatColor.GOLD+"/mineit edit mine [name]");
            sender.sendMessage(ChatColor.GOLD+"/mineit edit stagelimit [name] [stage number] [limit blocks number]");
            sender.sendMessage(ChatColor.GOLD+"/mineit edit time [name] [time]" + ChatColor.GREEN + ": it changes the time that must pass to change to the next stage");
            sender.sendMessage(ChatColor.GOLD+"/mineit reset [name]" + ChatColor.GREEN + ": it sets all the mine's block to " + Mine.STATE_ZERO.toString().toLowerCase());*/
        }),
        new CustomCommand("mineit create \\S+", "mineit.create", false, ChatColor.GOLD+"/mineit create [name]", (sender, args) -> {
            Player player = (Player) sender; // not for console usage

            if(!MineIt.instance.selectedBlocks.containsKey(player.getName()) || MineIt.instance.selectedBlocks.get(player.getName()).size()==0) {
                player.sendMessage(MineIt.errorPrefix +"Please, select the mine's blocks first.");
                return;
            }
            if(Mine.getMinesLength()>=45) {
                player.sendMessage(MineIt.errorPrefix +"You've reached the current mines limit!");
                return;
            }
            if(Mine.getMine(args[1]) != null) {
                player.sendMessage(MineIt.errorPrefix +"There's already a mine named '"+args[1]+"'.");
                return;
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
        })
    };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        // TODO unselect
        // TODO append to existing mine
        // TODO mine list

        for (CustomCommand command : CommandEvent.commands) {
            switch (command.search((sender instanceof Player) ? (Player) sender : null, cmd.getName(), args)) {
                case NO_MATCH:
                    continue;

                case NO_PERMISSIONS:
                    sender.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                    break;
                case MATCH:
                    command.notifier.onCommand(sender, args);
                    break;
                case NO_PLAYER:
                    sender.sendMessage("Don't use this command in console.");
                    break;
                case INVALID_LENGTH:
                    sender.sendMessage(MineIt.errorPrefix +"Unknown command. Use " + ChatColor.GOLD + "/mineit ?");
                    break;
                default:
                    MineIt.instance.printConsoleErrorMessage("Unknown response to command");
                    return false;
            }
            return true;
        }

        sender.sendMessage(MineIt.errorPrefix +"Unknown command");
        CommandEvent.commands[0].notifier.onCommand(sender, new String[]{}); // '?' command
        return true;



        /*if(args.length == 0) {
            if(!player.hasPermission("mineit.open")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return true;
            }
            MineIt.instance.mainInventory.openInventory(player);
            return true;
        }

        if(args[0].equalsIgnoreCase("create")) {
            if(!player.hasPermission("mineit.create")) {
                player.sendMessage(MineIt.errorPrefix + "You don't have the permissions to do that.");
                return true;
            }
            if(args.length!=2) {
                player.sendMessage(MineIt.errorPrefix +"Command error, use /mineit create [name].");
                return true;
            }
        }
        else if(args[0].equalsIgnoreCase("remove")) {
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
        }
        else if (args[0].equalsIgnoreCase("start")) {
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
        }
        else if (args[0].equalsIgnoreCase("stop")) {
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
        }
        else if(args[0].equalsIgnoreCase("edit")) {
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

                BasicInventory mineInv = ((SelectMineInventory)MineIt.instance.selectMineInventory).searchMine(args[2]);
                if (mineInv == null) {
                    player.sendMessage(MineIt.errorPrefix +"Mine '"+args[2]+"' not found.");
                    return true;
                }
                mineInv.openInventory(player);
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
                if (m.getStageCount() <= num) {
                    player.sendMessage(MineIt.errorPrefix + "There's only " + m.getStageCount() + " stages!");
                    return true;
                }

                m.setStageLimit(num, lim);
                player.sendMessage(MineIt.clearPrefix + "Set " + args[2] + "'s stage " + args[3] + " limit to " + args[4] + ".");
            }
        }
        else if(args[0].equalsIgnoreCase("reset")) {
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
        }
        else player.sendMessage(MineIt.errorPrefix +"Use "+ChatColor.GOLD+"/mineit ?"+ChatColor.RED+".");
        return true;*/
    }
}
