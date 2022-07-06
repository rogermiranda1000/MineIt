// 
// Decompiled by Procyon v0.5.36
// 

package com.rogermiranda1000.versioncontroller.bstats.charts;

import com.rogermiranda1000.versioncontroller.bstats.json.JsonObjectBuilder;

import java.util.concurrent.Callable;

public class SingleLineChart extends CustomChart
{
    private final Callable<Integer> callable;
    
    public SingleLineChart(final String chartId, final Callable<Integer> callable) {
        super(chartId);
        this.callable = callable;
    }
    
    @Override
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
        final int value = this.callable.call();
        if (value == 0) {
            return null;
        }
        return new JsonObjectBuilder().appendField("value", value).build();
    }
}
