package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class OnGenerateNewMapItemEvent extends ExileEvent {

    public ItemStack mapStack;

    // the player the map is being generated for (may be null for context-less generation). Lets the
    // main mod apply player-driven map rolls (e.g. the Atlas map_rarity_bias stat) instead of a dummy.
    @Nullable
    public Player player;

    public OnGenerateNewMapItemEvent(ItemStack mapStack, @Nullable Player player) {
        this.mapStack = mapStack;
        this.player = player;
    }
}
