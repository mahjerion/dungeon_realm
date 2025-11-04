package com.robertx22.dungeon_realm.main;

import com.robertx22.dungeon_realm.capability.DungeonEntityCapability;
import com.robertx22.dungeon_realm.capability.DungeonEntityData;
import com.robertx22.dungeon_realm.configs.DungeonConfig;
import com.robertx22.dungeon_realm.database.holders.DungeonMapBlocks;
import com.robertx22.dungeon_realm.database.holders.DungeonRelicStats;
import com.robertx22.dungeon_realm.item.DungeonMapGenSettings;
import com.robertx22.dungeon_realm.item.DungeonMapItem;
import com.robertx22.dungeon_realm.item.relic.RelicGenerator;
import com.robertx22.dungeon_realm.structure.DungeonMapCapability;
import com.robertx22.dungeon_realm.structure.DungeonMapData;
import com.robertx22.library_of_exile.components.LibMapCap;
import com.robertx22.library_of_exile.dimension.MapDimensions;
import com.robertx22.library_of_exile.events.base.EventConsumer;
import com.robertx22.library_of_exile.events.base.ExileEvents;
import com.robertx22.library_of_exile.main.ApiForgeEvents;
import com.robertx22.library_of_exile.main.ExileLog;
import com.robertx22.library_of_exile.util.PointData;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Optional;

import static com.robertx22.dungeon_realm.main.DungeonMain.DIMENSION_KEY;

public class DungeonEvents {

