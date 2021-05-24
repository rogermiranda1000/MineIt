package com.rogermiranda1000.mineit;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FileManager {
    private static final Gson gson = new Gson();

    public static Mines loadMines(File f) throws FileNotFoundException {
        return FileManager.gson.fromJson(FileManager.getFileContents(f), Mines.class);
    }

    public static void saveMines(File f, Mines mines) throws IOException {
        FileManager.gson.toJson(mines, new FileWriter(f));
    }

    private static String getFileContents(File f) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        Scanner scanner = new Scanner(f);
        while (scanner.hasNextLine()) sb.append(scanner.nextLine());
        scanner.close();

        return sb.toString();
    }
}
