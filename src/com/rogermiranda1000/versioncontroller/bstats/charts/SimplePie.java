// 
// Decompiled by Procyon v0.5.36
// 

package com.rogermiranda1000.versioncontroller.bstats.charts;

import com.rogermiranda1000.versioncontroller.bstats.json.JsonObjectBuilder;

import java.util.concurrent.Callable;

public class SimplePie extends CustomChart
{
    private final Callable<String> callable;
    
    public SimplePie(final String chartId, final Callable<String> callable) {
        super(chartId);
        this.callable = callable;
    }
    
    @Override
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
        final String value = this.callable.call();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return new JsonObjectBuilder().appendField("value", value).build();
    }
}