    public static void init() {

        // this way other mods can grab lib data without requiring dungeon realm as a dep. my head hurts
        ExileEvents.GRAB_LIB_MAP_DATA.register(new EventConsumer<ExileEvents.GrabLibMapData>() {
            @Override
            public void accept(ExileEvents.GrabLibMapData event) {
                DungeonMain.ifMapData(event.level, event.pos).ifPresent(x -> {
                    var cap = LibMapCap.get(event.level).data;
                    event.data = cap.getData(DungeonMain.MAIN_DUNGEON_STRUCTURE, event.pos);
                });
            }
        });

        ApiForgeEvents.registerForgeEvent(PlayerEvent.PlayerRespawnEvent.class, event -> {
            var p = event.getEntity();
            DungeonMapData.clearScoreboard(p);
        });

        ApiForgeEvents.registerForgeEvent(PlayerEvent.PlayerChangedDimensionEvent.class, event -> {
            var p = event.getEntity();
            Level level = p.level();
            BlockPos pos = p.blockPosition();
            if (isDungeonRealmDimension(event.getFrom())) {
                DungeonMapData.clearScoreboard(p);
            }

            if (isDungeonRealmDimension(event.getTo())) {
                DungeonMain.ifMapData(level, pos, false).ifPresent(x -> {
                    x.initScoreboard(p);
                });
            }
        });

        ApiForgeEvents.registerForgeEvent(LivingDeathEvent.class, event -> {
            if (event.getEntity().level().isClientSide) {
                return;
            }
            LivingEntity mob = event.getEntity();

            Level level = mob.level();
            BlockPos pos = mob.blockPosition();
            if (MapDimensions.isMap(level)) {

                DungeonEntityData dungeonEntityData = DungeonEntityCapability.get(mob).data;
                if (dungeonEntityData.isFinalMapBoss) {
                    level.setBlock(pos, DungeonEntries.REWARD_TELEPORT.get().defaultBlockState(), Block.UPDATE_ALL);
                    DungeonMain.REWARD_ROOM.generateManually((ServerLevel) level, mob.chunkPosition());
                    // todo drop another map
                }

                // precondition: `isDungeonMob` and `isDungeonEliteMob` should be distinct
                // both should not exist on the same mob
                DungeonMain.ifMapData(level, pos).ifPresent(x -> {
                    if (dungeonEntityData.isDungeonMob) {
                            x.mobKills++;
                    }

                    if (dungeonEntityData.isDungeonEliteMob) {
                        x.eliteKills++;
                    }

                    if (dungeonEntityData.isMiniBossMob) {
                        x.miniBossKills++;
                    }

                    x.updateMapCompletionRarity((ServerLevel) level, pos);
                });



                // hm, 3 relics per uber and 1 per map boss is ok?
                if (dungeonEntityData.isUberBoss) {
                    for (int i = 0; i < 3; i++) {
                        mob.spawnAtLocation(RelicGenerator.randomRelicItem(Optional.empty(), new RelicGenerator.Settings()));
                    }
                }

                if (dungeonEntityData.isFinalMapBoss) {

                    mob.spawnAtLocation(RelicGenerator.randomRelicItem(Optional.empty(), new RelicGenerator.Settings()));

                    var data = LibMapCap.getData(level, pos);

                    float chance = DungeonConfig.get().UBER_FRAG_DROPRATE.get().floatValue();

                    if (data != null) {
                        chance *= 1F + (data.relicStats.get(DungeonRelicStats.INSTANCE.BONUS_BOSS_FRAG_CHANCE.get()) / 100F);
                    }

                    if (RandomUtils.roll(chance)) {
                        mob.spawnAtLocation(DungeonEntries.UBER_FRAGMENT.get().getDefaultInstance());
                    }

                    // todo this isn't ideal
                    if (event.getSource().getEntity() instanceof Player p) {
                        var libdata = LibMapCap.getData(level, pos);
                        if (libdata != null) {
                            float mapchance = libdata.relicStats.get(DungeonRelicStats.INSTANCE.BONUS_MAP_ITEM_FROM_BOSS_CHANCE);
                            if (RandomUtils.roll(mapchance)) {
                                mob.spawnAtLocation(DungeonMapItem.newRandomMapItemStack(new DungeonMapGenSettings()));
                            }
                        }
                    }

                }
            }
        });

        ApiForgeEvents.registerForgeEvent(EntityJoinLevelEvent.class, event -> {
            if (!isDungeonRealmDimension(event.getLevel().dimension()) || !(event.getEntity() instanceof ItemEntity item)) {
                return;
            }

            var level = event.getLevel();
            var pos = item.blockPosition();
            DungeonMain.ifMapData(level, pos).ifPresent(mapData -> {
                item.setInvulnerable(true);
            });
        });

        ExileEvents.DUNGEON_DATA_BLOCK_PLACED.register(new EventConsumer<>() {
            @Override
            public void accept(ExileEvents.DungeonDataBlockPlaced event) {
                var blockNbt = event.blockInfo.nbt();
                if (blockNbt == null) {
                    ExileLog.get().warn("Dungeon Data Block NBT is null");
                    return;
                }
                String blockMetadata;

                if (blockNbt.contains("metadata")) { // structure block
                    blockMetadata = blockNbt.getString("metadata");
                } else if (blockNbt.contains("Command")) { // command block
                    blockMetadata = blockNbt.getString("Command");
                } else {
                    blockMetadata = "unknown";
                }

                var serverLevel = event.levelAccessor.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, DIMENSION_KEY));
                if (DungeonMain.MAP.isInside(DungeonMain.MAIN_DUNGEON_STRUCTURE, serverLevel, event.pos)) {
                    DungeonMain.ifMapData(serverLevel, event.pos).ifPresent(mapData -> {
                        var mobSpawnBlockKind = DungeonMapBlocks.getMobSpawnBlockKindFromBlockMetadata(blockMetadata);
                        mobSpawnBlockKind.ifPresent(mapData::incrementSpawnBlockCountByKind);
                    });
                }
            }
        });


        ExileEvents.ON_CHEST_LOOTED.register(new EventConsumer<>() {
            @Override
            public void accept(ExileEvents.OnChestLooted event) {
                Level level = event.player.level();
                if (level.isClientSide) {
                    return;
                }
                if (MapDimensions.isMap(level)) {
                    BlockPos pos = event.pos;
                    DungeonMain.ifMapData(level, pos).ifPresent(x -> {
                        x.lootedChests++;
                        x.updateMapLootCompletion((ServerLevel) level, pos);
                    });
                }
            }
        });


        ExileEvents.PROCESS_CHUNK_DATA.register(new EventConsumer<>() {
            @Override
            public void accept(ExileEvents.OnProcessChunkData event) {
                if (event.struc.guid().equals(DungeonMain.MAIN_DUNGEON_STRUCTURE.guid())) {
                    DungeonMain.ifMapData(event.p.level(), event.cp.getMiddleBlockPosition(5)).ifPresent(x -> {
                        x.bonusContents.processedChunks++;
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

    private static boolean isDungeonRealmDimension(ResourceKey<Level> levelResourceKey) {
        return levelResourceKey.location().compareTo(DIMENSION_KEY) == 0;
    }

    // we're only spawning bonus content in the main map dim+structure
    public static void trySpawnLeagueMechanicIfCan(Level world, BlockPos pos) {
        if (DungeonMain.MAP.isInside(DungeonMain.MAIN_DUNGEON_STRUCTURE, (ServerLevel) world, pos)) {
            var data = DungeonMapCapability.get(world).data.data.getData(DungeonMain.MAIN_DUNGEON_STRUCTURE, pos);
            if (data != null) {
                float chance = data.bonusContents.calcSpawnChance(pos);

                if (RandomUtils.roll(chance)) {
                    data.spawnBonusMapContent(world, pos);
                }
                var cp = new ChunkPos(pos);
                var point = new PointData(cp.x, cp.z);
                data.bonusContents.mechsChunks.add(point);
            }
        }
    }

}
