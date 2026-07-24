package com.robertx22.dungeon_realm.database.data_blocks;

import com.robertx22.library_of_exile.database.map_data_block.MapBlockCtx;
import com.robertx22.library_of_exile.database.map_data_block.MapDataBlock;
import com.robertx22.library_of_exile.util.wiki.WikiEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

/**
 * Marks where the player lands when entering a map. Read straight off the entrance room's template
 * when the map is created (see DungeonStructure#findDataBlockInRoom), because at that point the
 * dimension hasn't generated a single chunk yet.
 * <p>
 * So this processor has nothing left to do by the time the block itself is processed. It exists so
 * the block is still a *recognized* one: unmatched data blocks fall through to the dimension's
 * DEFAULT_DATA_BLOCK, which is "mob" by default, and would spawn a mob pack right on the player.
 */
public class PlayerSpawnMB extends MapDataBlock {

    public PlayerSpawnMB(String id) {
        super(id, id);
        this.aliases.add("spawn");
    }

    @Override
    public Class<?> getClassForSerialization() {
        return PlayerSpawnMB.class;
    }

    @Override
    public void processImplementationINTERNAL(String key, BlockPos pos, Level world, CompoundTag nbt, MapBlockCtx ctx) {
        // nothing, the position was already consumed when the map was created
    }

    @Override
    public WikiEntry getWikiEntry() {
        return WikiEntry.of("Sets where players spawn when they enter the map. Only works in entrance rooms, and only for entering the map. If a room has several, the one closest to the room's center is used. Rooms without one spawn players in the middle of the room like before.");
    }
}
