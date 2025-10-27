package com.robertx22.dungeon_realm.item;

import com.robertx22.dungeon_realm.api.DungeonExileEvents;
import com.robertx22.dungeon_realm.api.OnGenerateNewMapItemEvent;
import com.robertx22.dungeon_realm.database.DungeonDatabase;
import com.robertx22.dungeon_realm.main.DungeonEntries;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DungeonMapItem extends Item {
    public DungeonMapItem() {
        super(new Properties().stacksTo(1));
    }


    // todo probably want to add more features dont the line..
    // todo2, might want a better coded custom parameter class like loot tables have with lootcontext
    public static ItemStack newRandomMapItemStack(DungeonMapGenSettings p) {


        ItemStack stack = new ItemStack(DungeonEntries.DUNGEON_MAP_ITEM.get());

        var data = randomNewMapData();

        DungeonItemNbt.DUNGEON_MAP.saveTo(stack, data);

        var event = new OnGenerateNewMapItemEvent(stack);
        DungeonExileEvents.ON_GENERATE_NEW_MAP_ITEM.callEvents(event);

        return stack;

    }

    public static DungeonItemMapData randomNewMapData() {


        var data = new DungeonItemMapData();
        data.dungeon = GetRandomDungeonGUID();
        return data;

    }

    public static String GetRandomDungeonGUID() {
        return RandomUtils.weightedRandom(DungeonDatabase.Dungeons().getFilterWrapped(i -> true).list).id;
    }
}
