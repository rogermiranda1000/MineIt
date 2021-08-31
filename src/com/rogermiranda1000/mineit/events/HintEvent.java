package com.rogermiranda1000.mineit.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.List;

public class HintEvent implements Listener {
    @EventHandler
    public void onTabComplete(TabCompleteEvent e) {
        if (!e.getBuffer().matches("^/mineit(:mineit)? ")) return;

        String rawCmd = e.getBuffer().replaceFirst("^/mineit(:mineit)? ", "mineit ");
        /*List<String> hints = e.getCompletions();
        hints.add("AAAAAAAAAAAAAAAAAAAAAAA");*/
    }
}
