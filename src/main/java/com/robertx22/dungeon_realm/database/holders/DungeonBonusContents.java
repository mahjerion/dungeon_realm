package com.robertx22.dungeon_realm.database.holders;

import com.robertx22.dungeon_realm.main.DungeonEntries;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.database.extra_map_content.MapContent;
import com.robertx22.library_of_exile.registry.helpers.ExileKey;
import com.robertx22.library_of_exile.registry.helpers.ExileKeyHolder;
import com.robertx22.library_of_exile.registry.helpers.KeyInfo;
import com.robertx22.library_of_exile.registry.register_info.ModRequiredRegisterInfo;

public class DungeonBonusContents extends ExileKeyHolder<MapContent> {

    public static DungeonBonusContents INSTANCE = new DungeonBonusContents(DungeonMain.REGISTER_INFO);

    public DungeonBonusContents(ModRequiredRegisterInfo info) {
        super(info);
    }

    public ExileKey<MapContent, KeyInfo> UBER_BOSS = ExileKey.ofId(this, "uber_boss", x -> MapContent.of(x.GUID(), 0, DungeonEntries.UBER_TELEPORT.getKey().location().toString(), 1, 1));

    // regular boss arena access is now exclusively through the Map GUI's percent-gated teleport
    // (TeleportToBossPacket / DungeonConfig.MAP_PERCENT_COMPLETE_NEEDED_FOR_BOSS_ARENA) - the old
    // physical boss_teleport block used to be scattered into every map as always-spawn bonus content,
    // which let players stumble onto it and skip the exploration requirement entirely.

    @Override
    public void loadClass() {

    }
}
