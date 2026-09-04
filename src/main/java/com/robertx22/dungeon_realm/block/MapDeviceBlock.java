package com.robertx22.dungeon_realm.block;

import com.robertx22.dungeon_realm.api.CanEnterMapEvent;
import com.robertx22.dungeon_realm.api.DungeonExileEvents;
import com.robertx22.dungeon_realm.api.OnStartMapEvent;
import com.robertx22.dungeon_realm.block_entity.MapDeviceBE;
import com.robertx22.dungeon_realm.database.holders.DungeonMapBlocks;
import com.robertx22.dungeon_realm.item.DungeonItemMapData;
import com.robertx22.dungeon_realm.item.DungeonItemNbt;
import com.robertx22.dungeon_realm.item.DungeonMapItem;
import com.robertx22.dungeon_realm.main.DungeonEntries;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.dungeon_realm.structure.DungeonMapCapability;
import com.robertx22.dungeon_realm.structure.DungeonMapData;
import com.robertx22.library_of_exile.components.LibMapCap;
import com.robertx22.library_of_exile.components.LibMapData;
import com.robertx22.library_of_exile.components.PlayerDataCapability;
import com.robertx22.library_of_exile.database.init.LibDatabase;
import com.robertx22.library_of_exile.database.relic.stat.RelicStatsContainer;
import com.robertx22.library_of_exile.dimension.MapDimensions;
import com.robertx22.library_of_exile.events.base.ExileEvents;
import com.robertx22.library_of_exile.utils.TeleportUtils;
import com.robertx22.library_of_exile.utils.geometry.Circle2d;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class MapDeviceBlock extends BaseEntityBlock {
    public MapDeviceBlock() {
        super(BlockBehaviour.Properties.of().strength(10).noOcclusion().lightLevel(x -> 10));
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {

        List<ItemStack> all = new ArrayList<>();

        BlockEntity blockentity = pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        if (blockentity instanceof MapDeviceBE be) {
            all.add(asItem().getDefaultInstance());

            for (int i = 0; i < be.deviceInv.getContainerSize(); i++) {
                var s = be.deviceInv.getItem(i);
                if (!s.isEmpty()) {
                    all.add(s.copy());
                }
            }
            for (ItemStack s : be.pendingSpill) {
                if (!s.isEmpty()) {
                    all.add(s.copy());
                }
            }
        }

        return all;
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {

        try {
            if (pLevel.isClientSide) {
                var particle = ParticleTypes.WITCH;

                Circle2d c = new Circle2d(pPos, 1.5F);
                SimpleParticleType finalParticle = particle;
                c.doXTimes(5, x -> {
                    c.spawnParticle(pLevel, c.getRandomEdgePos(), finalParticle);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param relics resolved exactly once, right where the instance data is written. The device GUI consumes
     *               relic uses inside this supplier, so a start that never reaches this point (gated by
     *               CanStartMapEvent before the call) costs nothing. May yield null when no relics are slotted.
     */
    public static void startNewMap(Player p, ItemStack stack, MapDeviceBE be, Supplier<RelicStatsContainer> relics) {

        try {
            DungeonItemMapData map = DungeonItemNbt.DUNGEON_MAP.loadFrom(stack);

            var count = map.getOrSetStartPos(p.level(), stack);
            var start = DungeonMain.MAIN_DUNGEON_STRUCTURE.getStartFromCounter(count.x, count.z);

            //var pdata = PlayerDataCapability.get(p);

            var data = new DungeonMapData();
            data.item = map;
            data.x = start.x;
            data.z = start.z;
            data.dungeon = map.dungeon;
            if (data.dungeon == null || data.dungeon.isEmpty()) {
                data.dungeon = DungeonMapItem.GetRandomDungeonGUID(p); //TODO: so this is just backwards support, but apparently we can never remove it to support old maps without predefined dungeon...
            }

            var libdata = new LibMapData();
            RelicStatsContainer relicStats = relics == null ? null : relics.get();
            libdata.relicStats = relicStats == null ? new RelicStatsContainer(new HashMap<>()) : relicStats;

            data.bonusContents.setupOnMapStart(stack, libdata, p);

            // snapshot the map item before it's consumed, so the MapScreen icon can show its real tooltip
            data.setSnapshotFrom(stack);

            DungeonMapCapability.get(p.level()).data.data.setData(p, data, DungeonMain.MAIN_DUNGEON_STRUCTURE, start.getMiddleBlockPosition(5));
            LibMapCap.get(p.level()).data.setData(p, libdata, DungeonMain.MAIN_DUNGEON_STRUCTURE, start.getMiddleBlockPosition(5));

            // must come AFTER the map data is written: the spawn pos needs the dungeon's room size to
            // center the player in the entrance room, and that's only knowable from the saved map data.
            // computed earlier it would fall back to a random dungeon and offset by the wrong room size.
            var struc = DungeonMain.MAIN_DUNGEON_STRUCTURE;
            var def = TeleportUtils.getSpawnTeleportPos(struc, start.getMiddleBlockPosition(5));

            // a builder can override the landing spot with a player_spawn data block. the entrance room
            // is always the middle cell of the grid, whose origin chunk is the dungeon's start chunk,
            // which is what keeps this to entrance rooms only. none placed = the old center spawn.
            var marker = LibDatabase.MapDataBlocks().get(DungeonMapBlocks.INSTANCE.PLAYER_SPAWN.GUID());
            var pos = struc.findDataBlockInRoom(p.getServer(), start, s -> marker.matches(s, null, null, null), def)
                    .orElse(def);

            // same map the secondary structures record theirs in, so re-entry paths resolve it too
            data.spawnPositions.put(struc.guid(), pos.asLong());

            be.pos = pos;
            be.currentWorldUUID = DungeonMapCapability.getFromServer().data.data.uuid;

            be.setChanged();

            // todo
            var event = new OnStartMapEvent(p, stack, start, DungeonMain.MAP);
            DungeonExileEvents.ON_START_NEW_MAP.callEvents(event);

            // the stack is the one sitting in the device's map slot, so this empties the slot
            stack.shrink(1);
            be.deviceInv.setChanged();

            // true: this is the instance-creating entry, the only one the spawn grace is for
            if (joinCurrentMap(p, be, true)) {
                // deferred to arrival, not done here. joinCurrentMap only SCHEDULES the teleport, so
                // at this point the instance's chunks are still generating - and setBlock into a
                // chunk that isn't loaded is a blocking, generate-if-missing load on the server
                // thread. Doing it inline was 4% of all time spent in ticks over 100ms, and it
                // blocked on the very chunk the teleport had just asked to be loaded in the
                // background, which cancelled out the whole point of waiting for it.
                var dungeonLevel = p.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, DungeonMain.DIMENSION_KEY));
                BlockPos devicePos = pos.south();
                Runnable placeReturnDevice = () -> dungeonLevel.setBlock(devicePos,
                        DungeonEntries.MAP_DEVICE_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);

                var cap = PlayerDataCapability.get(p);
                var delayed = cap == null ? null : cap.delayedTeleportData;
                if (delayed != null) {
                    delayed.onArrival = placeReturnDevice;
                } else {
                    placeReturnDevice.run();
                }
            }


        } catch (Exception e) {
            // failing anywhere between getOrSetStartPos and setData burns an instance slot and leaves the
            // item carrying its coordinates, so the next attempt with it silently re-enters an instance
            // that was never written. that used to look like nothing happening at all - say so.
            DungeonMain.LOG.error("Failed to start a dungeon map for " + p.getScoreboardName() + ".", e);
            p.sendSystemMessage(Component.literal("Failed to start the map, check the server log.").withStyle(ChatFormatting.RED));
        }
    }

    /**
     * @param grace whether this entry gets the spawn grace. Only true when called from startNewMap,
     *              which is the entry that creates the instance - a player walking back into a dungeon
     *              that is already running arrives among content that already exists, so the grace would
     *              protect nobody while holding back the chunks they haven't reached yet.
     */
    public static boolean joinCurrentMap(Player p, MapDeviceBE be, boolean grace) {

        var event = new CanEnterMapEvent(p, be);
        DungeonExileEvents.CAN_ENTER_MAP.callEvents(event);
        if (!event.canEnter) {
            return false;
        }

        var pdata = PlayerDataCapability.get(p);
        // false means a teleport was already in flight for this player and this one was ignored, so say
        // so rather than reporting a join that isn't happening - startNewMap keys its arrival work off
        // this return, and doing that work for a teleport nobody scheduled would attach it to the
        // teleport already on its way.
        if (!pdata.mapTeleports.entranceTeleportLogic(p, DungeonMain.DIMENSION_KEY, be.pos, grace)) {
            return false;
        }

        // the entrance teleport is delayed and stats packets are otherwise only sent on kill/chest events,
        // so sync the joining player now, otherwise their client keeps showing the previous map's data.
        var dungeonLevel = p.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, DungeonMain.DIMENSION_KEY));
        if (dungeonLevel != null) {
            DungeonMain.ifMapData(dungeonLevel, be.pos).ifPresent(x -> x.sendStatsToPlayer(p));
        }
        return true;
    }

    @Override
    public InteractionResult use(BlockState pState, Level world, BlockPos pPos, Player p, InteractionHand pHand, BlockHitResult pHit) {

        if (!world.isClientSide) {
            var be = world.getBlockEntity(pPos);

            if (be instanceof MapDeviceBE obe) {

                // the return device placed inside the instance: leave, or shift to go all the way home
                if (MapDimensions.isMap(world)) {
                    if (p.isCrouching()) {
                        PlayerDataCapability.get(p).mapTeleports.teleportHome(p);
                    } else {
                        PlayerDataCapability.get(p).mapTeleports.exitTeleportLogic(p);
                    }
                    return InteractionResult.SUCCESS;
                }

                // everything else - slotting the map and relics, the atlas, starting or joining - goes
                // through the shared device GUI, which the main mod opens for this player
                ExileEvents.OPEN_MAP_DEVICE.callEvents(new ExileEvents.OpenMapDeviceEvent(p, world, pPos));
            }
        }

        return InteractionResult.SUCCESS;
    }


    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new MapDeviceBE(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide) {
            return null;
        }
        return (level, pos, state, blockEntity) -> {
            if (blockEntity instanceof MapDeviceBE be && !be.pendingSpill.isEmpty()) {
                // relics from a pre-rework device that had no slot to migrate into, see MapDeviceBE.load
                for (ItemStack stack : be.pendingSpill) {
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack);
                }
                be.pendingSpill.clear();
                be.setChanged();
            }
        };
    }

}
