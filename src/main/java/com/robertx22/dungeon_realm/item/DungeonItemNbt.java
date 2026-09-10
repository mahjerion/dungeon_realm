package com.robertx22.dungeon_realm.item;

import com.robertx22.dungeon_realm.item.relic.RelicItemData;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.utils.ItemstackDataSaver;
import net.minecraft.world.item.ItemStack;

public class DungeonItemNbt {

    public static ItemstackDataSaver<DungeonItemMapData> DUNGEON_MAP = new ItemstackDataSaver<>(DungeonMain.MODID + "_dungeon_map", DungeonItemMapData.class, () -> new DungeonItemMapData());

    /**
     * The main mod used to register its own relic type ("mmorpg") that was folded into this one. A relic
     * rolls its type into NBT, so items from before that keep the dead id forever unless it's fixed here,
     * the one place every reader (tooltip, device, preview) loads relic data through. The tag itself is
     * rewritten on the next save (a consumed use); until then the in-memory fixup is enough.
     */
    public static ItemstackDataSaver<RelicItemData> RELIC = new ItemstackDataSaver<>(DungeonMain.MODID + "_relic", RelicItemData.class, () -> new RelicItemData()) {
        @Override
        public RelicItemData loadFrom(ItemStack stack) {
            RelicItemData data = super.loadFrom(stack);
            if (data != null && RelicItemData.LEGACY_MNS_TYPE.equals(data.type)) {
                data.type = DungeonMain.MODID;
            }
            return data;
        }
    };

}
