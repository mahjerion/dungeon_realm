package com.robertx22.dungeon_realm.structure;

import com.robertx22.dungeon_realm.item.DungeonItemMapData;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.database.init.LibDatabase;
import com.robertx22.library_of_exile.database.map_finish_rarity.MapFinishRarity;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.stream.Collectors;

public class DungeonMapData {
    // todo

    public MapBonusContentsData bonusContents = new MapBonusContentsData();
    public HashMap<String, Long> spawnPositions = new HashMap<>();

    public DungeonItemMapData item = new DungeonItemMapData();

    public int x = 0;
    public int z = 0;

    public String finish_rar = "common";

    public MapRoomsData rooms = new MapRoomsData();

    public MapFinishRarity getFinishRarity() {
        return LibDatabase.MapFinishRarity().get(finish_rar);
    }

    public void spawnBonusMapContent(Level level, BlockPos pos) {
        // bonus content should only spawn inside the main dungeon structure
        if (DungeonMain.MAIN_DUNGEON_STRUCTURE.isInside((ServerLevel) level, pos)) {
            var list = bonusContents.map.entrySet().stream().filter(x -> x.getValue().remainingSpawns > 0).collect(Collectors.toList());

            if (list.size() > 0) {
                var en = RandomUtils.randomFromList(list);
                var mc = LibDatabase.MapContent().get(en.getKey());

                var block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(mc.block_id));
                level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
                bonusContents.map.get(en.getKey()).remainingSpawns--;
            }
        }
    }
}
