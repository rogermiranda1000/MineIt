package com.rogermiranda1000.mineit;

import com.rogermiranda1000.versioncontroller.VersionController;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.inventory.ItemStack;

public class Stage {
    private BlockType block;

    /**
     * Maximum number of simultaneous blocks on that stage
     */
    private int stageLimit;

    private boolean isBreakable;

    private Stage previousStage;

    private Stage nextStage;

    /**
     * Number of current simultaneous blocks on that stage
     */
    private int stageBlocks;

    public Stage(BlockType block, int stageLimit, boolean isBreakable, Stage previousStage) {
        this.block = block;
        this.stageLimit = stageLimit;
        this.isBreakable = isBreakable;
        this.previousStage = previousStage;
        this.stageBlocks = 0;
        this.nextStage = null;
    }

    public Stage(BlockType block, boolean isBreakable) {
        this(block, Integer.MAX_VALUE, isBreakable, null);
    }

    public Stage(String name, int stageLimit, boolean isBreakable, Stage previousStage) {
        this(VersionController.get().getMaterial(name), stageLimit, isBreakable, previousStage);
    }

    public Stage(String name, int stageLimit, Stage previousStage) {
        this(name, stageLimit, true, previousStage);
    }

    public Stage(String name, int stageLimit, boolean isBreakable) {
        this(name, stageLimit, isBreakable, null);
    }

    public void setBlock(BlockType block, boolean isBreakable) {
        this.block = block;
        this.isBreakable = isBreakable;
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
        return this.block.getName();
    }

    @Nullable
    public BlockType getStageMaterial() {
        return this.block;
    }

    public ItemStack getStageItemStack() {
        return this.block.getItemStack();
    }

    public void setStageLimit(int limit) {
        this.stageLimit = limit;
    }

    public int getStageLimit() {
        return this.stageLimit;
    }

    public boolean isBreakable() {
        return this.isBreakable;
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
