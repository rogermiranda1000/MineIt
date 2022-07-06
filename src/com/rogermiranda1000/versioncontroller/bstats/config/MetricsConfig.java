// 
// Decompiled by Procyon v0.5.36
// 

package com.rogermiranda1000.versioncontroller.bstats.config;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Pattern;
import java.util.Optional;
import java.util.function.Function;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;

public class MetricsConfig
{
    private final File file;
    private final boolean defaultEnabled;
    private String serverUUID;
    private boolean enabled;
    private boolean logErrors;
    private boolean logSentData;
    private boolean logResponseStatusText;
    private boolean didExistBefore;
    
    public MetricsConfig(final File file, final boolean defaultEnabled) throws IOException {
        this.didExistBefore = true;
        this.file = file;
        this.defaultEnabled = defaultEnabled;
        this.setupConfig();
    }
    
    public String getServerUUID() {
        return this.serverUUID;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public boolean isLogErrorsEnabled() {
        return this.logErrors;
    }
    
    public boolean isLogSentDataEnabled() {
        return this.logSentData;
    }
    
    public boolean isLogResponseStatusTextEnabled() {
        return this.logResponseStatusText;
    }
    
    public boolean didExistBefore() {
        return this.didExistBefore;
    }
    
    private void setupConfig() throws IOException {
        if (!this.file.exists()) {
            this.didExistBefore = false;
            this.writeConfig();
        }
        this.readConfig();
        if (this.serverUUID == null) {
            this.writeConfig();
            this.readConfig();
        }
    }
    
    private void writeConfig() throws IOException {
        final List<String> configContent = new ArrayList<String>();
        configContent.add("# bStats (https://bStats.org) collects some basic information for plugin authors, like");
        configContent.add("# how many people use their plugin and their total player count. It's recommended to keep");
        configContent.add("# bStats enabled, but if you're not comfortable with this, you can turn this setting off.");
        configContent.add("# There is no performance penalty associated with having metrics enabled, and data sent to");
        configContent.add("# bStats is fully anonymous.");
        configContent.add("enabled=" + this.defaultEnabled);
        configContent.add("server-uuid=" + UUID.randomUUID().toString());
        configContent.add("log-errors=false");
        configContent.add("log-sent-data=false");
        configContent.add("log-response-status-text=false");
        this.writeFile(this.file, configContent);
    }
    
    private void readConfig() throws IOException {
        final List<String> lines = this.readFile(this.file);
        if (lines == null) {
            throw new AssertionError((Object)"Content of newly created file is null");
        }
        this.enabled = this.getConfigValue("enabled", lines).map((Function<? super String, Boolean>)"true"::equals).orElse(true);
        this.serverUUID = this.getConfigValue("server-uuid", lines).orElse(null);
        this.logErrors = this.getConfigValue("log-errors", lines).map((Function<? super String, Boolean>)"true"::equals).orElse(false);
        this.logSentData = this.getConfigValue("log-sent-data", lines).map((Function<? super String, Boolean>)"true"::equals).orElse(false);
        this.logResponseStatusText = this.getConfigValue("log-response-status-text", lines).map((Function<? super String, Boolean>)"true"::equals).orElse(false);
    }
    
    private Optional<String> getConfigValue(final String key, final List<String> lines) {
        return lines.stream().filter(line -> line.startsWith(key + "=")).map(line -> line.replaceFirst(Pattern.quote(key + "="), "")).findFirst();
    }
    
    private List<String> readFile(final File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        final FileReader fileReader = new FileReader(file);
        try {
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            try {
                final List<? super String> list = bufferedReader.lines().collect(Collectors.toList());
                bufferedReader.close();
                fileReader.close();
                return (List<String>)list;
            }
            catch (Throwable t) {
                try {
                    bufferedReader.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (Throwable t2) {
            try {
                fileReader.close();
            }
            catch (Throwable exception2) {
                t2.addSuppressed(exception2);
            }
            throw t2;
        }
    }
    
    private void writeFile(final File file, final List<String> lines) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        final FileWriter fileWriter = new FileWriter(file);
        try {
            final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            try {
                for (final String line : lines) {
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
                bufferedWriter.close();
            }
            catch (Throwable t) {
                try {
                    bufferedWriter.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
            fileWriter.close();
        }
        catch (Throwable t2) {
            try {
                fileWriter.close();
            }
            catch (Throwable exception2) {
                t2.addSuppressed(exception2);
            }
            throw t2;
        }
    }
}
