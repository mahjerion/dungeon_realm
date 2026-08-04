package com.robertx22.dungeon_realm.structure;

import com.robertx22.dungeon_realm.configs.DungeonConfig;
import com.robertx22.dungeon_realm.database.DungeonDatabase;
import com.robertx22.dungeon_realm.database.dungeon.Dungeon;
import com.robertx22.dungeon_realm.item.DungeonMapItem;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.dimension.MapGenerationUTIL;
import com.robertx22.library_of_exile.dimension.structure.dungeon.DungeonBuilder;
import com.robertx22.library_of_exile.dimension.structure.dungeon.DungeonData;
import com.robertx22.library_of_exile.dimension.structure.dungeon.DungeonStructure;
import com.robertx22.library_of_exile.dimension.structure.dungeon.IDungeon;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.robertx22.dungeon_realm.main.DungeonMain.DIMENSION_KEY;

public class DungeonMapStructure extends DungeonStructure {


    @Override
    public String guid() {
        return "dungeon";
    }

    // one warning per instance: getMap runs per generated chunk and per mob spawn, so an unowned
    // instance would otherwise flood the log
    private static final Set<ChunkPos> WARNED_NO_MAP_DATA = ConcurrentHashMap.newKeySet();

    // a wipe hands the same coordinates out again to entirely different maps, so a coordinate that warned
    // before must be able to warn again - otherwise a real problem in a recycled instance stays silent
    public static void forgetWarnings() {
        WARNED_NO_MAP_DATA.clear();
    }

    @Override
    public DungeonBuilder getMap(ChunkPos cp) {
        var serverLevel = DungeonMain.server.getLevel(ResourceKey.create(Registries.DIMENSION, DIMENSION_KEY));
        AtomicReference<String> mapDungeon = new AtomicReference<>();
        var start = getStartChunkPos(cp);
        DungeonMain.ifMapData(serverLevel, cp.getMiddleBlockPosition(5)).ifPresentOrElse(
                (x) -> mapDungeon.set(x.dungeon),
                () -> {
                    // no saved map data for this instance, so there is no right answer here. it used to
                    // roll a dungeon from the WHOLE database, which ignores Atlas unlocks and made maps
                    // generate as dungeons the player never unlocked. dungeonSettings still picks a
                    // deterministic one so the instance is at least self consistent, but the builder is
                    // flagged unresolved so the layout is never cached as this instance's identity.
                    if (WARNED_NO_MAP_DATA.add(start)) {
                        DungeonMain.LOG.warn("No dungeon map data for the instance at " + start
                                + ", generating a placeholder dungeon. If a player is in this map, its layout "
                                + "will not match the map item they used.");
                    }
                }
        );

        String dungeon = mapDungeon.get();
        DungeonBuilder b = new DungeonBuilder(dungeonSettings(start, dungeon));
        b.resolvedFromMapData = dungeon != null && !dungeon.isEmpty();
        return b;
    }

    public static DungeonBuilder.Settings dungeonSettings(ChunkPos pos, String mapDungeon) {
        var rand = MapGenerationUTIL.createRandom(pos);

        List<Dungeon> dungeons = new ArrayList<>(DungeonDatabase.Dungeons().getFilterWrapped(x -> true).list);
        var dungeon = dungeons.stream().filter(x -> x.id.equals(mapDungeon)).findFirst();
        IDungeon mapFinalDungeon = dungeon.orElseGet(() -> RandomUtils.weightedRandom(dungeons, rand.nextDouble()));;

        // a dungeon with bigger rooms is a lot more to walk through, so it can ask for fewer of them
        var data = mapFinalDungeon.getDungeonData();
        int minRooms = data.min_rooms > 0 ? data.min_rooms : DungeonConfig.get().MIN_MAP_ROOMS.get();
        int maxRooms = data.max_rooms > 0 ? data.max_rooms : DungeonConfig.get().MAX_MAP_ROOMS.get();
        // a dungeon that only overrides max_rooms would otherwise keep the config's min (e.g. 12),
        // and RandomRange(12, 10) returns 12 - silently ignoring the smaller cap. clamp min to max.
        minRooms = Math.min(minRooms, maxRooms);

        var settings = new DungeonBuilder.Settings(
            rand,
            minRooms,
            maxRooms,
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

    // spawn in the center of the entrance room, not the center of its origin chunk. a room is
    // roomChunks x roomChunks chunks anchored at the start chunk, so its center sits (roomChunks-1)*8
    // blocks further in +X/+Z. 0 for the default 16-wide dungeons (unchanged), 8 for 32, 24 for 64.
    @Override
    public int getSpawnCenterBlockOffset(ChunkPos start) {
        // getMap falls back to a random weighted dungeon when there's no map data yet, and that dungeon's
        // room size has nothing to do with the one that will actually generate here - guessing 32 for a
        // 16 wide dungeon puts the player 8 blocks inside a wall. no data, no offset.
        var serverLevel = DungeonMain.server.getLevel(ResourceKey.create(Registries.DIMENSION, DIMENSION_KEY));
        if (DungeonMain.ifMapData(serverLevel, start.getMiddleBlockPosition(5)).isEmpty()) {
            return 0;
        }
        return (getMap(start).getRoomChunks() - 1) * 8;
    }

    // the room grid is always this many cells, whatever a dungeon's room size is. bigger rooms make a
    // bigger dungeon, not a dungeon with fewer rooms.
    public static final int GRID_CELLS = 20;
    // widest footprint any dungeon can ever have, in chunks
    public static final int MAX_GRID_SPAN_CHUNKS = GRID_CELLS * DungeonData.MAX_ROOM_CHUNKS;

    // spacing between dungeon instances, in chunks. must fit MAX_GRID_SPAN_CHUNKS plus a gap, so that a
    // player at the edge of one dungeon can never load chunks belonging to the next one.
    // WARNING: changing this or START_OFFSET re-grids where every instance lives, which invalidates
    // dungeons already generated in existing worlds. it's sized for the largest supported room so it
    // never has to change again.
    public static int DUNGEON_LENGTH = 90;

    // where inside each spacing period a dungeon starts. this has to be a single constant covering every
    // room size, because the start is resolved from chunk coords alone, before we know which dungeon (and
    // therefore which room size) is there. the grid grows both ways from it, so it must leave
    // MAX_GRID_SPAN_CHUNKS/2 on each side without crossing into the neighbouring period.
    public static final int START_OFFSET = MAX_GRID_SPAN_CHUNKS / 2 + 1;

    @Override
    protected ChunkPos INTERNALgetStartChunkPos(ChunkPos cp) {
        // floorMod, not %: % is negative for negative chunk coords, which made this non idempotent
        // out there (-31 -> -19, then -19 -> 11) and broke the uniform instance grid.
        return new ChunkPos(
                cp.x + START_OFFSET - Math.floorMod(cp.x, DUNGEON_LENGTH),
                cp.z + START_OFFSET - Math.floorMod(cp.z, DUNGEON_LENGTH));
    }
}
