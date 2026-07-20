package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEvent;
import net.minecraft.world.entity.player.Player;

// Fired by MapDeviceBlock when a player right-clicks it with no relic key/map item and no map
// currently bound - the main mod listens and tells that player's client to open the Atlas Map screen.
public class OpenAtlasMapEvent extends ExileEvent {

    public final Player player;

    public OpenAtlasMapEvent(Player player) {
        this.player = player;
    }
}
