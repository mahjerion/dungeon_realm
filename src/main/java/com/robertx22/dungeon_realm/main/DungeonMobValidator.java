package com.robertx22.dungeon_realm.main;

import com.robertx22.dungeon_realm.capability.DungeonEntityCapability;
import com.robertx22.dungeon_realm.capability.DungeonEntityData;
import com.robertx22.library_of_exile.dimension.MobValidator;
import net.minecraft.world.entity.LivingEntity;

public class DungeonMobValidator extends MobValidator {
    @Override
    public boolean isValidMob(LivingEntity en) {
        DungeonEntityData data = DungeonEntityCapability.get(en).data;

        return data.isDungeonMob ||
                data.isDungeonEliteMob ||
                data.isMiniBossMob ||
                data.isFinalMapBoss ||
                data.isUberBoss ||
                data.isPinnacleBoss ||
                // the bonus encounters (Strongbox, Imprisoned Monster) deliberately no longer tag their
                // mobs as dungeon/mini-boss mobs, so that opening one doesn't move the map's exploration
                // goal. They're still legitimately spawned map content though, and this validator is the
                // loot gate (OnMobDeathDrops) - without these two they'd silently drop nothing at all,
                // including the league-tagged uniques the whole encounter exists to award.
                data.isStrongboxGuardian ||
                data.isImprisonedMonster;
    }
}
