package com.rogermiranda1000.mineit;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CustomCommand {
    public enum CustomCommandReturns {
        MATCH,
        NO_MATCH,
        NO_PERMISSIONS,
        /**
         * executed by the console while consoleUsage being off
         */
        NO_PLAYER,
        /**
         * nº of arguments less than argLength
         */
        INVALID_LENGTH
    }

    private final Pattern commandPattern;
    @Nullable private final String permission;
    @NotNull private final String usage;
    private final String[] partialUsage;
    @Nullable private final String description;
    public final MatchCommandNotifier notifier;
    private final byte argLength;
    /**
     * It returns if the command can be used by the console (true), or not (false)
     */
    private final boolean consoleUsage;


    /**
     * Constructor
     * @param command Regex to match the command ($ and ^ will be added after; do not add it)
     * @param argLength Nº of arguments (/!\ the first string it's not an argument)
     * @param permission Permission to run the command (if needed)
     * @param consoleUsage Can the command be used by the console?
     * @param description Command show + description. If null it takes the command itself
     */
    public CustomCommand(String command, int argLength, @Nullable String permission, boolean consoleUsage, @Nullable String usage, @Nullable String description, MatchCommandNotifier notifier) throws PatternSyntaxException {
        this.commandPattern = Pattern.compile(command); // .matches() adds '^', '$'
        this.permission = permission;
        this.usage = (usage == null) ? command : usage;
        this.partialUsage = this.usage.substring(1).split(" ");
        this.description = description;
        this.notifier = notifier;
        this.consoleUsage = consoleUsage;

        this.argLength = (byte) argLength;
    }

    /**
     * Constructor
     * @param command Regex to match the command
     * @param permission Permission to run the command (if needed)
     * @param consoleUsage Can the command be used by the console?
     * @param description Command show + description. If null it takes the command itself
     */
    public CustomCommand(String command, @Nullable String permission, boolean consoleUsage, @Nullable String usage, @Nullable String description, MatchCommandNotifier notifier) throws PatternSyntaxException {
        this(command, StringUtils.countMatches(command, " "), permission, consoleUsage, usage, description, notifier);
    }

    private static String merge(@NotNull String cmd, @NotNull String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(cmd);
        sb.append(' ');
        for (String arg : args) {
            sb.append(arg);
            sb.append(' ');
        }
        sb.setLength(sb.length() - 1); // remove last space
        return sb.toString();
    }

    public CustomCommandReturns search(@Nullable Player player, @NotNull String cmd, @NotNull String[] args) {
        if (!this.commandPattern.matcher(CustomCommand.merge(cmd, args)).matches()) return CustomCommandReturns.NO_MATCH;

        if (args.length != this.argLength) return CustomCommandReturns.INVALID_LENGTH;
        if (player != null) {
            // it's a player
            if (this.permission != null && !player.hasPermission(this.permission)) return CustomCommandReturns.NO_PERMISSIONS;
        }
        else {
            // it's the console
            if (!this.consoleUsage) return CustomCommandReturns.NO_PLAYER;
        }

        return CustomCommandReturns.MATCH;
    }

    /**
     * It returns the portion of this command that fits
     * @param splittedCmd 'mineit ...'.split(" ")
     * @return null if no match, string to append to hints if found
     */
    @Nullable
    public String candidate(String[] splittedCmd) {
        if (splittedCmd.length > this.partialUsage.length) return null; // you have written more than the actual command

        int x;
        for (x = 0; x < splittedCmd.length-1; x++) {
            if (!splittedCmd[x].matches(this.partialUsage[x])) return null;
        }

        // last text is the one that may be recommended
        if (this.partialUsage[x].matches("[a-zA-Z]+")) {
            // text
            if (splittedCmd[x].length() >= this.partialUsage[x].length()) return null; // you have written more than the actual command

            int n = 0;
            while (n < splittedCmd[x].length() && splittedCmd[x].charAt(n) != this.partialUsage[x].charAt(n)) n++;
            if (n != splittedCmd[x].length()) return null; // different character before finishing

            return this.partialUsage[x].substring(n); // get the reminder part
        }
        else {
            // Regex
            // TODO
            //splittedCmd[x].matches(this.partialUsage[x])
            return null;
        }
    }

    @Override
    public String toString() {
        return ChatColor.GOLD + this.usage + ((this.description != null) ? ChatColor.GREEN + ": " + this.description : "");
    }
}
