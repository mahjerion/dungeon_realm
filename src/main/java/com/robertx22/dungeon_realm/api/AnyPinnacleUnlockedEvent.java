package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEvent;
import net.minecraft.world.entity.player.Player;

import java.util.List;

// Synchronous query, same reasoning as GetPackSizeBonusEvent: dungeon_realm can't see the main
// mod's AtlasData, so it asks whether anyone present has finished the Atlas pinnacle branch.
public class AnyPinnacleUnlockedEvent extends ExileEvent {

    public final List<Player> players;
    public boolean anyUnlocked = false;

    public AnyPinnacleUnlockedEvent(List<Player> players) {
        this.players = players;
    }
}
