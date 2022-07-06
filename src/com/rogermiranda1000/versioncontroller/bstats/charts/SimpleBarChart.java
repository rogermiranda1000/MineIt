// 
// Decompiled by Procyon v0.5.36
// 

package com.rogermiranda1000.versioncontroller.bstats.charts;

import com.rogermiranda1000.versioncontroller.bstats.json.JsonObjectBuilder;

import java.util.Map;
import java.util.concurrent.Callable;

public class SimpleBarChart extends CustomChart
{
    private final Callable<Map<String, Integer>> callable;
    
    public SimpleBarChart(final String chartId, final Callable<Map<String, Integer>> callable) {
        super(chartId);
        this.callable = callable;
    }
    
    @Override
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
        final JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
        final Map<String, Integer> map = this.callable.call();
        if (map == null || map.isEmpty()) {
            return null;
        }
        for (final Map.Entry<String, Integer> entry : map.entrySet()) {
            valuesBuilder.appendField(entry.getKey(), new int[] { entry.getValue() });
        }
        return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
    }
}
