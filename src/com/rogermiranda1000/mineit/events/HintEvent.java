package com.rogermiranda1000.mineit.events;

import com.rogermiranda1000.mineit.CustomCommand;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class HintEvent implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("mineit")) return null;

        String []rawCmd = (String[]) ArrayUtils.add(args, 0, "mineit");
        Set<String> hints = new HashSet<>(); // a set doesn't allow duplicates
        for (CustomCommand cmd : CommandEvent.commands) hints.addAll(cmd.candidate(rawCmd));
        return new ArrayList<>(hints);
    }
}
