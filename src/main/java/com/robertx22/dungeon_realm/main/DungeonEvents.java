package com.robertx22.dungeon_realm.main;

import com.robertx22.dungeon_realm.capability.DungeonEntityCapability;
import com.robertx22.dungeon_realm.configs.ObeliskConfig;
import com.robertx22.dungeon_realm.structure.DungeonMapCapability;
import com.robertx22.library_of_exile.dimension.MapDimensions;
import com.robertx22.library_of_exile.events.base.EventConsumer;
import com.robertx22.library_of_exile.events.base.ExileEvents;
import com.robertx22.library_of_exile.util.PointData;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class DungeonEvents {

    public static void init() {

        ExileEvents.MOB_DEATH.register(new EventConsumer<>() {
            @Override
            public void accept(ExileEvents.OnMobDeath event) {
                if (event.mob.level().isClientSide) {
                    return;
                }
                if (MapDimensions.isMap(event.mob.level())) {
                    if (DungeonEntityCapability.get(event.mob).data.isDungeonMob) {
                        DungeonMain.ifMapData(event.mob.level(), event.mob.blockPosition()).ifPresent(x -> {
                            x.rooms.get(event.mob.chunkPosition()).mobs.done++;
                        });
                    }
                    if (DungeonEntityCapability.get(event.mob).data.isDungeonBoss) {
                        if (RandomUtils.roll(ObeliskConfig.get().UBER_FRAG_DROPRATE.get())) {
                            event.mob.spawnAtLocation(DungeonEntries.UBER_FRAGMENT.get().getDefaultInstance());
                        }
                    }
                }
            }
        });
        
        ExileEvents.ON_CHEST_LOOTED.register(new EventConsumer<>() {
            @Override
            public void accept(ExileEvents.OnChestLooted event) {
                if (event.player.level().isClientSide) {
                    return;
                }
                if (MapDimensions.isMap(event.player.level())) {
                    DungeonMain.ifMapData(event.player.level(), event.pos).ifPresent(x -> {
                        x.rooms.get(new ChunkPos(event.pos)).mobs.done++;
                    });
                }
            }
        });

        ExileEvents.MOB_DEATH.register(new EventConsumer<>() {
            @Override
            public void accept(ExileEvents.OnMobDeath event) {
                if (event.mob.level().isClientSide) {
                    return;
                }

                if (DungeonEntityCapability.get(event.mob).data.isDungeonBoss) {
                    if (MapDimensions.isMap(event.mob.level())) {
                        event.mob.level().setBlock(event.mob.blockPosition(), DungeonEntries.REWARD_TELEPORT.get().defaultBlockState(), Block.UPDATE_ALL);

                        DungeonMain.REWARD_ROOM.generateManually((ServerLevel) event.mob.level(), event.mob.chunkPosition());
                        // todo drop another map

                    }
                }
            }
        });

        ExileEvents.PROCESS_CHUNK_DATA.register(new EventConsumer<>() {
            @Override
            public void accept(ExileEvents.OnProcessChunkData event) {
                if (event.struc.guid().equals(DungeonMain.MAIN_DUNGEON_STRUCTURE.guid())) {
                    DungeonMain.ifMapData(event.p.level(), event.cp.getMiddleBlockPosition(5)).ifPresent(x -> {
                        if (x.bonusContents.totalGenDungeonChunks < 1) {
                            var built = DungeonMain.MAIN_DUNGEON_STRUCTURE.getMap(event.cp);
                            built.build();
                            x.bonusContents.totalGenDungeonChunks = built.builtDungeon.amount;
                        }
                    });
                }
            }
        });

        ExileEvents.PROCESS_DATA_BLOCK.register(new EventConsumer<>() {
            @Override
            public void accept(ExileEvents.OnProcessMapDataBlock event) {
                if (event.dataBlock.tags.contains(DataBlockTags.CAN_SPAWN_LEAGUE)) {
                    trySpawnLeagueMechanicIfCan(event.world, event.pos);
                }
            }
        });
    }

    public static void trySpawnLeagueMechanicIfCan(Level world, BlockPos pos) {
        var data = DungeonMapCapability.get(world).data.data.getData(DungeonMain.MAIN_DUNGEON_STRUCTURE, pos);
        float chance = data.bonusContents.calcSpawnChance(pos);

        if (RandomUtils.roll(chance)) {
            data.spawnBonusMapContent(world, pos);
        }
        var cp = new ChunkPos(pos);
        var point = new PointData(cp.x, cp.z);
        data.bonusContents.mechsChunks.add(point);

    }

}
