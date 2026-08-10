package com.robertx22.dungeon_realm.structure;

import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.config.map_dimension.ChunkProcessType;
import com.robertx22.library_of_exile.config.map_dimension.ProcessMapChunks;
import com.robertx22.library_of_exile.dimension.structure.SimplePrebuiltMapData;
import com.robertx22.library_of_exile.dimension.structure.SimplePrebuiltMapStructure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public class RewardStructure extends SimplePrebuiltMapStructure {

    @Override
    public String guid() {
        return "reward_room";
    }

    // static, not a fresh instance per call: SimplePrebuiltMapData caches its resolved footprint in a
    // transient field, so handing out a new one per chunk left that cache permanently cold and made
    // generateInChunk re-probe the template manager on EVERY chunk. The reward room is the same one
    // everywhere, so there is nothing per instance to keep apart.
    private static final SimplePrebuiltMapData DATA =
            new SimplePrebuiltMapData(0, DungeonMain.MODID + ":map_reward/sandstone");

    @Override
    public SimplePrebuiltMapData getMap(ChunkPos start) {
        return DATA;
        // todo
        /*
        var random = MapGenerationUTIL.createRandom(start);
        var list = DungeonDatabase.UberBoss().getList();
        var ob = RandomUtils.weightedRandom(list, random.nextDouble());
        return ob.structure;

         */
    }

    public void generateManually(ServerLevel level, ChunkPos cp) {
        var start = getStartChunkPos(cp);
        var chunk = level.getChunk(start.x, start.z);
        ProcessMapChunks.spawnDataFromChunk(level, chunk, ChunkProcessType.REWARD_ROOM);
    }

    @Override
    public int getSpawnHeight() {
        return -60 + 48;
        
    }


    @Override
    protected ChunkPos INTERNALgetStartChunkPos(ChunkPos cp) {
        return DungeonMain.MAIN_DUNGEON_STRUCTURE.INTERNALgetStartChunkPos(cp);
    }

}
