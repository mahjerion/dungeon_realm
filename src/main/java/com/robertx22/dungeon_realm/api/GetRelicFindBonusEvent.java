package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEvent;
import net.minecraft.world.entity.player.Player;

// Synchronous query, same reasoning as GetUberFragmentFindBonusEvent/GetDuplicateMapChanceEvent.
public class GetRelicFindBonusEvent extends ExileEvent {

    public final Player player;
    public float bonusPercent = 0;

    public GetRelicFindBonusEvent(Player player) {
        this.player = player;
    }
}
