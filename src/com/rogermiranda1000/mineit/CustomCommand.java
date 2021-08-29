package com.rogermiranda1000.mineit;

import org.apache.commons.lang.StringUtils;
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

    private final Pattern command;
    @Nullable private final String permission;
    @NotNull private final String description;
    public final MatchCommandNotifier notifier;
    private final byte argLength;
    private final boolean consoleUsage;


    /**
     * Constructor
     * @param command Regex to match the command ($ and ^ will be added after; do not add it)
     * @param argLength Nº of arguments (/!\ the first string it's not an argument)
     * @param permission Permission to run the command (if needed)
     * @param consoleUsage Can the command be used by the console?
     * @param description Command show + description. If null it takes the command itself
     */
    public CustomCommand(String command, int argLength, @Nullable String permission, boolean consoleUsage, @Nullable String description, MatchCommandNotifier notifier) throws PatternSyntaxException {
        this.command = Pattern.compile("^" + command + "$");
        this.permission = permission;
        this.description = (description == null) ? command : description;
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
    public CustomCommand(String command, @Nullable String permission, boolean consoleUsage, @Nullable String description, MatchCommandNotifier notifier) throws PatternSyntaxException {
        this(command, StringUtils.countMatches(command, " "), permission, consoleUsage, description, notifier);
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
        if (!this.command.matcher(CustomCommand.merge(cmd, args)).matches()) return CustomCommandReturns.NO_MATCH;

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

    @Nullable
    public String getPermission() {
        return this.permission;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * It returns if the command can be used by the console
     * @return Used by the console (true), or not (false)
     */
    public boolean getConsoleUsage() {
        return this.consoleUsage;
    }
}
