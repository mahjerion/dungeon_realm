package com.robertx22.dungeon_realm.structure;

import com.robertx22.dungeon_realm.database.DungeonDatabase;
import com.robertx22.dungeon_realm.database.boss_arena.BossArena;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.dimension.MapGenerationUTIL;
import com.robertx22.library_of_exile.dimension.structure.SimplePrebuiltMapData;
import com.robertx22.library_of_exile.dimension.structure.SimplePrebuiltMapStructure;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.world.level.ChunkPos;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ArenaStructure extends SimplePrebuiltMapStructure {

    @Override
    public String guid() {
        return "boss_arena";
    }

    // the pick is deterministic from the instance's start chunk, but working it out is not: getList()
    // copies the whole registry into a new ArrayList, weightedRandom copies it AGAIN, then runs two
    // stream passes over it - and generateInChunk asks for this once per chunk. Same bounded,
    // access ordered LRU as DungeonStructure.builtDungeonCache; evicting is free because a rebuilt
    // answer is identical.
    private static final int MAX_CACHED = 32;
    private final Map<ChunkPos, BossArena> arenaCache = Collections.synchronizedMap(
            new LinkedHashMap<>(MAX_CACHED * 2, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<ChunkPos, BossArena> eldest) {
                    return size() > MAX_CACHED;
                }
            });

    public BossArena getArena(ChunkPos cp) {
        return arenaCache.computeIfAbsent(getStartChunkPos(cp), start -> {
            var random = MapGenerationUTIL.createRandom(start);
            var list = DungeonDatabase.BossArena().getList();
            return RandomUtils.weightedRandom(list, random.nextDouble());
        });
    }

    @Override
    public SimplePrebuiltMapData getMap(ChunkPos start) {
        return getArena(start).structure;
    }

    @Override
    public int getSpawnHeight() {
        return -60;
    }


    @Override
    protected ChunkPos INTERNALgetStartChunkPos(ChunkPos cp) {
        return DungeonMain.MAIN_DUNGEON_STRUCTURE.INTERNALgetStartChunkPos(cp);
    }

}
