package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEvent;
import net.minecraft.world.entity.player.Player;

// Synchronous query, same reasoning as GetUberFragmentFindBonusEvent: dungeon_realm asks the main
// mod for the killer's Atlas duplicate-map chance (percent). Killer-based, matching the uber
// fragment drop that shares this boss-death hook.
public class GetDuplicateMapChanceEvent extends ExileEvent {

    public final Player player;
    public float bonusPercent = 0;

    public GetDuplicateMapChanceEvent(Player player) {
        this.player = player;
    }
}
