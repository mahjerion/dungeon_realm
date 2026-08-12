package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// Synchronous query: dungeon_realm asks the main mod for its share of the additional-bonus-content
// chance pool (percent). That's the player's Atlas additional-event stat plus whatever the map item
// itself contributes (higher tier maps have a higher chance at more league mechanics), which is why
// the map stack comes along - dungeon_realm doesn't know about map tiers.
//
// The returned percent is summed with the BONUS_CONTENT_CHANCE relic stat into a single pool, so
// these sources add up instead of each being its own separate coin flip. Every full 100% in the pool
// is one guaranteed extra content and the remainder is rolled once - see MapBonusContentsData.
public class GetBonusContentChanceEvent extends ExileEvent {

    public final Player player;
    public final ItemStack mapStack;
    public float bonusPercent = 0;

    public GetBonusContentChanceEvent(Player player, ItemStack mapStack) {
        this.player = player;
        this.mapStack = mapStack;
    }
}
