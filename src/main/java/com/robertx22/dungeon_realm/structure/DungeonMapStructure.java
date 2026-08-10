package com.robertx22.dungeon_realm.structure;

import com.robertx22.dungeon_realm.configs.DungeonConfig;
import com.robertx22.dungeon_realm.database.DungeonDatabase;
import com.robertx22.dungeon_realm.item.DungeonMapItem;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.dimension.MapGenerationUTIL;
import com.robertx22.library_of_exile.dimension.structure.dungeon.DungeonBuilder;
import com.robertx22.library_of_exile.dimension.structure.dungeon.DungeonData;
import com.robertx22.library_of_exile.dimension.structure.MapStructure;
import com.robertx22.library_of_exile.dimension.structure.dungeon.DungeonStructure;
import com.robertx22.library_of_exile.dimension.structure.dungeon.IDungeon;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

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
    // instance would otherwise flood the log.
    //
    // Bounded, because "one entry per instance" is not the small number it sounds like: anything that
    // generates ahead of the player (Distant Horizons, a blocking raycast) walks into instances nobody
    // has ever been given, and on a long lived server that is thousands of them.
    private static final int MAX_WARNED_INSTANCES = 4096;
    private static final Set<ChunkPos> WARNED_NO_MAP_DATA = ConcurrentHashMap.newKeySet();

    // a wipe hands the same coordinates out again to entirely different maps, so a coordinate that warned
    // before must be able to warn again - otherwise a real problem in a recycled instance stays silent
    public static void forgetWarnings() {
        WARNED_NO_MAP_DATA.clear();
        DungeonStructure.forgetSkippedCarveWarnings();
        // a wipe hands these coordinates to an entirely different map, so a chunk that had nothing
        // placeable in it before has to be allowed to try again
        MapStructure.forgetRepairFailures();
    }

    @Override
    public DungeonBuilder getMap(ChunkPos cp) {
        var serverLevel = DungeonMain.server.getLevel(ResourceKey.create(Registries.DIMENSION, DIMENSION_KEY));
        AtomicReference<String> mapDungeon = new AtomicReference<>();
        var start = getStartChunkPos(cp);
        DungeonMain.ifMapData(serverLevel, cp.getMiddleBlockPosition(5)).ifPresent(x -> mapDungeon.set(x.dungeon));

        String saved = mapDungeon.get();
        // the id has to still name a REGISTERED dungeon, not merely be a non empty string. A map whose
        // dungeon left the database (datapack edit, addon disabled) otherwise counted as resolved, and
        // the random dungeon picked in its place got cached as the instance's permanent identity.
        boolean resolved = DungeonDatabase.Dungeons().isRegistered(saved);

        String dungeon = saved;
        if (!resolved) {
            // No usable map data, so there is no right answer - and nothing will be carved from this
            // builder either way (getBuiltDungeon refuses to build an unresolved one). All this needs to
            // do is give dungeonSettings SOMETHING with a room size and a mob list so it cannot NPE.
            //
            // It used to pin the roll in DungeonWorldData.generatedDungeonAtStart so that every chunk of
            // a data-less instance agreed on the same wrong theme. That mattered only while such chunks
            // were still being carved. Now that they aren't, the pin bought nothing and cost a great
            // deal: it grew by one entry for every instance anything ever generated ahead of time - and
            // it is serialized to NBT, so it grew the saved world too, forever.
            IDungeon rolled = randomDungeonFor(start);
            dungeon = rolled == null ? null : rolled.GUID();

            if (WARNED_NO_MAP_DATA.size() < MAX_WARNED_INSTANCES && WARNED_NO_MAP_DATA.add(start)) {
                // the thread and the triggering chunk are the diagnostic bit: a worldgen worker name
                // means ordinary player movement reached here, but "Server thread" means something
                // force-generated this chunk synchronously from inside a tick - which is the shape of
                // the raycast hang, and tells you the two problems are firing together.
                DungeonMain.LOG.warn((saved == null || saved.isEmpty()
                        ? "No dungeon map data for the instance at " + start
                        : "The instance at " + start + " wants dungeon '" + saved + "', which is not registered")
                        + ". Nothing will be carved here until it can be read - resolving '" + dungeon
                        + "' only so the room size and mob list have an answer. Triggered by chunk " + cp
                        + " on thread '" + Thread.currentThread().getName() + "'.");
            }
        }

        DungeonBuilder b = new DungeonBuilder(dungeonSettings(start, dungeon));
        b.resolvedFromMapData = resolved;
        return b;
    }

    /**
     * Deterministic pick for an instance whose dungeon isn't known, drawn from its OWN Random.
     * <p>
     * This used to roll off the same {@code rand} the layout is built from, inside an {@code orElseGet},
     * so merely failing to find the dungeon advanced the shared stream by one - which changed the room
     * count roll and every rotation drawn after it. A guessed layout then differed from the real one even
     * when the guess happened to name the right dungeon, and the two could not be stitched together.
     */
    private static IDungeon randomDungeonFor(ChunkPos pos) {
        var fallbackRand = MapGenerationUTIL.createRandom(pos);
        return RandomUtils.weightedRandom(DungeonDatabase.Dungeons().getList(), fallbackRand.nextDouble());
    }

    public static DungeonBuilder.Settings dungeonSettings(ChunkPos pos, String mapDungeon) {
        var rand = MapGenerationUTIL.createRandom(pos);

        IDungeon mapFinalDungeon = DungeonDatabase.Dungeons().getOptional(mapDungeon)
                .map(x -> (IDungeon) x)
                .orElseGet(() -> randomDungeonFor(pos));

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
