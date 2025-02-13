package com.robertx22.dungeon_realm.item;

import com.robertx22.dungeon_realm.main.DungeonEntries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ObeliskMapItem extends Item {
    public ObeliskMapItem() {
        super(new Properties().stacksTo(1));
    }


    public static ItemStack blankMap() {

        ItemStack stack = new ItemStack(DungeonEntries.DUNGEON_MAP_ITEM.get());

        var data = new DungeonItemMapData();

        ObeliskItemNbt.OBELISK_MAP.saveTo(stack, data);

        return stack;

    }
}
