package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEvent;
import net.minecraft.world.entity.player.Player;

import java.util.List;

// Synchronous query, same reasoning as GetUnlockedAtlasNodesEvent: dungeon_realm can't see
// the main mod's player Stat pipeline, so it asks for the Atlas pack_size bonus (percent,
// highest among the players present) via this event instead.
public class GetPackSizeBonusEvent extends ExileEvent {

    public final List<Player> players;
    public float bonusPercent = 0;

    public GetPackSizeBonusEvent(List<Player> players) {
        this.players = players;
    }
}
