package com.rogermiranda1000.mineit.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.MineIt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FileManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Mine loadMine(File f) throws IOException {
        try {
            return FileManager.gson.fromJson(FileManager.getFileContents(f), BasicMine.class).getMine();
        } catch (JsonSyntaxException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    public static void saveMine(File f, Mine mine) throws IOException {
        FileWriter fw = new FileWriter(f);
        FileManager.gson.toJson(new BasicMine(mine), fw);
        fw.close();
    }

    public static void removeMine(Mine mine) throws Exception {
        File f = new File(MineIt.instance.getDataFolder(), mine.getName() + ".yml");
        if (f.exists()) {
            if (!f.delete()) throw new IOException("error deleting the file");
        }
    }

    private static String getFileContents(File f) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        Scanner scanner = new Scanner(f);
        while (scanner.hasNextLine()) sb.append(scanner.nextLine());
        scanner.close();

        return sb.toString();
    }
}
