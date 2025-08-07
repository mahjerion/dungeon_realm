package com.robertx22.dungeon_realm.block_entity;

import com.robertx22.dungeon_realm.item.relic.RelicAffixData;
import com.robertx22.dungeon_realm.item.relic.RelicItemData;
import com.robertx22.library_of_exile.database.relic.stat.ExactRelicStat;
import com.robertx22.library_of_exile.database.relic.stat.RelicMod;
import com.robertx22.library_of_exile.database.relic.stat.RelicStat;
import com.robertx22.library_of_exile.database.relic.stat.RelicStatsContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

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
        renderRelicStats(pGuiGraphics);
    }

    private void renderRelicStats(GuiGraphics pGuiGraphics) {
        var relics = this.menu.getRelics();
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

        Font minecraftFont = Minecraft.getInstance().font;
        var maxWidth = 0;
        List<Component> lines = new ArrayList<>();
        for (var relicStatWithValue : mappedGroupedRelicStats.values()) {
            var stat = relicStatWithValue.stat;
            var value = relicStatWithValue.value;
            MutableComponent statText = stat.getTooltip(value);
            lines.add(statText);
            var textWidth = minecraftFont.width(statText);
            maxWidth = Math.max(maxWidth, textWidth);
        }
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2 + 3;
        int statsX = startX + this.imageWidth;
        pGuiGraphics.fill(statsX, startY, statsX + maxWidth, startY + lines.size() * 11, 0x80000000);
        var y = 2;
        for (var line : lines) {
            pGuiGraphics.drawString(minecraftFont, line, statsX, startY + y, 0xFFFFFF, false);
            y += 11;
        }
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(CONTAINER_BACKGROUND, i, j, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        pGuiGraphics.blit(CONTAINER_BACKGROUND, i, j + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    private record RelicStatWithValue (RelicStat stat, Float value){}
}
