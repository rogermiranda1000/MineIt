package com.rogermiranda1000.mineit;

import com.rogermiranda1000.versioncontroller.VersionController;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.inventory.ItemStack;

public class Stage {
    private final Object block;

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

    public Stage(String block, int stageLimit, Stage previousStage) {
        this.block = VersionController.get().getMaterial(block);
        this.stageLimit = stageLimit;
        this.previousStage = previousStage;
        this.stageBlocks = 0;
        this.nextStage = null;
    }

    public Stage(String name, int stageLimit) {
        this(name, stageLimit, null);
    }

    public Stage(String name) {
        this(name, Integer.MAX_VALUE, null);
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
        return VersionController.get().getName(this.block);
    }

    public Object getStageMaterial() {
        return this.block;
    }

    public ItemStack getStageItemStack() {
        return VersionController.get().getItemStack(this.block);
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
        return this.getName().equals(s.getName());
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
