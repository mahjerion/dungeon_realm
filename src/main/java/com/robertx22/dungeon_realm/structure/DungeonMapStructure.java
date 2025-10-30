package com.robertx22.dungeon_realm.structure;

import com.robertx22.dungeon_realm.configs.DungeonConfig;
import com.robertx22.dungeon_realm.database.DungeonDatabase;
import com.robertx22.dungeon_realm.database.dungeon.Dungeon;
import com.robertx22.dungeon_realm.item.DungeonMapItem;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.dimension.MapGenerationUTIL;
import com.robertx22.library_of_exile.dimension.structure.dungeon.DungeonBuilder;
import com.robertx22.library_of_exile.dimension.structure.dungeon.DungeonStructure;
import com.robertx22.library_of_exile.dimension.structure.dungeon.IDungeon;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.robertx22.dungeon_realm.main.DungeonMain.DIMENSION_KEY;

public class DungeonMapStructure extends DungeonStructure {


    @Override
    public String guid() {
        return "dungeon";
    }

    @Override
    public DungeonBuilder getMap(ChunkPos cp) {
        var serverLevel = DungeonMain.server.getLevel(ResourceKey.create(Registries.DIMENSION, DIMENSION_KEY));
        AtomicReference<String> mapDungeon = new AtomicReference<>();
        var start = getStartChunkPos(cp);
        DungeonMain.ifMapData(serverLevel, cp.getMiddleBlockPosition(5)).ifPresentOrElse(
                (x) -> mapDungeon.set(x.dungeon),
                () -> {
                    var rand = MapGenerationUTIL.createRandom(start);
                    String randomDungeon = RandomUtils.weightedRandom(DungeonDatabase.Dungeons().getFilterWrapped(i -> true).list, rand.nextDouble()).id;
                    mapDungeon.set(randomDungeon);
                }
        );

        DungeonBuilder b = new DungeonBuilder(dungeonSettings(start, mapDungeon.get()));
        return b;
    }

    public static DungeonBuilder.Settings dungeonSettings(ChunkPos pos, String mapDungeon) {
        var rand = MapGenerationUTIL.createRandom(pos);

        List<Dungeon> dungeons = new ArrayList<>(DungeonDatabase.Dungeons().getFilterWrapped(x -> true).list);
        var dungeon = dungeons.stream().filter(x -> x.id.equals(mapDungeon)).findFirst();
        IDungeon mapFinalDungeon = dungeon.orElseGet(() -> RandomUtils.weightedRandom(dungeons, rand.nextDouble()));;

        var settings = new DungeonBuilder.Settings(
            rand,
            DungeonConfig.get().MIN_MAP_ROOMS.get(),
            DungeonConfig.get().MAX_MAP_ROOMS.get(),
            mapFinalDungeon
        );

        // todo
        // settings.possibleDungeons = Arrays.asList(DungeonDungeons.INSTANCE.NIGHT_TERROR.get());

        return settings;
    }

    public ChunkPos getStartFromCounter(int x, int z) {
        var start = new ChunkPos(x * DUNGEON_LENGTH, z * DUNGEON_LENGTH);
        start = getStartChunkPos(start);
        return start;
    }

    @Override
    public int getSpawnHeight() {
        return 50;
    }

    public static int DUNGEON_LENGTH = 30;

    @Override
    protected ChunkPos INTERNALgetStartChunkPos(ChunkPos cp) {
        int chunkX = cp.x;
        int chunkZ = cp.z;
        int distToEntranceX = 11 - (chunkX % DUNGEON_LENGTH);
        int distToEntranceZ = 11 - (chunkZ % DUNGEON_LENGTH);
        chunkX += distToEntranceX;
        chunkZ += distToEntranceZ;
        return new ChunkPos(chunkX, chunkZ);
    }
}
