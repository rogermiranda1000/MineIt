package com.rogermiranda1000.mineit.file;

import com.rogermiranda1000.mineit.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BasicStage {
    private final String name;
    private final String previousState;
    private final String nextStage;
    private final int stageLimit;

    public BasicStage(Stage stage) {
        this.name = stage.getName();
        this.previousState = (stage.getPreviousStage() == null) ? "" : stage.getPreviousStage().getName();
        this.nextStage = (stage.getNextStage() == null) ? "" : stage.getNextStage().getName();
        this.stageLimit = stage.getStageLimit();
    }

    public static ArrayList<Stage> getStages(ArrayList<BasicStage> stages) throws IOException {
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
            if (!bs.nextStage.equals("")) {
                Stage nextStage = cache.get(bs.nextStage);
                if (nextStage == null) throw new IOException("Unknown next stage in mine");
                r.get(i).setNextStage(nextStage);
            }
            if (!bs.previousState.equals("")) {
                Stage prevStage = cache.get(bs.previousState);
                if (prevStage == null) throw new IOException("Unknown previous stage in mine");
                r.get(i).setPreviousStage(prevStage);
            }
        }

        return r;
    }
}
