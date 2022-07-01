package com.rogermiranda1000.mineit;

public class ListenerNotFoundException extends RuntimeException {
    public ListenerNotFoundException() {
        super("Listener not found");
    }

    public ListenerNotFoundException(String str) {
        super(str);
    }
}
