package com.robertx22.dungeon_realm.client;

import com.robertx22.dungeon_realm.main.DungeonWords;
import com.robertx22.library_of_exile.database.init.LibDatabase;
import com.robertx22.library_of_exile.localization.TranslationType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class DungeonStatsOverlay {
    private static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation("dungeon_realm", "textures/gui/dungeon_stats_overlay.png");

    // Nine-patch settings
    private static final int TEXTURE_SIZE = 256; // Total texture size in pixels
    private static final int CORNER_SIZE = 8;     // Size of corners that don't stretch
    public static void renderHud(GuiGraphics g, float partialTick) {
        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;
        var window = minecraft.getWindow();

        var mapRarityName = getMapRarityName();
        var killCompletion = getMapKillCompletion();
        var lootCompletion = getMapLootCompletion();

        int contentPadding = 2;
        int totalPadding = contentPadding + CORNER_SIZE;

        int maxWidth = Math.max(
            Math.max(font.width(mapRarityName), font.width(killCompletion)),
            font.width(lootCompletion)
        );

        int boxW = maxWidth + totalPadding * 2;
        int boxH = (font.lineHeight * 4) + totalPadding * 2;
        int edgePadding = 5;

        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();

        int x = screenWidth - boxW - edgePadding;
        int y = (screenHeight - boxH) / 2;

        renderAt(g, x, y, boxW, boxH, mapRarityName, killCompletion, lootCompletion);
    }

    private static MutableComponent getMapLootCompletion() {
        int lootPercent = DungeonStatsStore.getLootCompletionPercent();
        int lootColor = interpolateColor(ChatFormatting.GRAY.getColor(), ChatFormatting.AQUA.getColor(), lootPercent / 100.0f);
        return DungeonWords.DUNGEON_STATS_LOOT_COMPLETION.get(
            Component.literal(String.valueOf(lootPercent)).withStyle(style -> style.withColor(lootColor)),
            Component.literal("100").withStyle(ChatFormatting.AQUA)
        );
    }

    private static MutableComponent getMapKillCompletion() {
        int killPercent = DungeonStatsStore.getKillCompletionPercent();
        int killColor = interpolateColor(ChatFormatting.GRAY.getColor(), ChatFormatting.YELLOW.getColor(), killPercent / 100.0f);
        return DungeonWords.DUNGEON_STATS_KILL_COMPLETION.get(
            Component.literal(String.valueOf(killPercent)).withStyle(style -> style.withColor(killColor)),
            Component.literal("100").withStyle(ChatFormatting.YELLOW)
        );
    }

    private static MutableComponent getMapRarityName() {
        var mapRarityId = DungeonStatsStore.getMapRarityId();
        var mapRarity = LibDatabase.MapFinishRarity().get(mapRarityId);
        return mapRarity.getTranslation(TranslationType.NAME).getTranslatedName();
    }

    public static void renderAt(GuiGraphics g, int x, int y, int boxW, int boxH, Component mapRarityName, Component killCompletion, Component lootCompletion) {
        var font = Minecraft.getInstance().font;

        // Render nine-patch background (with fallback to solid color if texture missing)
        renderNinePatchWithFallback(g, x, y, boxW, boxH);

        // Account for texture border (CORNER_SIZE) + content padding
        int contentPadding = 2;
        int totalPadding = contentPadding + CORNER_SIZE;
        var ty = y + totalPadding;

        var mapRarityId = DungeonStatsStore.getMapRarityId();
        var mapRarity = LibDatabase.MapFinishRarity().get(mapRarityId);
        int centerX = x + boxW / 2;
        var labelColor = mapRarity.textFormatting().getColor();
        g.drawCenteredString(font, mapRarityName, centerX, ty, labelColor);
        ty += font.lineHeight * 2;

        // Right-align kill completion
        int killX = x + boxW - totalPadding - font.width(killCompletion);
        g.drawString(font, killCompletion, killX, ty, 0xFFFFFFFF);
        ty += font.lineHeight;

        // Right-align loot completion
        int lootX = x + boxW - totalPadding - font.width(lootCompletion);
        g.drawString(font, lootCompletion, lootX, ty, 0xFFFFFFFF);
    }

    /**
     * Attempts to render nine-patch texture, falls back to solid color if texture is missing.
     */
    private static void renderNinePatchWithFallback(GuiGraphics g, int x, int y, int width, int height) {
        try {
            renderNinePatch(g, x, y, width, height);
        } catch (Exception e) {
            // Fallback to solid color rendering if texture doesn't exist
            renderSolidBackground(g, x, y, width, height);
        }
    }

    /**
     * Fallback rendering using solid fills (original implementation).
     */
    private static void renderSolidBackground(GuiGraphics g, int x, int y, int width, int height) {
        int bg = 0xFF404040; // Gray background with full opacity
        g.fill(x, y, x + width, y + height, bg);

        // White border
        g.fill(x, y, x + width, y + 1, 0xFFFFFFFF); // Top
        g.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFFF); // Bottom
        g.fill(x, y, x + 1, y + height, 0xFFFFFFFF); // Left
        g.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFFF); // Right
    }

    /**
     * Renders a nine-patch texture that scales dynamically.
     *
     * Nine-patch layout (256x256 texture):
     * ┌──────┬───────────┬──────┐
     * │  TL  │    Top    │  TR  │  Corner size: 8x8
     * │ 8x8  │   8x240   │ 8x8  │  Corners stay fixed
     * ├──────┼───────────┼──────┤
     * │ Left │  Center   │Right │  Edges stretch
     * │240x8 │  240x240  │240x8 │  Center fills/tiles
     * ├──────┼───────────┼──────┤
     * │  BL  │  Bottom   │  BR  │
     * │ 8x8  │   8x240   │ 8x8  │
     * └──────┴───────────┴──────┘
     */
    private static void renderNinePatch(GuiGraphics g, int x, int y, int width, int height) {
        int c = CORNER_SIZE;
        int ts = TEXTURE_SIZE;

        // Corners (never stretch)
        g.blit(OVERLAY_TEXTURE, x, y, 0, 0, c, c, ts, ts); // Top-left
        g.blit(OVERLAY_TEXTURE, x + width - c, y, ts - c, 0, c, c, ts, ts); // Top-right
        g.blit(OVERLAY_TEXTURE, x, y + height - c, 0, ts - c, c, c, ts, ts); // Bottom-left
        g.blit(OVERLAY_TEXTURE, x + width - c, y + height - c, ts - c, ts - c, c, c, ts, ts); // Bottom-right

        // Edges (stretch in one direction)
        g.blit(OVERLAY_TEXTURE, x + c, y, c, 0, width - c * 2, c, ts, ts); // Top edge
        g.blit(OVERLAY_TEXTURE, x + c, y + height - c, c, ts - c, width - c * 2, c, ts, ts); // Bottom edge
        g.blit(OVERLAY_TEXTURE, x, y + c, 0, c, c, height - c * 2, ts, ts); // Left edge
        g.blit(OVERLAY_TEXTURE, x + width - c, y + c, ts - c, c, c, height - c * 2, ts, ts); // Right edge

        // Center (fills the middle)
        g.blit(OVERLAY_TEXTURE, x + c, y + c, c, c, width - c * 2, height - c * 2, ts, ts);
    }

    /**
     * Interpolates between two colors based on a progress value.
     *
     * @param colorStart Starting color (RGB int, e.g., 0xAAAAAA for gray)
     * @param colorEnd   Ending color (RGB int, e.g., 0xFFFF55 for yellow)
     * @param progress   Progress from 0.0 (start color) to 1.0 (end color)
     * @return Interpolated color as RGB int
     */
    private static int interpolateColor(int colorStart, int colorEnd, float progress) {
        // Clamp progress to 0.0 - 1.0 range
        progress = Math.max(0.0f, Math.min(1.0f, progress));

        // Extract RGB components from start color
        int startR = (colorStart >> 16) & 0xFF;
        int startG = (colorStart >> 8) & 0xFF;
        int startB = colorStart & 0xFF;

        // Extract RGB components from end color
        int endR = (colorEnd >> 16) & 0xFF;
        int endG = (colorEnd >> 8) & 0xFF;
        int endB = colorEnd & 0xFF;

        // Interpolate each component
        int r = (int) (startR + (endR - startR) * progress);
        int g = (int) (startG + (endG - startG) * progress);
        int b = (int) (startB + (endB - startB) * progress);

        // Combine back into RGB int
        return (r << 16) | (g << 8) | b;
    }
}
