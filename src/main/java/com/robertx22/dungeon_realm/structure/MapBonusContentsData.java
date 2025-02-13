package com.robertx22.dungeon_realm.structure;


import com.robertx22.dungeon_realm.database.holders.DungeonBonusContents;
import com.robertx22.dungeon_realm.item.ObeliskItemNbt;
import com.robertx22.library_of_exile.database.extra_map_content.MapContent;
import com.robertx22.library_of_exile.database.init.LibDatabase;
import com.robertx22.library_of_exile.dimension.structure.MapStructure;
import com.robertx22.library_of_exile.util.PointData;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MapBonusContentsData {


    HashMap<String, BonusContentData> map = new HashMap<>();

    public int totalGenDungeonChunks = 0;
    public int processedChunks = 0;

    // chunks mechanics were tried to spawn or succeded in (limit 1 per chunk, both attempts and successes)
    public Set<PointData> mechsChunks = new HashSet<>();

    // calc chance in such a way that if a mechanic should spawn 5 times, and the dungeon has 20 chunks left, it scatters the spawn chunks but never accidentally not generates
    public float calcSpawnChance(BlockPos pos) {

        var cp = new ChunkPos(pos);
        var point = new PointData(cp.x, cp.z);

        if (mechsChunks.contains(point)) {
            return 0;
        }

        int remaining = getTotalSpawnsRemainingFromAllLeagues();
        int chunksLeft = totalGenDungeonChunks - processedChunks;


        if (chunksLeft < remaining) {
            return 100;
        }


        float chance = remaining / (float) chunksLeft * 100F;

        return chance;

    }


    public int getTotalSpawnsRemainingFromAllLeagues() {
        return (int) map.values().stream().mapToInt(x -> x.remainingSpawns).sum();
    }


    // todo eventually player stats will decide mechanic spawn chances etc

    void addContent(MapContent c) {
        var data = new BonusContentData();
        this.map.put(c.GUID(), data);
    }

    public void setupOnMapStart(ItemStack stack, Player p) {

        var map = ObeliskItemNbt.OBELISK_MAP.loadFrom(stack);

        for (MapContent c : LibDatabase.MapContent().getFiltered(x -> x.always_spawn)) {
            addContent(c);
        }

        for (MapContent c : LibDatabase.MapContent().getFiltered(x -> !x.always_spawn)) {
            addContent(c);
        }
        int bonus = map.bonus_contents;

        var possible = LibDatabase.MapContent().getFiltered(x -> !x.always_spawn);

        if (bonus > possible.size()) {
            bonus = possible.size();
        }

        for (int i = 0; i < bonus; i++) {
            var c = RandomUtils.weightedRandom(possible);
            addContent(c);
            possible.removeIf(x -> x.GUID().equals(c.GUID()));
        }

        if (map.uber) {
            addContent(DungeonBonusContents.INSTANCE.UBER_BOSS.get());
        }
    }

    public BonusContentData get(MapStructure m) {
        return map.getOrDefault(m.guid(), BonusContentData.EMPTY);
    }

}
