package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEvent;
import net.minecraft.world.entity.player.Player;

// Synchronous query: dungeon_realm asks the main mod for the player's Atlas additional-bonus-event
// chance (percent). This is the player-stat parallel to the BONUS_CONTENT_CHANCE relic stat - it
// rolls for one extra bonus content (league event) when a map starts.
public class GetBonusContentChanceEvent extends ExileEvent {

    public final Player player;
    public float bonusPercent = 0;

    public GetBonusContentChanceEvent(Player player) {
        this.player = player;
    }
}
