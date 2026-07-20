package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEvent;
import net.minecraft.world.entity.player.Player;

import java.util.List;

// Fired when the map's final boss dies. Carries everyone physically present so listeners
// can decide eligibility themselves - e.g. the Atlas system only credits players who had
// already unlocked this dungeon's node (same players who could have targeted/rolled it
// themselves), not just whoever happened to tag along.
public class OnMapFullyClearedEvent extends ExileEvent {

    public final String dungeonGuid;
    public final List<Player> players;

    public OnMapFullyClearedEvent(String dungeonGuid, List<Player> players) {
        this.dungeonGuid = dungeonGuid;
        this.players = players;
    }
}
