package com.rogermiranda1000.mineit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class PluginVersionChecker {
    private static final String PLUGIN_ID = "69161";
    private static final String PLUGIN_VERSION_URL = "https://api.spigotmc.org/legacy/update.php?resource=" + PluginVersionChecker.PLUGIN_ID;

    public static String getVersion() throws IOException {
        InputStream inputStream = new URL(PluginVersionChecker.PLUGIN_VERSION_URL).openStream();
        Scanner scanner = new Scanner(inputStream);
        if (scanner.hasNext()) {
            String version = scanner.next();
            scanner.close();
            return version;
        }
        else throw new IOException("No version returned");
    }

    /**
     * It compares two versions
     * @param current The current plugin version
     * @param comparing The version to compare (getted from spigot; the last one)
     * @return If the current version is lower than the newest one
     * @throws NumberFormatException If the strings are not integers with dots
     */
    public static boolean isLower(String current, String comparing) throws NumberFormatException {
        String []currentParams = current.split("\\."), comparingParams = comparing.split("\\.");
        int []currentParamsInteger = new int[3], comparingParamsInteger = new int[3];
        for (int x = 0; x < 3; x++) {
            if (x < currentParams.length) currentParamsInteger[x] = Integer.parseInt(currentParams[x]);
            else currentParamsInteger[x] = 0;
        }
        for (int x = 0; x < 3; x++) {
            if (x < comparingParams.length) comparingParamsInteger[x] = Integer.parseInt(comparingParams[x]);
            else comparingParamsInteger[x] = 0;
        }

        if (currentParamsInteger[0] < comparingParamsInteger[0]) return true;
        else if (currentParamsInteger[0] > comparingParamsInteger[0]) return false;
        else {
            if (currentParamsInteger[1] < comparingParamsInteger[1]) return true;
            else if (currentParamsInteger[1] > comparingParamsInteger[1]) return false;
            else {
                if (currentParamsInteger[2] == comparingParamsInteger[2]) return false;
                else return (currentParamsInteger[2] < comparingParamsInteger[2]);
            }
        }
    }
}
