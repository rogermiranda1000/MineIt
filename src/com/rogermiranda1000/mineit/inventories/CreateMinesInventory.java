package com.rogermiranda1000.mineit.inventories;

import com.rogermiranda1000.helper.BasicInventory;
import com.rogermiranda1000.mineit.Mine;

import java.util.HashMap;

public interface CreateMinesInventory {
    MinesInventory create(HashMap<Mine, BasicInventory> minesInventories, int offset, MinesInventory pre);
}
