package com.robertx22.dungeon_realm.item;

import com.robertx22.dungeon_realm.api.DungeonExileEvents;
import com.robertx22.dungeon_realm.api.GetUnlockedAtlasNodesEvent;
import com.robertx22.dungeon_realm.api.OnGenerateNewMapItemEvent;
import com.robertx22.dungeon_realm.database.DungeonDatabase;
import com.robertx22.dungeon_realm.main.DungeonEntries;
import com.robertx22.library_of_exile.database.atlas.AtlasNodeUtils;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public class DungeonMapItem extends Item {
    public DungeonMapItem() {
        super(new Properties().stacksTo(1));
    }


    // todo probably want to add more features dont the line..
    // todo2, might want a better coded custom parameter class like loot tables have with lootcontext
    public static ItemStack newRandomMapItemStack(DungeonMapGenSettings p, @Nullable Player player) {


        ItemStack stack = new ItemStack(DungeonEntries.DUNGEON_MAP_ITEM.get());

        var data = randomNewMapData(player);

        DungeonItemNbt.DUNGEON_MAP.saveTo(stack, data);

        var event = new OnGenerateNewMapItemEvent(stack);
        DungeonExileEvents.ON_GENERATE_NEW_MAP_ITEM.callEvents(event);

        return stack;

    }

    public static DungeonItemMapData randomNewMapData(@Nullable Player player) {
        var data = new DungeonItemMapData();
        data.dungeon = GetRandomDungeonGUID(player);
        return data;
    }

    // dungeons with no Atlas node stay ungated (backward compat). A null player also
    // isn't gated, since there's nobody to check unlock progress for.
    public static String GetRandomDungeonGUID(@Nullable Player player) {
        var unlockedNodeIds = getUnlockedAtlasNodeIds(player);
        var candidates = DungeonDatabase.Dungeons()
                .getFilterWrapped(i -> player == null || AtlasNodeUtils.isDungeonUnlocked(unlockedNodeIds, i.id)).list;
        return RandomUtils.weightedRandom(candidates).id;
    }

    private static Set<String> getUnlockedAtlasNodeIds(@Nullable Player player) {
        if (player == null) {
            return Collections.emptySet();
        }
        return DungeonExileEvents.GET_UNLOCKED_ATLAS_NODES.callEvents(new GetUnlockedAtlasNodesEvent(player)).unlockedNodeIds;
    }

    public static ItemStack newFixedMapItemStack(DungeonMapGenSettings p, String dungeonGuid, @Nullable Player player) {
        ItemStack stack = new ItemStack(DungeonEntries.DUNGEON_MAP_ITEM.get());
        var data = fixedNewMapData(dungeonGuid, player);
        DungeonItemNbt.DUNGEON_MAP.saveTo(stack, data);
        var event = new OnGenerateNewMapItemEvent(stack);
        DungeonExileEvents.ON_GENERATE_NEW_MAP_ITEM.callEvents(event);
        return stack;
    }

    // if the requested target isn't unlocked for this player, fall back to a random unlocked
    // dungeon instead of handing out a map for content they haven't reached on the Atlas yet.
    public static DungeonItemMapData fixedNewMapData(String dungeonGuid, @Nullable Player player) {
        var data = new DungeonItemMapData();
        if (player != null && !AtlasNodeUtils.isDungeonUnlocked(getUnlockedAtlasNodeIds(player), dungeonGuid)) {
            data.dungeon = GetRandomDungeonGUID(player);
        } else {
            data.dungeon = dungeonGuid;
        }
        return data;
    }
}
