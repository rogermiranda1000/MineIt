package com.rogermiranda1000.mineit.events;

import com.rogermiranda1000.mineit.CustomCommand;
import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.file.FileManager;
import com.rogermiranda1000.mineit.inventories.BasicInventory;
import com.rogermiranda1000.mineit.inventories.SelectMineInventory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CommandEvent implements CommandExecutor {
    private static final ArrayList<String> RESERVED_NAMES = new ArrayList<>();

    static {
        CommandEvent.RESERVED_NAMES.add("config"); // config yml file
        CommandEvent.RESERVED_NAMES.add("all"); // permission conflict
    }

    /**
     * Regex command > Permision to run
     */
    public static final CustomCommand[] commands = {
        new CustomCommand("mineit \\?", null, true, "mineit ?", null, (sender, args) -> {
            sender.sendMessage(ChatColor.GOLD+"--Mine It--");
            for (CustomCommand command : CommandEvent.commands) sender.sendMessage(command.toString());
        }),
        new CustomCommand("mineit", "mineit.open", false, "mineit", null, (sender, cmd) -> {
            MineIt.instance.mainInventory.openInventory((Player)sender);
        }),
        new CustomCommand("mineit tool", "mineit.create", false, "mineit tool", "get the selection tool", (sender, cmd) -> {
            ((Player)sender).getInventory().addItem(MineIt.item);
        }),
        new CustomCommand("mineit list", null, true, "mineit list", "see all the created mines", (sender, cmd) -> {
            StringBuilder sb = new StringBuilder();
            for (Mine m : Mine.getMines()) {
                sb.append(m.getName());
                sb.append(", ");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 2); // remove last ", " (if any)

            sender.sendMessage(MineIt.clearPrefix + "Mine list: " + sb.toString());
        }),
        new CustomCommand("mineit create \\S+", "mineit.create", false, "mineit create [name]", null, (sender, args) -> {
            Player player = (Player) sender; // not for console usage

            if(!MineIt.instance.selectedBlocks.containsKey(player.getName()) || MineIt.instance.selectedBlocks.get(player.getName()).size()==0) {
                player.sendMessage(MineIt.errorPrefix +"Please, select the mine's blocks first.");
                return;
            }
            if(Mine.getMinesLength()>=45) {
                player.sendMessage(MineIt.errorPrefix +"You've reached the current mines limit!");
                return;
            }
            if (CommandEvent.RESERVED_NAMES.contains(args[1])) {
                player.sendMessage(MineIt.errorPrefix +"You're using a reserved name!");
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

            TextComponent edit = new TextComponent(ChatColor.GREEN + "/mineit edit mine " + args[1]);
            edit.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mineit edit mine " + args[1]));
            TextComponent enable = new TextComponent(ChatColor.GREEN + "/mineit start " + args[1]);
            enable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mineit start " + args[1]));
            try {
                Class.forName("net.md_5.bungee.api.chat.hover.content.Content");
                edit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.AQUA + "Click to run the command")));
                enable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.AQUA + "Click to run the command")));
            } catch (ClassNotFoundException ignored) {}
            player.spigot().sendMessage(new TextComponent(MineIt.clearPrefix+ChatColor.RED+"The mine it's stopped. Configure it with "), edit, new TextComponent(ChatColor.RED + " and then enable it with "), enable);
        }),
        new CustomCommand("mineit remove \\S+", "mineit.remove", true, "mineit remove [mine]", null, (sender, cmd) -> {
            Mine m = Mine.getMine(cmd[1]);
            if(m == null) {
                sender.sendMessage(MineIt.errorPrefix +"The mine '"+cmd[1]+"' doesn't exist.");
                return;
            }

            Mine.removeMine(m);
            try {
                FileManager.removeMine(m);
            } catch (Exception ignored) {}
            sender.sendMessage(MineIt.clearPrefix+"Mine '"+cmd[1]+"' removed.");
        }),
        new CustomCommand("mineit start \\S+", "mineit.state", true, "mineit start [mine]", null, (sender, cmd) -> {
            Mine m = Mine.getMine(cmd[1]);
            if(m == null) {
                sender.sendMessage(MineIt.errorPrefix +"The mine '"+cmd[1]+"' doesn't exist.");
                return;
            }

            m.setStart(true);
            sender.sendMessage(MineIt.clearPrefix + "Mine '" + cmd[1] + "' started.");
        }),
        new CustomCommand("mineit stop \\S+", "mineit.state", true, "mineit stop [mine]", null, (sender, cmd) -> {
            Mine m = Mine.getMine(cmd[1]);
            if(m == null) {
                sender.sendMessage(MineIt.errorPrefix +"The mine '"+cmd[1]+"' doesn't exist.");
                return;
            }

            m.setStart(false);
            sender.sendMessage(MineIt.clearPrefix + "Mine '" + cmd[1] + "' stopped.");
        }),
        new CustomCommand("mineit edit mine \\S+", "mineit.open", false, "mineit edit mine [mine]", null, (sender, cmd) -> {
            BasicInventory mineInv = ((SelectMineInventory)MineIt.instance.selectMineInventory).searchMine(cmd[2]);
            if (mineInv == null) {
                sender.sendMessage(MineIt.errorPrefix +"Mine '"+cmd[2]+"' not found.");
                return;
            }

            mineInv.openInventory((Player)sender);
        }),
        new CustomCommand("mineit edit time \\S+ \\d+", "mineit.time", true, "mineit edit time [mine] [time]", "it changes the time that must pass to change to the next stage", (sender, cmd) -> {
            Mine m = Mine.getMine(cmd[2]);
            if (m == null) {
                sender.sendMessage(MineIt.errorPrefix + "Mine '" + cmd[2] + "' not found.");
                return;
            }

            int time;
            try {
                time = Integer.parseInt(cmd[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(MineIt.errorPrefix + "'" + cmd[3] + "' is not a number!");
                return;
            }
            if (time < 1) {
                sender.sendMessage(MineIt.errorPrefix + "The time can't be lower to 1.");
                return;
            }

            m.setDelay(time);
            sender.sendMessage(MineIt.clearPrefix + "Set " + cmd[2] + "'s time to " + cmd[3] + ".");
        }),
        new CustomCommand("mineit edit stagelimit \\S+ \\d+ \\d+", "mineit.stagelimit", true, "mineit edit stagelimit [mine] [stage_number] [limit_blocks_number]", null, (sender, cmd) -> {
            Mine m = Mine.getMine(cmd[2]);
            if (m == null) {
                sender.sendMessage(MineIt.errorPrefix + "Mine '" + cmd[2] + "' not found.");
                return;
            }

            int num;
            int lim;
            try {
                num = Integer.parseInt(cmd[3]) - 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(MineIt.errorPrefix + "'" + cmd[3] + "' is not a number!");
                return;
            }
            if (num <= 0) {
                sender.sendMessage(MineIt.errorPrefix + "The stage number can't be lower to 1.");
                return;
            }

            try {
                lim = Integer.parseInt(cmd[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(MineIt.errorPrefix + "'" + cmd[4] + "' is not a number!");
                return;
            }
            if (lim < 0) {
                sender.sendMessage(MineIt.errorPrefix + "The limit number can't be lower to 0.");
                return;
            }

            if (m.getStageCount() <= num) {
                sender.sendMessage(MineIt.errorPrefix + "There's only " + m.getStageCount() + " stages!");
                return;
            }

            m.setStageLimit(num, lim);
            sender.sendMessage(MineIt.clearPrefix + "Set " + cmd[2] + "'s stage " + cmd[3] + " limit to " + cmd[4] + ".");
        }),
        new CustomCommand("mineit reset \\S+", "mineit.reset", true, "mineit reset [mine]", "it sets all the mine's block to " + Mine.STATE_ZERO.toString().toLowerCase(), (sender, cmd) -> {
            Mine m = Mine.getMine(cmd[1]);
            if (m == null) {
                sender.sendMessage(MineIt.errorPrefix +"Mine '"+cmd[1]+"' not found.");
                return;
            }

            m.resetBlocksMine();
            sender.sendMessage(MineIt.clearPrefix + "Mine '" + cmd[1] + "' restarted.");
        }),
        new CustomCommand("mineit select unselect", "mineit.select", false, "mineit select unselect", "unselects all the selected blocks by the user", (sender, cmd) -> {
            ArrayList<Location> r = MineIt.instance.selectedBlocks.remove(((Player)sender).getName());
            if (r == null) {
                sender.sendMessage(MineIt.errorPrefix + "First you need to have some blocks selected!");
                return;
            }

            for (Location loc : r) loc.getBlock().setType(Mine.SELECT_BLOCK); // unselect
            sender.sendMessage(MineIt.clearPrefix + "Selected blocks restarted.");
        })
    };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        // TODO back
        // TODO unselect on leave
        // TODO select a single block
        // TODO append to existing mine

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
    }
}
