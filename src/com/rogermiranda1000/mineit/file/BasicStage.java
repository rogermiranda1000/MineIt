package com.rogermiranda1000.mineit.file;

import com.rogermiranda1000.mineit.Stage;

import java.util.ArrayList;
import java.util.HashMap;

public class BasicStage {
    private final String name;
    private final String previousState;
    private final String nextStage;
    private final int stageLimit;

    public BasicStage(Stage stage) {
        this.name = stage.getName();
        this.previousState = (stage.getPreviousStage() == null) ? null : stage.getPreviousStage().getName();
        this.nextStage = (stage.getNextStage() == null) ? null : stage.getNextStage().getName();
        this.stageLimit = stage.getStageLimit();
    }

    public static ArrayList<Stage> getStages(ArrayList<BasicStage> stages) {
        HashMap<String, Stage> cache = new HashMap<>();
        ArrayList<Stage> r = new ArrayList<>(stages.size());

        // create all stages
        for (BasicStage bs : stages) {
            Stage now = new Stage(bs.name, bs.stageLimit);
            cache.put(bs.name, now);
            r.add(now);
        }

        // create all the relations
        for (int i = 0; i < stages.size(); i++) {
            BasicStage bs = stages.get(i);
            r.get(i).setPreviousStage((bs.nextStage == null) ? null : cache.get(bs.nextStage));
            r.get(i).setPreviousStage((bs.previousState == null) ? null : cache.get(bs.previousState));
        }

        return r;
    }
}
