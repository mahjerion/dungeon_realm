package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.List;

// Fired when the map's final boss dies. Carries everyone physically present so listeners
// can decide eligibility themselves - e.g. the Atlas system only credits players who had
// already unlocked this dungeon's node (same players who could have targeted/rolled it
// themselves), not just whoever happened to tag along. uber/level/pos let listeners in the
// main mod resolve this specific run's rolled attributes (e.g. MapItemData tier/rarity via
// WorldData) to gate per-node completion requirements.
public class OnMapFullyClearedEvent extends ExileEvent {

    public final String dungeonGuid;
    public final List<Player> players;
    public final boolean uber;
    public final boolean pinnacle;
    public final ServerLevel level;
    public final BlockPos pos;

    public OnMapFullyClearedEvent(String dungeonGuid, List<Player> players, boolean uber, boolean pinnacle, ServerLevel level, BlockPos pos) {
        this.dungeonGuid = dungeonGuid;
        this.players = players;
        this.uber = uber;
        this.pinnacle = pinnacle;
        this.level = level;
        this.pos = pos;
    }
}
