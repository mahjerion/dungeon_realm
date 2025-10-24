package com.robertx22.dungeon_realm.database.data_blocks.chests;

import com.robertx22.dungeon_realm.main.DungeonLootTables;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.database.map_data_block.MapBlockCtx;
import com.robertx22.library_of_exile.database.map_data_block.MapDataBlock;
import com.robertx22.library_of_exile.main.ExileLog;
import com.robertx22.library_of_exile.util.wiki.WikiEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class MapChestMB extends MapDataBlock {

    public MapChestMB(String id) {
        super(id, id);
        this.aliases.add("big_chest"); // this is because i removed puzzle block
        this.aliases.add("puzzle"); // this is because i removed puzzle block
        this.aliases.add("trap_chest");
    }

    @Override
    public Class<?> getClassForSerialization() {
        return MapChestMB.class;
    }

    @Override
    public void processImplementationINTERNAL(String key, BlockPos pos, Level world, CompoundTag nbt, MapBlockCtx ctx) {

        boolean isTrapped = key.contains("trap");

        ResourceLocation table = DungeonLootTables.randomMapLoot();


        createChest(world, pos, isTrapped, table);
        DungeonMain.ifMapData(world, pos).ifPresent(x -> {
            x.totalChests++;
            x.updateMapLootCompletion((ServerLevel) world, pos);
        });
    }

    public static void createChest(Level world, BlockPos pos, boolean trapped, ResourceLocation table) {

        if (trapped) {
            world.setBlock(pos, Blocks.TRAPPED_CHEST.defaultBlockState(), 2);
        } else {
            world.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
        }

        BlockEntity tile = world.getBlockEntity(pos);

        if (tile instanceof ChestBlockEntity) {
            ChestBlockEntity chest = (ChestBlockEntity) tile;

            chest.setLootTable(world, world.getRandom(), pos, table);

        } else {
            ExileLog.get().warn("Chest gen failed, tile not instanceof vanilla chest.");
        }
    }

    @Override
    public WikiEntry getWikiEntry() {
        return WikiEntry.of("Always spawns a chest");
    }
}
