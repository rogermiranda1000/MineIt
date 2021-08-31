package com.rogermiranda1000.mineit.events;

import com.rogermiranda1000.mineit.CustomCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.List;

public class HintEvent implements Listener {
    @EventHandler
    public void onTabComplete(TabCompleteEvent e) {
        if (!e.getBuffer().matches("^/mineit(:mineit)? ")) return;

        String []rawCmd = e.getBuffer().replaceFirst("^/mineit(:mineit)? ", "mineit ").split(" ");
        List<String> hints = e.getCompletions();
        hints.clear(); // mineit commands doesn't use players
        for (CustomCommand cmd : CommandEvent.commands) {
            String match = cmd.candidate(rawCmd);
            if (match != null) hints.add(match);
        }
    }
}
