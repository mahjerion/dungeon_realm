package com.robertx22.dungeon_realm.item.relic;

import com.robertx22.dungeon_realm.item.DungeonItemNbt;
import com.robertx22.library_of_exile.database.init.LibDatabase;
import com.robertx22.library_of_exile.database.relic.relic_rarity.RelicRarity;
import com.robertx22.library_of_exile.database.relic.relic_type.RelicType;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class RelicGenerator {


    public static class Settings {

        public Optional<RelicRarity> rar = Optional.empty();
        public Optional<RelicType> type = Optional.empty();

    }

    public static ItemStack randomRelicItem(Optional<Player> p, Settings settings) {
        var data = randomRelic(p, settings);
        ItemStack stack = new ItemStack(data.getType().getItem());
        DungeonItemNbt.RELIC.saveTo(stack, data);
        return stack;
    }

    public static RelicItemData randomRelic(Optional<Player> p, Settings settings) {

        RelicItemData data = new RelicItemData();

        var rar = settings.rar.orElse(LibDatabase.RelicRarities().random());
        var type = settings.type.orElse(LibDatabase.RelicTypes().random());

        data.rar = rar.GUID();
        data.type = type.GUID();
        data.uses = rar.max_uses;

        for (int i = 0; i < rar.affixes; i++) {

            var pool = LibDatabase.RelicAffixes().getFilterWrapped(x -> {
                if (x.implicit) {
                    return false; // implicits have their own dedicated slot, rolled below
                }
                if (data.affixes.stream().anyMatch(e -> e.id.equals(x.GUID()))) {
                    return false; // no same affixes
                }
                if (!type.GUID().equals(x.relic_type)) {
                    return false;
                }
                return true;
            });

            // a type can have fewer distinct affixes than the rarity asks for (a datapack that raises a
            // rarity's affix count, or disables affixes of a league type). random() on an empty pool
            // returns null, which used to NPE here and kill the whole drop - the relic just ends up
            // with fewer affixes instead.
            if (pool.list.isEmpty()) {
                break;
            }
            var affix = pool.random();

            int perc = RandomUtils.RandomRange(rar.min_affix_percent, rar.max_affix_percent);
            data.affixes.add(new RelicAffixData(affix.GUID(), perc));
        }

        // exactly one implicit per relic, regardless of rarity, from the implicits bound to this type
        // (an empty relic_type still means "any type"). The league content guarantees are all bound to
        // the dungeon type since only dungeon relics enter the map device, so harvest and obelisk
        // relics roll no implicit.
        var implicitPool = LibDatabase.RelicAffixes().getFilterWrapped(x -> {
            if (!x.implicit) {
                return false;
            }
            return x.relic_type.isEmpty() || type.GUID().equals(x.relic_type);
        });
        if (!implicitPool.list.isEmpty()) { // no implicits registered at all - relic just has none
            var imp = implicitPool.random();
            int perc = RandomUtils.RandomRange(rar.min_affix_percent, rar.max_affix_percent);
            data.implicit = new RelicAffixData(imp.GUID(), perc);
        }

        return data;
    }
}
