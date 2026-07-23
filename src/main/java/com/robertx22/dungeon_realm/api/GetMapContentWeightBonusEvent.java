package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEvent;
import net.minecraft.world.entity.player.Player;

import java.util.List;

// Synchronous query, same reasoning as GetPackSizeBonusEvent: dungeon_realm can't see the main mod's
// player Stat pipeline, so it asks for the Atlas "event chance" bonus for a given map-content id
// (percent, highest among the players present). Used to weight which league content spawns as bonus
// content in a map - the player-stat parallel to ContentWeightRS relic stats.
public class GetMapContentWeightBonusEvent extends ExileEvent {

    public final List<Player> players;
    public final String contentId;
    public float bonusPercent = 0;

    // set true to exclude this content from the bonus-content pool entirely (e.g. below the
    // main mod's configured min level for this league mechanic in maps) - distinct from a
    // negative bonusPercent, which only lowers weight and still risks the all-zero-weight
    // fallback in RandomUtils.weightedRandom picking it uniformly anyway.
    public boolean blocked = false;

    public GetMapContentWeightBonusEvent(List<Player> players, String contentId) {
        this.players = players;
        this.contentId = contentId;
    }
}
