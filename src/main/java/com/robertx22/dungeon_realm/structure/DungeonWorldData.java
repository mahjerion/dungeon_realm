package com.robertx22.dungeon_realm.structure;

import com.robertx22.library_of_exile.dimension.worlddata.MapStructureCounter;

import java.util.concurrent.ConcurrentHashMap;

public class DungeonWorldData {

    public MapStructureCounter counter = new MapStructureCounter();

    public DungeonPlayerData data = new DungeonPlayerData();

    // instance start chunk key ("x_z") -> the dungeon id actually generated there.
    //
    // getMap has to answer "which dungeon is this instance?" for every chunk, and when the map data
    // can't be read it has no choice but to pick one. With nothing recording that pick, the next chunk
    // picks again - and every chunk is written to disk permanently, which is how one instance ends up
    // part sewers and part garden with rooms that don't line up.
    //
    // deliberately separate from the ownership store in `data`: that releases an instance once nobody
    // is using it, but what has already been built into the world stays true until the chunks are gone.
    //
    // must STAY declared as the concrete ConcurrentHashMap type. Worldgen reads and writes it from
    // background threads, and LoadSave rebuilds the field with Gson, which picks the implementation
    // from the DECLARED type - so declaring it as Map would compile but quietly put a plain HashMap
    // back on the world's first reload.
    //
    // cleared by the dimension wipe for free: clearMapDataOnFolderWipe replaces the whole
    // DungeonWorldData, keeping only the counter.
    public ConcurrentHashMap<String, String> generatedDungeonAtStart = new ConcurrentHashMap<>();

}
