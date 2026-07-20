package com.robertx22.dungeon_realm.block;

import com.robertx22.dungeon_realm.api.DungeonExileEvents;
import com.robertx22.dungeon_realm.api.SpawnPinnacleEvent;
import com.robertx22.dungeon_realm.api.SpawnUberEvent;
import com.robertx22.dungeon_realm.capability.DungeonEntityCapability;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.dimension.MapDimensions;
import com.robertx22.library_of_exile.localization.TranslationType;
import com.robertx22.library_of_exile.utils.SoundUtils;
import com.robertx22.library_of_exile.utils.geometry.MyPosition;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class UberBossAltarBlock extends Block {

    public UberBossAltarBlock() {
        super(BlockBehaviour.Properties.of().strength(200).noOcclusion());
    }

    @Override
    public InteractionResult use(BlockState pState, Level level, BlockPos pPos, Player p, InteractionHand pHand, BlockHitResult pHit) {


        if (!level.isClientSide) {

            try {
                if (MapDimensions.isMap(level)) {

                    DungeonMain.ifMapData(level, pPos).ifPresent(x -> {
                        var uber = DungeonMain.UBER_ARENA.getUber(new ChunkPos(pPos));
                        // reads THIS map's own recorded upgrade tier - same outcome for every
                        // player in the run, no per-player check (a Pinnacle Map still needs the
                        // pinnacle boss pool actually populated for this arena template)
                        boolean pinnacle = x.item.pinnacle && !uber.possible_pinnacle_bosses.isEmpty();
                        var type = pinnacle ? uber.getRandomPinnacleBoss() : uber.getRandomBoss();

                        LivingEntity en = (LivingEntity) type.create(level);
                        en.setPos(new MyPosition(pPos).add(0, 1, 0));

                        p.sendSystemMessage(uber.getTranslation(TranslationType.DESCRIPTION).getTranslatedName().withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                        SoundUtils.playSound(p, SoundEvents.WITHER_SPAWN);
                        level.setBlock(pPos, Blocks.AIR.defaultBlockState(), 0);

                        level.addFreshEntity(en);

                        if (pinnacle) {
                            DungeonEntityCapability.get(en).data.isPinnacleBoss = true;
                            DungeonExileEvents.ON_SPAWN_PINNACLE_BOSS.callEvents(new SpawnPinnacleEvent(en));
                        } else {
                            DungeonEntityCapability.get(en).data.isUberBoss = true;
                            DungeonExileEvents.ON_SPAWN_UBER_BOSS.callEvents(new SpawnUberEvent(en));
                        }

                        if (en instanceof Mob mob) {
                            mob.setPersistenceRequired();
                        }


                    });


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return InteractionResult.SUCCESS;
    }
}
