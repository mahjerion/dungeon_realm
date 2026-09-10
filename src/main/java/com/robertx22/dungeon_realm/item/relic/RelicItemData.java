package com.robertx22.dungeon_realm.item.relic;

import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.database.init.LibDatabase;
import com.robertx22.library_of_exile.database.relic.relic_rarity.RelicRarity;
import com.robertx22.library_of_exile.database.relic.relic_type.RelicType;
import com.robertx22.library_of_exile.database.relic.stat.ExactRelicStat;
import com.robertx22.library_of_exile.database.relic.stat.RelicMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelicItemData {

    public List<RelicAffixData> affixes = new ArrayList<>();

    // the relic's single implicit affix, rolled from its own pool and not counted against the
    // rarity's affix count. Null on relics saved before this field existed (no "implicit" tag in
    // their nbt) - those simply have no implicit, same as how "uses" defaulted for old relics.
    public RelicAffixData implicit = null;

    public String rar = "common";

    // the main mod's retired relic type, still in the NBT of old relics - see DungeonItemNbt.RELIC
    public static final String LEGACY_MNS_TYPE = "mmorpg";

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

    // implicit first, then the regular affixes - the display order everywhere
    public List<RelicAffixData> getAllAffixes() {
        List<RelicAffixData> all = new ArrayList<>();
        if (implicit != null) {
            all.add(implicit);
        }
        all.addAll(affixes);
        return all;
    }

    // single source of truth for what this relic contributes to a map. The map device (what actually
    // applies), its stat preview screen and the item tooltip each used to walk affixes -> mods ->
    // toExact themselves, which is how the implicit could silently end up in one but not the others.
    public List<ExactRelicStat> getExactStats() {
        List<ExactRelicStat> ex = new ArrayList<>();
        for (RelicAffixData affix : getAllAffixes()) {
            for (RelicMod mod : affix.get().mods) {
                ex.add(mod.toExact(affix.p));
            }
        }
        return ex;
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
