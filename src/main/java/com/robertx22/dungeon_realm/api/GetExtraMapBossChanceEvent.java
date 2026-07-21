package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEvent;
import net.minecraft.world.entity.player.Player;

import java.util.List;

// Synchronous query, same reasoning as GetPackSizeBonusEvent: dungeon_realm can't see the main
// mod's player Stat pipeline, so it asks for the Atlas additional-boss-chance bonus (percent,
// highest among the players present) via this event. This is the player-stat parallel to the
// EXTRA_MAP_BOSS_CHANCE relic stat.
public class GetExtraMapBossChanceEvent extends ExileEvent {

    public final List<Player> players;
    public float bonusPercent = 0;

    public GetExtraMapBossChanceEvent(List<Player> players) {
        this.players = players;
    }
}
