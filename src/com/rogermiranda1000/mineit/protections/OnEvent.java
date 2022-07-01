package com.rogermiranda1000.mineit.protections;

import org.bukkit.event.Event;

public interface OnEvent {
    /**
     * @retval TRUE     An error has occurred
     * @retval FALSE    All ok
     */
    public boolean onEvent(Event e);
}
