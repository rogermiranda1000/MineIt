package com.rogermiranda1000.mineit;

public interface MinesChangedEvent {
    void onMinesChanged();
    void onMineRemoved(Mine m);
    void onMineAdded(Mine m);
}
