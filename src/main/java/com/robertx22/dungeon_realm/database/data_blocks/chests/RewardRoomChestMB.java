package com.robertx22.dungeon_realm.database.data_blocks.chests;

import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.config.map_dimension.ChunkProcessType;
import com.robertx22.library_of_exile.database.map_data_block.MapDataBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class RewardRoomChestMB extends MapDataBlock {

    public RewardRoomChestMB(String id) {
        super(id, id);
        this.process_on = ChunkProcessType.REWARD_ROOM;
    }

    @Override
    public Class<?> getClassForSerialization() {
        return RewardRoomChestMB.class;
    }

    @Override
    public void processImplementationINTERNAL(String key, BlockPos pos, Level world, CompoundTag nbt) {

        DungeonMain.ifMapData(world, pos).ifPresent(x -> {

            var rar = x.getFinishRarity();

            int current = nbt.getInt("reward_chests");

            if (current++ < rar.reward_chests) {
                MapChestMB.createChest(world, pos, false, new ResourceLocation(rar.loot_table));
                nbt.putInt("reward_chests", current);
            } else {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

                // this was ancient code.. why was it here?
                //world.removeBlockEntity((pos)); // dont drop chest loot. this is a big problem if u remove this line
                //world.removeBlock(pos, false);   // don't drop loot
                //world.removeBlockEntity(pos);
            }
        });

    }
}
