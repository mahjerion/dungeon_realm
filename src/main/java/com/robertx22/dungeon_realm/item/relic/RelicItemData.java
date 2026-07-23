package com.robertx22.dungeon_realm.item.relic;

import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.database.init.LibDatabase;
import com.robertx22.library_of_exile.database.relic.relic_rarity.RelicRarity;
import com.robertx22.library_of_exile.database.relic.relic_type.RelicType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelicItemData {

    public List<RelicAffixData> affixes = new ArrayList<>();


    public String rar = "common";

    public String type = DungeonMain.MODID;

    // uses remaining before the relic is consumed. Defaults to 1 so relics saved before this field
    // existed (no "uses" tag in their nbt) keep the old single-use behavior.
    public int uses = 1;

    public RelicRarity getRarity() {
        return LibDatabase.RelicRarities().get(rar);
    }

    public RelicType getType() {
        return LibDatabase.RelicTypes().get(type);
    }

    public int getMaxUses() {
        return getRarity().max_uses;
    }

    // returns true once the relic has no uses left and should be removed from the inventory
    public boolean consumeUse() {
        uses--;
        return uses <= 0;
    }

    // given relics in slot order, returns the ones that actually count toward each RelicType's
    // max_equipped cap - first slots win, later relics of an already-full type are dropped. Shared
    // by the actual consumption logic (MapDeviceBE) and the stat preview (MapDeviceMenu) so they
    // can't disagree about which relics apply.
    public static List<RelicItemData> filterEquippable(List<RelicItemData> orderedBySlot) {
        Map<String, Integer> counts = new HashMap<>();
        List<RelicItemData> valid = new ArrayList<>();
        for (RelicItemData data : orderedBySlot) {
            int cur = counts.getOrDefault(data.type, 0) + 1;
            counts.put(data.type, cur);
            if (cur <= data.getType().max_equipped) {
                valid.add(data);
            }
        }
        return valid;
    }

}
