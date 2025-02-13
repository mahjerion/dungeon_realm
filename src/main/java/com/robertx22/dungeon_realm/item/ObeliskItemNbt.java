package com.robertx22.dungeon_realm.item;

import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.utils.ItemstackDataSaver;

public class ObeliskItemNbt {

    public static ItemstackDataSaver<DungeonItemMapData> OBELISK_MAP = new ItemstackDataSaver<>(DungeonMain.MODID + "_obelisk", DungeonItemMapData.class, () -> new DungeonItemMapData());

}
