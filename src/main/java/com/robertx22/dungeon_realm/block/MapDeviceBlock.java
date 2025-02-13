package com.robertx22.dungeon_realm.block;

import com.robertx22.dungeon_realm.api.CanEnterMapEvent;
import com.robertx22.dungeon_realm.api.CanStartMapEvent;
import com.robertx22.dungeon_realm.api.DungeonExileEvents;
import com.robertx22.dungeon_realm.block_entity.MapDeviceBE;
import com.robertx22.dungeon_realm.item.DungeonItemMapData;
import com.robertx22.dungeon_realm.item.ObeliskItemNbt;
import com.robertx22.dungeon_realm.item.ObeliskMapItem;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.dungeon_realm.structure.DungeonMapCapability;
import com.robertx22.dungeon_realm.structure.DungeonMapData;
import com.robertx22.library_of_exile.components.PlayerDataCapability;
import com.robertx22.library_of_exile.dimension.MapDimensions;
import com.robertx22.library_of_exile.utils.PlayerUtil;
import com.robertx22.library_of_exile.utils.SoundUtils;
import com.robertx22.library_of_exile.utils.geometry.Circle2d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MapDeviceBlock extends BaseEntityBlock {
    public MapDeviceBlock() {
        super(BlockBehaviour.Properties.of().strength(10).noOcclusion().lightLevel(x -> 10));
    }

    @Override
    public void onRemove(BlockState pState, Level level, BlockPos p, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity blockentity = level.getBlockEntity(p);
            if (blockentity instanceof MapDeviceBE be) {
                if (level instanceof ServerLevel) {
                    ItemEntity en = new ItemEntity(level, p.getX(), p.getY(), p.getZ(), asItem().getDefaultInstance());
                    level.addFreshEntity(en);
                }
            }
        }
        super.onRemove(pState, level, p, pNewState, pIsMoving);
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

    public static void startNewMap(Player p, ItemStack stack, MapDeviceBE be) {

        DungeonItemMapData map = ObeliskItemNbt.OBELISK_MAP.loadFrom(stack);

        var count = map.getOrSetStartPos(p.level(), stack);
        var start = DungeonMain.MAIN_DUNGEON_STRUCTURE.getStartFromCounter(count.x, count.z);
        var pos = DungeonMain.MAIN_DUNGEON_STRUCTURE.getSpawnTeleportPos(start.getMiddleBlockPosition(5));

        var pdata = PlayerDataCapability.get(p);

        var data = new DungeonMapData();
        data.item = map;
        data.x = start.x;
        data.z = start.z;

        be.pos = pos;

        be.setChanged();

        stack.shrink(1);

        DungeonMapCapability.get(p.level()).data.data.setData(p, data, DungeonMain.MAIN_DUNGEON_STRUCTURE, start.getMiddleBlockPosition(5));

        pdata.mapTeleports.entranceTeleportLogic(p, DungeonMain.DIMENSION_KEY, pos);
    }

    public static void joinCurrentMap(Player p, MapDeviceBE be) {

        var event = new CanEnterMapEvent(p, be);
        DungeonExileEvents.CAN_ENTER_MAP.callEvents(event);
        if (!event.canEnter) {
            return;
        }

        var pdata = PlayerDataCapability.get(p);
        pdata.mapTeleports.entranceTeleportLogic(p, DungeonMain.DIMENSION_KEY, be.pos);
    }

    @Override
    public InteractionResult use(BlockState pState, Level world, BlockPos pPos, Player p, InteractionHand pHand, BlockHitResult pHit) {

        if (!world.isClientSide) {
            var be = world.getBlockEntity(pPos);

            if (be instanceof MapDeviceBE obe) {

                if (MapDimensions.isMap(world)) {
                    PlayerDataCapability.get(p).mapTeleports.teleportHome(p);
                    return InteractionResult.SUCCESS;
                }
                ItemStack stack = p.getMainHandItem();

                var event = new CanStartMapEvent(stack, p);

                DungeonExileEvents.CAN_START_MAP.callEvents(event);

                if (!event.canEnter) {
                    return InteractionResult.SUCCESS;
                }

                if (ObeliskItemNbt.OBELISK_MAP.has(stack)) {
                    startNewMap(p, stack, obe);
                } else {
                    if (obe.isActivated()) {
                        joinCurrentMap(p, obe);
                    } else {
                        // todo fix this later
                        if (!obe.gaveMap) {
                            obe.setGaveMap();
                            var map = ObeliskMapItem.blankMap();
                            PlayerUtil.giveItem(map, p);
                            SoundUtils.playSound(p, SoundEvents.ITEM_PICKUP);
                        }
                    }

                }

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
        return new BlockEntityTicker<T>() {
            @Override
            public void tick(Level pLevel, BlockPos pPos, BlockState pState, T pBlockEntity) {
                // todo
            }
        };
    }

}
