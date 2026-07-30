package com.robertx22.dungeon_realm.api;

import com.robertx22.dungeon_realm.item.DungeonMapGenSettings;
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

    // what the caller asked for. The mns handler re-rolls the map data on a fresh blueprint, so
    // anything the caller decided (like whether this map may inherit a tier floor) has to travel here.
    public DungeonMapGenSettings settings;

    public OnGenerateNewMapItemEvent(ItemStack mapStack, @Nullable Player player, DungeonMapGenSettings settings) {
        this.mapStack = mapStack;
        this.player = player;
        this.settings = settings == null ? new DungeonMapGenSettings() : settings;
    }
}
