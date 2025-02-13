package com.robertx22.dungeon_realm.main;

import com.robertx22.library_of_exile.main.ApiForgeEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class ObeliskRewardLogic {


    public static void init() {

        ApiForgeEvents.registerForgeEvent(LivingDeathEvent.class, x -> {
            LivingEntity en = x.getEntity();
            var world = en.level();
            if (!world.isClientSide) {
                DungeonMain.ifMapData(world, en.blockPosition()).ifPresent(d -> {
                    // d.mob_kills++;
                });
            }
        });

    }
}
