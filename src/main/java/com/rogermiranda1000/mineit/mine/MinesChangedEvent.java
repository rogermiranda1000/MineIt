package com.rogermiranda1000.mineit.mine;

import com.rogermiranda1000.mineit.mine.Mine;

public interface MinesChangedEvent {
    void onMinesChanged();
    void onMineRemoved(Mine m);
    void onMineAdded(Mine m);
}
