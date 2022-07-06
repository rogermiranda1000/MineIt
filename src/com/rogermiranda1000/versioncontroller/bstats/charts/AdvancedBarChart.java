// 
// Decompiled by Procyon v0.5.36
// 

package com.rogermiranda1000.versioncontroller.bstats.charts;

import com.rogermiranda1000.versioncontroller.bstats.json.JsonObjectBuilder;

import java.util.Map;
import java.util.concurrent.Callable;

public class AdvancedBarChart extends CustomChart
{
    private final Callable<Map<String, int[]>> callable;
    
    public AdvancedBarChart(final String chartId, final Callable<Map<String, int[]>> callable) {
        super(chartId);
        this.callable = callable;
    }
    
    @Override
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
        final JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
        final Map<String, int[]> map = this.callable.call();
        if (map == null || map.isEmpty()) {
            return null;
        }
        boolean allSkipped = true;
        for (final Map.Entry<String, int[]> entry : map.entrySet()) {
            if (entry.getValue().length == 0) {
                continue;
            }
            allSkipped = false;
            valuesBuilder.appendField(entry.getKey(), entry.getValue());
        }
        if (allSkipped) {
            return null;
        }
        return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
    }
}
