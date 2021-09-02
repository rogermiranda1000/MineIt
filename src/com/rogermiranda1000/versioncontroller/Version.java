package com.rogermiranda1000.versioncontroller;

import org.jetbrains.annotations.NotNull;

/**
 * A Minecraft version (ex: 1.8 or 1.12.2)
 */
public class Version implements Comparable<Version> {
    public static final Version MC_1_9 = new Version(1,9);
    public static final Version MC_1_10 = new Version(1,10);
    public static final Version MC_1_11 = new Version(1,11);
    public static final Version MC_1_12 = new Version(1,12);
    public static final Version MC_1_13 = new Version(1,13);
    public static final Version MC_1_15 = new Version(1,15);
    public static final Version MC_1_16_2 = new Version(1,16,2);
    public static final Version MC_1_17 = new Version(1,17);
    private final byte []version;

    /**
     * It parses a version as string
     * @param version Version (ex: 1.8 or 1.12.2)
     * @throws NumberFormatException The version is not a number
     */
    public Version(String version) throws NumberFormatException {
        this.version = new byte[3];

        String []currentParams = version.split("\\.");
        for (int x = 0; x < 3; x++) {
            if (x < currentParams.length) this.version[x] = Byte.parseByte(currentParams[x]);
            else this.version[x] = 0;
        }
    }

    /**
     * It parses a version
     * @param num Version (ex: 1.8 or 1.12.2)
     */
    @SuppressWarnings("ConstantConditions")
    public Version(int ...num) {
        this.version = new byte[3];

        for (int x = 0; x < 3; x++) {
            if (x < num.length) this.version[x] = (byte) num[x];
            else this.version[x] = (byte) 0;
        }
    }

    /**
     * It compares two versions
     * @param o Object to compare
     * @return   0: if (this == o)
     *          <0: if (this < o)
     *          >0: if (this > o)
     */
    @Override
    public int compareTo(@NotNull Version o) {
        int tmp, x = 0;
        do {
            tmp = this.version[x] - o.version[x];
            x++;
        } while (x < this.version.length && tmp == 0);
        return tmp;
    }

    @Override
    public String toString() {
        String r = this.version[0] + "." + this.version[1];
        if (this.version[2] != 0) r += "." + this.version[2];
        return r;
    }
}
