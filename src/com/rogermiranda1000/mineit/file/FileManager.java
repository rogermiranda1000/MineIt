package com.rogermiranda1000.mineit.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.rogermiranda1000.mineit.Mine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FileManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Mine loadMines(File f) throws IOException, InvalidLocationException, JsonSyntaxException {
        return FileManager.gson.fromJson(FileManager.getFileContents(f), BasicMine.class).getMine();
    }

    public static void saveMines(File f, Mine mines) throws IOException {
        FileWriter fw = new FileWriter(f);
        FileManager.gson.toJson(new BasicMine(mines), fw);
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
