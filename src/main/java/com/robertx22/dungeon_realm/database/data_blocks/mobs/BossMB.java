package com.robertx22.dungeon_realm.database.data_blocks.mobs;

import com.robertx22.dungeon_realm.capability.DungeonEntityData;
import com.robertx22.dungeon_realm.main.DataBlockTags;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.dungeon_realm.structure.MobSpawnBlockKind;
import com.robertx22.dungeon_realm.structure.IGetMobSpawnBlockKind;
import com.robertx22.library_of_exile.database.map_data_block.MapBlockCtx;
import com.robertx22.library_of_exile.database.map_data_block.MapDataBlock;
import com.robertx22.library_of_exile.util.wiki.WikiEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class BossMB extends MapDataBlock implements IGetMobSpawnBlockKind {
    public BossMB(String id) {
        super(id, id);

        this.aliases.add("boss_mob");

        this.tags.add(DataBlockTags.CAN_SPAWN_LEAGUE);
    }

    public MobSpawnBlockKind getMobSpawnBlockKind() {
        return MobSpawnBlockKind.MINI_BOSS;
    }

    @Override
    public void processImplementationINTERNAL(String s, BlockPos pos, Level level, CompoundTag nbt, MapBlockCtx ctx) {
        // null when every mob on this list belongs to a mod that is not installed. skipping the
        // spawner costs one empty room; dereferencing it here would abort chunk processing for the
        // whole map - see MobList.getRandomMob.
        var entry = DungeonMain.DUNGEON_MOB_SPAWNS.getPredeterminedRandom(level, pos).getRandomMob();
        if (entry == null) {
            return;
        }
        EntityType<? extends LivingEntity> type = entry.getType();


        MobBuilder.of(type, this, x -> {
            x.amount = 1;

            DungeonEntityData d = new DungeonEntityData();
            d.isMiniBossMob = true;

            x.mobEntityData = d;
        }).summonMobs(level, pos);

    }

    @Override
    public WikiEntry getWikiEntry() {
        return WikiEntry.none();
    }

    @Override
    public Class<?> getClassForSerialization() {
        return BossMB.class;
    }
}
