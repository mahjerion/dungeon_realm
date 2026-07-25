package com.robertx22.dungeon_realm.tooltip;

import com.robertx22.dungeon_realm.item.DungeonItemMapData;
import com.robertx22.dungeon_realm.item.DungeonItemNbt;
import com.robertx22.dungeon_realm.main.DungeonWords;
import com.robertx22.library_of_exile.tooltip.TooltipBuilder;
import com.robertx22.library_of_exile.tooltip.TooltipItem;
import com.robertx22.library_of_exile.tooltip.order.ExileTooltipPart;
import com.robertx22.library_of_exile.tooltip.order.TooltipOrder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.robertx22.dungeon_realm.main.DungeonWords.MapGUID;

public class MapTooltip extends TooltipItem {
    public static MapTooltip DEFAULT = new MapTooltip(ItemStack.EMPTY, null);

    public ItemStack stack;
    public DungeonItemMapData data;


    public MapTooltip(ItemStack stack, DungeonItemMapData data) {
        super("dungeon_map");
        this.stack = stack;
        this.data = data;
    }

    public static List<Component> getTooltip(ItemStack stack) {

        var map = DungeonItemNbt.DUNGEON_MAP.loadFrom(stack);

        if (map == null) {
            return Arrays.asList();
        }

        TooltipBuilder<MapTooltip> b = new TooltipBuilder<>(new MapTooltip(stack, map));

        // uber and pinnacle are mutually exclusive on a map item, so only one line ever shows
        if (map.pinnacle) {
            b.add(x -> {
                return new ExileTooltipPart(TooltipOrder.LATE, MapHasPinnacle());
            });
        } else if (map.uber) {
            b.add(x -> {
                return new ExileTooltipPart(TooltipOrder.LATE, MapHasUber());
            });
        }
        b.add(x -> {
            return new ExileTooltipPart(TooltipOrder.LAST, Arrays.asList(
                    DungeonWords.MAP_ITEM_DESC.get().withStyle(ChatFormatting.BLUE),
                    DungeonWords.MAP_ITEM_USE_INFO.get().withStyle(ChatFormatting.BLUE)
            ));
        });

        b.add(x -> new ExileTooltipPart(TooltipOrder.FIRST, MapLayoutName(map.dungeon)));

        return b.build();
    }

    public static @NotNull MutableComponent MapHasUber() {
        return DungeonWords.MAP_HAS_UBER_ARENA.get().withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    }

    // dark red to match the Pinnacle mob rarity color, so it reads as a step above Uber's red
    public static @NotNull MutableComponent MapHasPinnacle() {
        return DungeonWords.MAP_HAS_PINNACLE_ARENA.get().withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
    }

    public static @NotNull MutableComponent MapLayoutName(String dungeon) {
        return DungeonWords.MAP_LAYOUT.get(Component.translatable(MapGUID(dungeon)).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY);
    }
}
