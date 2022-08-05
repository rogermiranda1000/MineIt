package com.rogermiranda1000.mineit;

import com.rogermiranda1000.helper.BasicInventory;
import com.rogermiranda1000.helper.CustomCommand;
import com.rogermiranda1000.helper.MatchCommandNotifier;
import com.rogermiranda1000.mineit.blocks.Mines;
import com.rogermiranda1000.mineit.blocks.SelectedBlocks;
import com.rogermiranda1000.mineit.file.FileManager;
import com.rogermiranda1000.mineit.inventories.SelectMineInventory;
import com.rogermiranda1000.versioncontroller.VersionController;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.PatternSyntaxException;

public class CustomMineItCommand extends CustomCommand {
    public CustomMineItCommand(String command, int argLength, @Nullable String permission, boolean consoleUsage, @Nullable String usage, @Nullable String description, MatchCommandNotifier notifier) throws PatternSyntaxException {
        super(command, argLength, permission, consoleUsage, usage, description, notifier);
    }

    public CustomMineItCommand(String command, @Nullable String permission, boolean consoleUsage, @Nullable String usage, @Nullable String description, MatchCommandNotifier notifier) throws PatternSyntaxException {
        super(command, permission, consoleUsage, usage, description, notifier);
    }

    @Override
    public void searchSpecialText(Collection<String> results, String[] splittedCmd, String[] partialUsage, int index) {
        if (partialUsage[index].equalsIgnoreCase("[mine]")) {
            // add all matching mines
            for (Mine m : Mines.getInstance().getAllValues()) {
                String name = m.getName();
                if (CustomCommand.partiallyMatches(splittedCmd[index], name)) results.add(name);
            }
        }
    }

    private static final ArrayList<String> RESERVED_NAMES = new ArrayList<>();

    static {
        CustomMineItCommand.RESERVED_NAMES.add("config"); // config yml file
        CustomMineItCommand.RESERVED_NAMES.add("all"); // permission conflict
    }

