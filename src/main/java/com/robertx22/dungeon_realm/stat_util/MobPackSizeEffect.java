package com.robertx22.dungeon_realm.stat_util;

import com.robertx22.dungeon_realm.database.holders.DungeonRelicStats;
import com.robertx22.library_of_exile.database.relic.stat.RelicStatsContainer;
import com.robertx22.library_of_exile.utils.RandomUtils;

public class MobPackSizeEffect {
    public static int tryIncreasePackSize(int mobs, RelicStatsContainer c) {
        return tryIncreasePackSize(mobs, c, 0);
    }

    // extraPercent stacks additively with the relic-based pack size roll (e.g. from the
    // Atlas passive tree's pack_size stat)
    public static int tryIncreasePackSize(int mobs, RelicStatsContainer c, float extraPercent) {
        float size = c.get(DungeonRelicStats.INSTANCE.PACK_SIZE) + extraPercent;

        float pack = ((float) mobs * (1 + size / 100F));

        float remaining = (pack - ((int) pack)) * 100F;

        if (RandomUtils.roll(remaining)) {
            pack++;
        }

        return (int) pack;
    }

    public static int tryIncreasePackSize(int mobs, int packSizeStat) {

        float pack = ((float) mobs * (1 + packSizeStat / 100F));

        float remaining = (pack - ((int) pack)) * 100F;

        if (RandomUtils.roll(remaining)) {
            pack++;
        }

        return (int) pack;
    }
}
