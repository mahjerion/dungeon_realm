package com.robertx22.dungeon_realm.database.data_blocks.mobs;

import com.robertx22.dungeon_realm.capability.DungeonEntityCapability;
import com.robertx22.dungeon_realm.main.DataBlockTags;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.database.map_data_block.MapDataBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class MapBossMB extends MapDataBlock {
    public MapBossMB(String id) {
        super(id, id);
        this.tags.add(DataBlockTags.CAN_SPAWN_LEAGUE);
    }

    @Override
    public void processImplementationINTERNAL(String s, BlockPos pos, Level level, CompoundTag nbt) {

        var arena = DungeonMain.ARENA.getArena(new ChunkPos(pos));

        EntityType<? extends LivingEntity> type = arena.getRandomBoss();

        for (LivingEntity en : MobBuilder.of(type, this, x -> {
            x.amount = 1;
        }).summonMobs(level, pos)) {
            DungeonEntityCapability.get(en).data.isDungeonBoss = true;
        }
    }

    @Override
    public Class<?> getClassForSerialization() {
        return MapBossMB.class;
    }
}
