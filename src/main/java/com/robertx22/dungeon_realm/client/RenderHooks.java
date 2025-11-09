package com.robertx22.dungeon_realm.client;

import com.robertx22.dungeon_realm.main.DungeonEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Ensures our HUD overlay actually renders each frame.
 */
@Mod.EventBusSubscriber(modid = "dungeon_realm", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RenderHooks {

    private RenderHooks() {}

    @SubscribeEvent
    public static void onRenderGui(final net.minecraftforge.client.event.RenderGuiEvent.Post e) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && DungeonEvents.isDungeonRealmDimension(level.dimension())) {
            DungeonStatsOverlay.renderHud(e.getGuiGraphics(), e.getPartialTick());
        }
    }
}
