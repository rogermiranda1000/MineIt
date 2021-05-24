package com.rogermiranda1000.mineit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FileManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Mines loadMines(File f) throws FileNotFoundException {
        return FileManager.gson.fromJson(FileManager.getFileContents(f), Mines.class);
    }

    public static void saveMines(File f, Mines mines) throws IOException {
        FileWriter fw = new FileWriter(f);
        FileManager.gson.toJson(mines, fw);
        fw.close();
    }

    private static String getFileContents(File f) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        Scanner scanner = new Scanner(f);
        while (scanner.hasNextLine()) sb.append(scanner.nextLine());
        scanner.close();

        return sb.toString();
    }
}
