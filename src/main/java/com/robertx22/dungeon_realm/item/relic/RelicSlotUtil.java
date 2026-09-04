package com.robertx22.dungeon_realm.item.relic;

import com.robertx22.dungeon_realm.item.DungeonItemNbt;
import com.robertx22.library_of_exile.database.relic.stat.ExactRelicStat;
import com.robertx22.library_of_exile.database.relic.stat.RelicStat;
import com.robertx22.library_of_exile.database.relic.stat.RelicStatsContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The relic rules of a map device, over any {@link Container} slot range, so the placement check, the
 * consumption on map start and the stat preview all read the same slots the same way. The main mod's
 * device GUI calls these for the dungeon, harvest and obelisk devices alike - those two addons never see
 * relic NBT themselves, they only receive the resulting {@link RelicStatsContainer}.
 */
public class RelicSlotUtil {

    /**
     * The relics in the slot range, in slot order, already reduced to the ones that would actually apply
     * (first slots win within each type's max_equipped cap).
     */
    public static List<RelicItemData> loadEquippable(Container inv, int from, int count) {
        return RelicItemData.filterEquippable(new ArrayList<>(loadAll(inv, from, count).values()));
    }

    /** slot index -> relic data for every relic in the range, in slot order */
    private static LinkedHashMap<Integer, RelicItemData> loadAll(Container inv, int from, int count) {
        LinkedHashMap<Integer, RelicItemData> map = new LinkedHashMap<>();
        for (int i = from; i < from + count && i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            try {
                if (!stack.isEmpty() && DungeonItemNbt.RELIC.has(stack)) {
                    map.put(i, DungeonItemNbt.RELIC.loadFrom(stack));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public static boolean isRelic(ItemStack stack) {
        return !stack.isEmpty() && DungeonItemNbt.RELIC.has(stack);
    }

    /**
     * Whether the stack may go into {@code targetSlot}: it has to be a relic, and adding it must not push
     * its type past {@link com.robertx22.library_of_exile.database.relic.relic_type.RelicType#max_equipped}
     * counting the other relics already in the range (the target slot itself is excluded, so replacing a
     * relic of the same type is always fine).
     */
    public static boolean canPlace(Container inv, int from, int count, int targetSlot, ItemStack stack) {
        if (!isRelic(stack)) {
            return false;
        }
        try {
            RelicItemData data = DungeonItemNbt.RELIC.loadFrom(stack);
            String type = data.getType().id;

            int existing = 0;
            for (var en : loadAll(inv, from, count).entrySet()) {
                if (en.getKey() == targetSlot) {
                    continue;
                }
                if (en.getValue().getType().id.equals(type)) {
                    existing++;
                }
            }
            return existing + 1 <= data.getType().max_equipped;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Consumes a use from every relic in the range that actually applies (within its type's cap, see
     * {@link RelicItemData#filterEquippable}) - removing the ones that run out - and returns the summed
     * stats. Relics past the cap are left untouched since their stats were never applied.
     */
    public static RelicStatsContainer consumeAndCalculate(Container inv, int from, int count) {
        var all = loadAll(inv, from, count);
        List<RelicItemData> valid = RelicItemData.filterEquippable(new ArrayList<>(all.values()));

        for (var en : all.entrySet()) {
            RelicItemData data = en.getValue();
            if (!valid.contains(data)) {
                continue;
            }
            int slot = en.getKey();
            if (data.consumeUse()) {
                inv.removeItem(slot, 1);
            } else {
                DungeonItemNbt.RELIC.saveTo(inv.getItem(slot), data);
            }
        }
        inv.setChanged();

        List<ExactRelicStat> ex = new ArrayList<>();
        for (RelicItemData data : valid) {
            ex.addAll(data.getExactStats());
        }
        return RelicStatsContainer.calculate(ex);
    }

    /**
     * Summed value per stat for a preview, using the same capped calculation the map start applies.
     */
    public static Map<RelicStat, Float> aggregate(List<RelicItemData> relics) {
        List<ExactRelicStat> exact = new ArrayList<>();
        for (RelicItemData data : relics) {
            exact.addAll(data.getExactStats());
        }
        RelicStatsContainer total = RelicStatsContainer.calculate(exact);

        Map<RelicStat, Float> map = new HashMap<>();
        for (ExactRelicStat ex : exact) {
            RelicStat stat = ex.getStat();
            map.putIfAbsent(stat, total.get(stat));
        }
        return map;
    }
}
