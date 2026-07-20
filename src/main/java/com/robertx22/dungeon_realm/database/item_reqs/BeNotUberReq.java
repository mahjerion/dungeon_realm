package com.robertx22.dungeon_realm.database.item_reqs;

import com.robertx22.dungeon_realm.item.DungeonItemNbt;
import com.robertx22.orbs_of_crafting.misc.StackHolder;
import net.minecraft.world.entity.player.Player;

// shared guard for both the Uber Upgrade and Pinnacle Upgrade orbs - a map can only ever hold
// one of the two upgrade tiers, so applying either orb requires the map to have neither yet
public class BeNotUberReq extends BeItemTypeRequirement {
    public BeNotUberReq(String id) {
        super(id, "Must not already be Uber or Pinnacle");
    }

    @Override
    public Class<?> getClassForSerialization() {
        return BeNotUberReq.class;
    }

    @Override
    public boolean isValid(Player p, StackHolder obj) {

        if (!DungeonItemNbt.DUNGEON_MAP.has(obj.stack)) {
            return false;
        }

        var map = DungeonItemNbt.DUNGEON_MAP.loadFrom(obj.stack);

        return !map.uber && !map.pinnacle;
    }
}
