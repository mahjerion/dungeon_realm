package com.robertx22.dungeon_realm.structure;

import com.robertx22.dungeon_realm.database.DungeonDatabase;
import com.robertx22.dungeon_realm.database.uber_arena.UberBossArena;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.dimension.MapGenerationUTIL;
import com.robertx22.library_of_exile.dimension.structure.SimplePrebuiltMapData;
import com.robertx22.library_of_exile.dimension.structure.SimplePrebuiltMapStructure;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.world.level.ChunkPos;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class UberArenaStructure extends SimplePrebuiltMapStructure {

    @Override
    public String guid() {
        return "uber_boss_arena";
    }

    // see ArenaStructure.arenaCache - same per chunk registry copy, same fix.
    private static final int MAX_CACHED = 32;
    private final Map<ChunkPos, UberBossArena> uberCache = Collections.synchronizedMap(
            new LinkedHashMap<>(MAX_CACHED * 2, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<ChunkPos, UberBossArena> eldest) {
                    return size() > MAX_CACHED;
                }
            });

    public UberBossArena getUber(ChunkPos cp) {
        return uberCache.computeIfAbsent(getStartChunkPos(cp), start -> {
            var random = MapGenerationUTIL.createRandom(start);
            var list = DungeonDatabase.UberBoss().getList();
            return RandomUtils.weightedRandom(list, random.nextDouble());
        });
    }

    @Override
    public SimplePrebuiltMapData getMap(ChunkPos start) {
        return getUber(start).structure_data;
    }

    @Override
    public int getSpawnHeight() {
        return 85 + 50;
    }


    @Override
    protected ChunkPos INTERNALgetStartChunkPos(ChunkPos cp) {
        return DungeonMain.MAIN_DUNGEON_STRUCTURE.INTERNALgetStartChunkPos(cp);
    }

}