    public static final CustomMineItCommand []commands = {
            new CustomMineItCommand("mineit \\?", null, true, "mineit ?", null, (sender, args) -> {
                sender.sendMessage(ChatColor.GOLD+"--Mine It--");
                for (CustomMineItCommand command : CustomMineItCommand.commands) sender.sendMessage(command.toString());
            }),
            new CustomMineItCommand("mineit", "mineit.open", false, "mineit", null, (sender, cmd) -> {
                MineIt.instance.mainInventory.openInventory((Player)sender);
            }),
            new CustomMineItCommand("mineit tool", "mineit.create", false, "mineit tool", "get the selection tool", (sender, cmd) -> {
                ((Player)sender).getInventory().addItem(MineIt.item);
            }),
            new CustomMineItCommand("mineit unbreakable", "mineit.create", false, "mineit unbreakable", "set the current block as unbreakable, so the stage won't be mined", (sender, cmd) -> {
                Player player = (Player) sender; // not for console usage
                ItemStack[]hand = VersionController.get().getItemInHand(player);
                if (hand[0].getType() == Material.AIR || !hand[0].getType().isBlock()) {
                    player.sendMessage(MineIt.instance.errorPrefix +"You need to hold a block while using this command.");
                    return;
                }

                ItemMeta meta = hand[0].getItemMeta();
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                hand[0].setItemMeta(meta);

                player.sendMessage(MineIt.instance.clearPrefix + "Now use this block to create an unbreakable stage.");
            }),
            new CustomMineItCommand("mineit air", "mineit.create", false, "mineit air", "get the air stage", (sender, cmd) -> {
                Player player = (Player) sender; // not for console usage

                ItemStack air = new ItemStack(Mine.AIR_STAGE);

                // air is unbreakable
                ItemMeta meta = air.getItemMeta();
                meta.setDisplayName("Air");
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                air.setItemMeta(meta);

                // give item
                player.getInventory().addItem(air);
                player.sendMessage(MineIt.instance.clearPrefix + "Now use this block to create an air stage.");
            }),
            new CustomMineItCommand("mineit mimic", "mineit.create", false, "mineit mimic", "get the mimic block", (sender, cmd) -> {
                Player player = (Player) sender; // not for console usage

                // give item
                player.getInventory().addItem(MineIt.mimicBlock.clone());
                player.sendMessage(MineIt.instance.clearPrefix + "Now use this block to mimic the desired stage block.");
            }),
            new CustomMineItCommand("mineit list", null, true, "mineit list", "see all the created mines", (sender, cmd) -> {
                StringBuilder sb = new StringBuilder();
                for (Mine m : Mines.getInstance().getAllValues()) {
                    sb.append(m.getName());
                    sb.append(", ");
                }
                if (sb.length() > 0) sb.setLength(sb.length() - 2); // remove last ", " (if any)

                sender.sendMessage(MineIt.instance.clearPrefix + "Mine list: " + sb.toString());
            }),
            new CustomMineItCommand("mineit create \\S+", "mineit.create", false, "mineit create [name]", null, (sender, args) -> {
                Player player = (Player) sender; // not for console usage

                if (CustomMineItCommand.RESERVED_NAMES.contains(args[1])) {
                    player.sendMessage(MineIt.instance.errorPrefix +"You're using a reserved name!");
                    return;
                }
                if(Mines.getInstance().getMine(args[1]) != null) {
                    player.sendMessage(MineIt.instance.errorPrefix +"There's already a mine named '"+args[1]+"'.");
                    return;
                }

                final ArrayList<Location> locations = new ArrayList<>();
                SelectedBlocks.getInstance().getAllBlocksByValue(player, e -> locations.add(e.getValue()));
                if (locations.size() == 0) {
                    player.sendMessage(MineIt.instance.errorPrefix +"Please, select the mine's blocks first.");
                    return;
                }

                Mine m = new Mine(Mines.getInstance(), args[1], locations);
                Mines.getInstance().addMine(m);
                SelectedBlocks.getInstance().removeBlocksArtificiallyByValue(player);
                locations.forEach(l -> l.getBlock().setType(Mine.STATE_ZERO));

                player.sendMessage(MineIt.instance.clearPrefix + "Mine created successfully.");

                TextComponent edit = new TextComponent(ChatColor.GREEN + "/mineit edit mine " + args[1]);
                edit.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mineit edit mine " + args[1]));
                TextComponent enable = new TextComponent(ChatColor.GREEN + "/mineit start " + args[1]);
                enable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mineit start " + args[1]));
                try {
                    Class.forName("net.md_5.bungee.api.chat.hover.content.Content");
                    edit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.AQUA + "Click to run the command")));
                    enable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.AQUA + "Click to run the command")));
                } catch (ClassNotFoundException ignored) {}
                player.spigot().sendMessage(new TextComponent(MineIt.instance.clearPrefix+ChatColor.RED+"The mine it's stopped. Configure it with "), edit, new TextComponent(ChatColor.RED + " and then enable it with "), enable);
            }),
            new CustomMineItCommand("mineit remove \\S+", "mineit.remove", true, "mineit remove [mine]", null, (sender, cmd) -> {
                Mine m = Mines.getInstance().getMine(cmd[1]);
                if(m == null) {
                    sender.sendMessage(MineIt.instance.errorPrefix +"The mine '"+cmd[1]+"' doesn't exist.");
                    return;
                }

                Mines.getInstance().removeMine(m);
                try {
                    FileManager.removeMine(m);
                } catch (Exception ignored) {}
                sender.sendMessage(MineIt.instance.clearPrefix+"Mine '"+cmd[1]+"' removed.");
            }),
            new CustomMineItCommand("mineit start \\S+", "mineit.state", true, "mineit start [mine]", null, (sender, cmd) -> {
                Mine m = Mines.getInstance().getMine(cmd[1]);
                if(m == null) {
                    sender.sendMessage(MineIt.instance.errorPrefix +"The mine '"+cmd[1]+"' doesn't exist.");
                    return;
                }

                m.setStart(true);
                sender.sendMessage(MineIt.instance.clearPrefix + "Mine '" + cmd[1] + "' started.");
            }),
            new CustomMineItCommand("mineit stop \\S+", "mineit.state", true, "mineit stop [mine]", null, (sender, cmd) -> {
                Mine m = Mines.getInstance().getMine(cmd[1]);
                if(m == null) {
                    sender.sendMessage(MineIt.instance.errorPrefix +"The mine '"+cmd[1]+"' doesn't exist.");
                    return;
                }

                m.setStart(false);
                sender.sendMessage(MineIt.instance.clearPrefix + "Mine '" + cmd[1] + "' stopped.");
            }),
            new CustomMineItCommand("mineit edit mine \\S+", "mineit.open", false, "mineit edit mine [mine]", null, (sender, cmd) -> {
                BasicInventory mineInv = ((SelectMineInventory)MineIt.instance.selectMineInventory).searchMine(cmd[2]);
                if (mineInv == null) {
                    sender.sendMessage(MineIt.instance.errorPrefix +"Mine '"+cmd[2]+"' not found.");
                    return;
                }

                mineInv.openInventory((Player)sender);
            }),
            new CustomMineItCommand("mineit edit time \\S+ \\d+", "mineit.time", true, "mineit edit time [mine] [time]", "it changes the time that must pass to change to the next stage", (sender, cmd) -> {
                Mine m = Mines.getInstance().getMine(cmd[2]);
                if (m == null) {
                    sender.sendMessage(MineIt.instance.errorPrefix + "Mine '" + cmd[2] + "' not found.");
                    return;
                }

                int time;
                try {
                    time = Integer.parseInt(cmd[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(MineIt.instance.errorPrefix + "'" + cmd[3] + "' is not a number!");
                    return;
                }
                if (time < 1) {
                    sender.sendMessage(MineIt.instance.errorPrefix + "The time can't be lower to 1.");
                    return;
                }

                m.setDelay(time);
                sender.sendMessage(MineIt.instance.clearPrefix + "Set " + cmd[2] + "'s time to " + cmd[3] + ".");
            }),
            new CustomMineItCommand("mineit edit stagelimit \\S+ \\d+ \\d+", "mineit.stagelimit", true, "mineit edit stagelimit [mine] [stage_number] [limit_blocks_number]", null, (sender, cmd) -> {
                Mine m = Mines.getInstance().getMine(cmd[2]);
                if (m == null) {
                    sender.sendMessage(MineIt.instance.errorPrefix + "Mine '" + cmd[2] + "' not found.");
                    return;
                }

                int num;
                int lim;
                try {
                    num = Integer.parseInt(cmd[3]) - 1;
                } catch (NumberFormatException e) {
                    sender.sendMessage(MineIt.instance.errorPrefix + "'" + cmd[3] + "' is not a number!");
                    return;
                }
                if (num <= 0) {
                    sender.sendMessage(MineIt.instance.errorPrefix + "The stage number can't be lower to 1.");
                    return;
                }

                try {
                    lim = Integer.parseInt(cmd[4]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(MineIt.instance.errorPrefix + "'" + cmd[4] + "' is not a number!");
                    return;
                }
                if (lim < 0) {
                    sender.sendMessage(MineIt.instance.errorPrefix + "The limit number can't be lower to 0.");
                    return;
                }

                if (m.getStageCount() <= num) {
                    sender.sendMessage(MineIt.instance.errorPrefix + "There's only " + m.getStageCount() + " stages!");
                    return;
                }

                m.setStageLimit(num, lim);
                sender.sendMessage(MineIt.instance.clearPrefix + "Set " + cmd[2] + "'s stage " + cmd[3] + " limit to " + cmd[4] + ".");
            }),
            new CustomMineItCommand("mineit edit tp \\S+", "mineit.tpset", false, "mineit edit tp [mine]", "it stablishes the current location as the tp to the mine", (sender, cmd) -> {
                Mine m = Mines.getInstance().getMine(cmd[2]);
                if (m == null) {
                    sender.sendMessage(MineIt.instance.errorPrefix + "Mine '" + cmd[2] + "' not found.");
                    return;
                }

                m.setTp(((Player)sender).getLocation());
                sender.sendMessage(MineIt.instance.clearPrefix + "Set " + cmd[2] + "'s teleport position to yours.");
            }),
            new CustomMineItCommand("mineit reset \\S+", "mineit.reset", true, "mineit reset [mine]", "it sets all the mine's block to " + Mine.STATE_ZERO.toString().toLowerCase(), (sender, cmd) -> {
                Mine m = Mines.getInstance().getMine(cmd[1]);
                if (m == null) {
                    sender.sendMessage(MineIt.instance.errorPrefix +"Mine '"+cmd[1]+"' not found.");
                    return;
                }

                m.resetBlocksMine();
                sender.sendMessage(MineIt.instance.clearPrefix + "Mine '" + cmd[1] + "' restarted.");
            }),
            new CustomMineItCommand("mineit select unselect", "mineit.select", false, "mineit select unselect", "unselects all the selected blocks by the user", (sender, cmd) -> {
                ArrayList<Location> r = new ArrayList<>();
                SelectedBlocks.getInstance().removeBlocksArtificiallyByValue((Player)sender, (e)->r.add(e.getValue()));
                if (r.size() == 0) {
                    sender.sendMessage(MineIt.instance.errorPrefix + "First you need to have some blocks selected!");
                    return;
                }

                for (Location loc : r) loc.getBlock().setType(Mine.SELECT_BLOCK); // unselect
                sender.sendMessage(MineIt.instance.clearPrefix + "Selected blocks restarted.");
            }),
            new CustomMineItCommand("mineit tp \\S+", "mineit.tp", false, "mineit tp [mine]", "teleport to the mine", (sender, cmd) -> {
                Player p = (Player)sender;
                Mine m = Mines.getInstance().getMine(cmd[1]);
                if (m == null) {
                    p.sendMessage(MineIt.instance.errorPrefix + "Mine '" + cmd[1] + "' not found.");
                    return;
                }
                if (m.getTp() == null) {
                    p.sendMessage(MineIt.instance.errorPrefix + "The mine '" + cmd[1] + "' doesn't have a tp established yet.");
                    return;
                }

                p.teleport(m.getTp());
                p.sendMessage(MineIt.instance.clearPrefix + "Teleporting to mine " + cmd[1] + "...");
            }),
            new CustomMineItCommand("mineit tp", "mineit.tp", false, "mineit tp", "opens the teleport mine menu", (sender, cmd) -> {
                MineIt.instance.tpInventory.openInventory((Player)sender);
            }),
            new CustomMineItCommand("mineit report \\S+ .+", "mineit.report", true, "mineit report [contact] [report]",
                    "Send information about a problem. In the 'contact' zone set your email or discord so I can contact with you (if you don't want to set '-')", (sender, args) -> {
                String contact = args[1];
                if (!contact.equals("-") && !contact.contains("@") && !contact.contains("#")) {
                    sender.sendMessage(MineIt.instance.errorPrefix + "You need to put an email (something@website) or Discord (user#id) to contact. If you don't want to, then set '-'.");
                    return;
                }

                StringBuilder msg = new StringBuilder();
                for (int n = 2; n < args.length; n++) msg.append(args[n]).append(' ');
                msg.setLength(msg.length()-1); // remove last ' '

                MineIt.instance.userReport(contact.equals("-") ? null : contact, (sender instanceof Player) ? ((Player)sender).getName() : null, msg.toString());
                sender.sendMessage(MineIt.instance.clearPrefix + "Report sent! Thanks for helping.");
            })
            // TODO implement with new block object
            /*new CustomMineItCommand("mineit select back", "mineit.select", false, "mineit select back", "unselects the previous selected blocks by the user", (sender, cmd) -> {
                ArrayList<Location> r = new ArrayList<>();
                SelectedBlocks.getInstance().removeBlocksArtificiallyByValue((Player)sender, e->r.add(e.getValue()));
                if (r.size() == 0) {
                    sender.sendMessage(MineIt.instance.errorPrefix + "First you need to have some blocks selected!");
                    return;
                }

                for (Location loc : r) loc.getBlock().setType(Mine.SELECT_BLOCK); // unselect
                sender.sendMessage(MineIt.instance.clearPrefix + "Last selected blocks restarted.");
            })*/
    };
}
