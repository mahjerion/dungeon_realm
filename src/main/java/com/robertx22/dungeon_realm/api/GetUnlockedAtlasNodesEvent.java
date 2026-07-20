package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEvent;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

// Synchronous query: dungeon_realm can't see the main mod's player capability that
// tracks Atlas progress, so it asks for the player's unlocked Atlas node ids via this
// event instead. Listener is expected to fill in unlockedNodeIds before the event returns.
public class GetUnlockedAtlasNodesEvent extends ExileEvent {

    public final Player player;
    public Set<String> unlockedNodeIds = new HashSet<>();

    public GetUnlockedAtlasNodesEvent(Player player) {
        this.player = player;
    }
}
