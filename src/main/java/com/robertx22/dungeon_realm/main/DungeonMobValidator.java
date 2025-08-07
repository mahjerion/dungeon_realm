package com.robertx22.dungeon_realm.main;

import com.robertx22.dungeon_realm.capability.DungeonEntityCapability;
import com.robertx22.library_of_exile.dimension.MobValidator;
import net.minecraft.world.entity.LivingEntity;

public class DungeonMobValidator extends MobValidator {
    @Override
    public boolean isValidMob(LivingEntity en) {
        return DungeonEntityCapability.get(en).data.isDungeonMob ||
                DungeonEntityCapability.get(en).data.isDungeonEliteMob ||
                DungeonEntityCapability.get(en).data.isMiniBossMob ||
                DungeonEntityCapability.get(en).data.isFinalMapBoss ||
                DungeonEntityCapability.get(en).data.isUberBoss;
    }
}
