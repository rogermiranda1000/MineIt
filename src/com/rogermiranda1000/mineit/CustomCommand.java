package com.rogermiranda1000.mineit;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
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
    @Nullable private Pattern[] partialPattern;
    @Nullable private final String permission;
    @NotNull private final String usage;
    @Nullable private String[] partialUsage;
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

        String []pattern = command.split(" ");
        this.partialPattern = new Pattern[pattern.length];
        for (int x = 0; x < pattern.length; x++) this.partialPattern[x] = Pattern.compile(pattern[x]);
        this.partialUsage = this.usage.split(" ");
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
    public Collection<String> candidate(String[] splittedCmd) {
        Collection<String> r = new ArrayList<>();
        if (this.partialPattern == null || this.partialUsage == null) return r;
        if (splittedCmd.length > this.partialUsage.length) return r; // you have written more than the actual command

        int x;
        for (x = 0; x < splittedCmd.length-1; x++) {
            if (!this.partialPattern[x].matcher(splittedCmd[x]).matches()) return r;
        }

        // last text is the one that should be recommended
        if (!this.partialUsage[x].startsWith("[")) {
            // literal text
            if (CustomCommand.partiallyMatches(splittedCmd[x], this.partialUsage[x])) r.add(this.partialUsage[x]);
            return r;
        }
        else {
            // special text ("[something]")
            if (this.partialUsage[x].equalsIgnoreCase("[mine]")) {
                // add all matching mines
                for (Mine m : Mine.getMines()) {
                    String name = m.getName();
                    if (CustomCommand.partiallyMatches(splittedCmd[x], name)) r.add(name);
                }
            }
            return r;
        }
    }

    private static boolean partiallyMatches(String s1, String s2) {
        if (s1.length() >= s2.length()) return false; // you have written more than the actual command

        int n = 0;
        while (n < s1.length() && s1.charAt(n) == s2.charAt(n)) n++;
        return (n == s1.length()); // false -> different character before finishing
    }

    @Override
    public String toString() {
        return ChatColor.GOLD + "/" + this.usage + ((this.description != null) ? ChatColor.GREEN + ": " + this.description : "");
    }
}
