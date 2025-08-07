package com.robertx22.dungeon_realm.block_entity;

import com.robertx22.dungeon_realm.item.relic.RelicAffixData;
import com.robertx22.dungeon_realm.item.relic.RelicItemData;
import com.robertx22.dungeon_realm.main.DungeonWords;
import com.robertx22.library_of_exile.database.relic.stat.ExactRelicStat;
import com.robertx22.library_of_exile.database.relic.stat.RelicMod;
import com.robertx22.library_of_exile.database.relic.stat.RelicStat;
import com.robertx22.library_of_exile.database.relic.stat.RelicStatsContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This is a full copy of
    @see net.minecraft.client.gui.screens.inventory.ContainerScreen
 */
public class MapDeviceScreen extends AbstractContainerScreen<MapDeviceMenu> implements MenuAccess<MapDeviceMenu> {
    /** The ResourceLocation containing the chest GUI texture. */
    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");
    /** Window height is calculated with these values" the more rows, the higher */
    private final int containerRows;
    private final int LINE_HEIGHT = 11;

    public MapDeviceScreen(MapDeviceMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        int i = 222;
        int j = 114;
        this.containerRows = 3;
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(0, 0, 399); //tooltip has 400, we need to show it. ItemStack has unknown amount but definitely more than 100, so we hide slots behind the UI
        renderRelicStats(pGuiGraphics);
        pGuiGraphics.pose().popPose();
    }

    private void renderRelicStats(GuiGraphics pGuiGraphics) {
        var detailedMode = Screen.hasShiftDown();
        if (detailedMode) {
            drawRelicsInfo(pGuiGraphics);
        } else {
            drawHint(pGuiGraphics);
        }
    }

    private void drawHint(GuiGraphics pGuiGraphics) {
        MutableComponent shiftText = Component.literal("<SHIFT>").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        MutableComponent hintText = DungeonWords.SHOW_RELIC_STATS_HINT.get(shiftText).withStyle(ChatFormatting.GREEN);
        Font minecraftFont = Minecraft.getInstance().font;
        int textWidth = minecraftFont.width(hintText);
        int startY = (this.height - this.imageHeight) / 2;
        int startX = (this.width - this.imageWidth) / 2;
        pGuiGraphics.drawString(minecraftFont, hintText, startX + this.imageWidth - textWidth, startY - LINE_HEIGHT, 0xFFFFFF);
    }

    public void drawRelicsInfo(GuiGraphics pGuiGraphics) {
        var relics = this.menu.getRelics();
        if (relics.isEmpty()) {
            return;
        }

        Map<String, RelicStatWithValue> mappedGroupedRelicStats = getRelicStatWithValueMap(relics);

        Font minecraftFont = Minecraft.getInstance().font;
        var maxLineWidth = 0;
        List<Component> lines = new ArrayList<>();
        for (var relicStatWithValue : mappedGroupedRelicStats.values()) {
            var stat = relicStatWithValue.stat;
            var value = relicStatWithValue.value;
            MutableComponent statText = stat.getTooltip(value);
            lines.add(statText);
            var textWidth = minecraftFont.width(statText);
            maxLineWidth = Math.max(maxLineWidth, textWidth);
        }

        int startY = (this.height - this.imageHeight) / 2;
        drawStatsBackground(pGuiGraphics, maxLineWidth, startY, lines.size());
        drawStats(pGuiGraphics, lines, minecraftFont, startY);
    }

    private void drawStatsBackground(GuiGraphics pGuiGraphics, int maxLineWidth, int startY, int size) {
        int backgroundStartX = (this.width - this.imageWidth) / 2;
        int backgroundEndX = backgroundStartX + this.imageWidth;
        int backgroundEndY = startY + this.imageHeight;
        if (maxLineWidth > this.imageWidth) {
            backgroundStartX -= (maxLineWidth - this.imageWidth) / 2;
            backgroundEndX += (maxLineWidth - this.imageWidth) / 2;
        }
        if (size * LINE_HEIGHT > this.imageWidth) {
            backgroundEndY = startY + size * LINE_HEIGHT;
        }
        pGuiGraphics.fill(backgroundStartX, startY, backgroundEndX, backgroundEndY, 0x80000000);
    }

    private void drawStats(GuiGraphics pGuiGraphics, List<Component> lines, Font minecraftFont, int startY) {
        var y = 2;
        int centeredX = this.width / 2;
        for (var line : lines) {
            pGuiGraphics.drawCenteredString(minecraftFont, line, centeredX, startY + y, 0xFFFFFF);
            y += LINE_HEIGHT;
        }
    }

    private static @NotNull Map<String, RelicStatWithValue> getRelicStatWithValueMap(List<RelicItemData> relics) {
        List<ExactRelicStat> exactRelicStats = new ArrayList<>();

        for (RelicItemData data : relics) {
            for (RelicAffixData affix : data.affixes) {
                for (RelicMod mod : affix.get().mods) {
                    exactRelicStats.add(mod.toExact(affix.p));
                }
            }
        }

        var groupedRelicStats = RelicStatsContainer.calculate(exactRelicStats);
        Map<String, RelicStatWithValue> mappedGroupedRelicStats = new HashMap<>();
        for (var exactRelicStat : exactRelicStats) {
            RelicStat stat = exactRelicStat.getStat();
            if (!mappedGroupedRelicStats.containsKey(stat.id)) {
                mappedGroupedRelicStats.put(stat.id, new RelicStatWithValue(stat, groupedRelicStats.get(stat)));
            }
        }
        return mappedGroupedRelicStats;
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(CONTAINER_BACKGROUND, i, j, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        pGuiGraphics.blit(CONTAINER_BACKGROUND, i, j + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    private record RelicStatWithValue (RelicStat stat, Float value){}
}
