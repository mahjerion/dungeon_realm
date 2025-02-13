package com.robertx22.dungeon_realm.database.data_blocks.chests;

import com.robertx22.dungeon_realm.database.holders.DungeonMapBlocks;
import com.robertx22.library_of_exile.database.map_data_block.MapDataBlock;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class MapChanceChestMB extends MapDataBlock {

    public MapChanceChestMB(String id) {
        super(id, id);
        this.aliases.add("chest_chance");

    }

    @Override
    public Class<?> getClassForSerialization() {
        return MapChanceChestMB.class;
    }

    @Override
    public void processImplementationINTERNAL(String key, BlockPos pos, Level world, CompoundTag nbt) {

        if (!nbt.getBoolean("chance_chest") && RandomUtils.roll(25)) {
            nbt.putBoolean("chance_chest", true);
            DungeonMapBlocks.INSTANCE.MAP_CHEST.get().process(key, pos, world, nbt);
        } else {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

}
