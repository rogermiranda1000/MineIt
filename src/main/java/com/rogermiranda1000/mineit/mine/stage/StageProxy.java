package com.rogermiranda1000.mineit.mine.stage;

import com.rogermiranda1000.mineit.mine.Mine;
import org.bukkit.Location;

/**
 * Hides the Stage; making it possible to have it as unknown at the beginning.
 */
public class StageProxy implements StageProvider {
    private StageProvider stageProvider;

    public StageProxy(Location stageBlockLoc, Mine m) {
        this.stageProvider = new UncachedStage(stageBlockLoc, m);
    }

    public void setStage(Stage s) {
        this.stageProvider = s;
    }

    @Override
    public Stage getStage() {
        Stage stage = this.stageProvider.getStage();
        if (stage != null) {
            // the first call will be uncached, so we'll save the result
            this.setStage(stage);
        }
        return stage;
    }
}
