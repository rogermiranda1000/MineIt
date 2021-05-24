package com.rogermiranda1000.mineit;

import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;

import java.util.ArrayList;

public class Stage {
    private final String name;

    /**
     * Maximum number of simultaneous blocks on that stage
     */
    private int stageLimit;

    private Stage previousStage;

    private Stage nextStage;

    /**
     * Number of current simultaneous blocks on that stage
     */
    private int stageBlocks;

    public Stage(String name, int stageLimit, Stage previousStage) {
        this.name = name;
        this.stageLimit = stageLimit;
        this.previousStage = previousStage;
        this.stageBlocks = 0;
        this.nextStage = null;
    }

    public Stage(String name, int stageLimit) {
        this(name, stageLimit, null);
    }

    public void setNextStage(Stage stage) {
        this.nextStage = stage;
    }

    public void setPreviousStage(Stage stage) {
        this.previousStage = stage;
    }

    @Nullable
    public Stage getNextStage() {
        return this.nextStage;
    }

    @Nullable
    public Stage getPreviousStage() {
        return this.previousStage;
    }

    public void incrementStageBlocks() {
        this.stageBlocks++;
    }

    public void decrementStageBlocks() {
        this.stageBlocks--;
    }

    public void resetStageCount() {
        this.stageBlocks = 0;
    }

    public boolean fitsOneBlock() {
        return (this.stageBlocks + 1) <= this.stageLimit;
    }

    public String getName() {
        return this.name;
    }

    public Material getStageMaterial() {
        return Material.getMaterial(this.name);
    }

    public void setStageLimit(int limit) {
        this.stageLimit = limit;
    }

    public int getStageLimit() {
        return this.stageLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Stage)) return false;

        if (this == o) return true;
        Stage s = (Stage) o;
        return this.name.equals(s.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
