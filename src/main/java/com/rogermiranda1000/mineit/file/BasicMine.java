package com.rogermiranda1000.mineit.file;

import com.rogermiranda1000.helper.blocks.file.BasicLocation;
import com.rogermiranda1000.mineit.mine.Mine;
import com.rogermiranda1000.mineit.mine.stage.Stage;
import com.rogermiranda1000.mineit.mine.blocks.Mines;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;

public class BasicMine {
    private final ArrayList<BasicStage> stages;
    private final String mineName;
    private final String identifier;
    private final boolean started;
    private final int delay;
    @Nullable private final BasicLocation tp;

    public BasicMine(Mine mine) {
        this.mineName = mine.getName();
        this.identifier = mine.getMineBlockIdentifier().getName();
        this.started = mine.isStarted();
        this.delay = mine.getDelay();
        this.tp = (mine.getTp() == null) ? null : new BasicLocation(mine.getTp());

        this.stages = new ArrayList<>();
        for (Stage s : mine.getStages()) this.stages.add(new BasicStage(s));
    }

    public Mine getMine() throws IOException {
        return new Mine(Mines.getInstance(), this.mineName, this.identifier, this.started, BasicStage.getStages(this.stages), this.delay, (this.tp == null) ? null : this.tp.getLocation());
    }
}
