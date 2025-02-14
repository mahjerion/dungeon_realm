package com.robertx22.dungeon_realm.configs;

import com.robertx22.dungeon_realm.main.DungeonMain;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DungeonConfig {

    public static final ForgeConfigSpec SPEC;
    public static final DungeonConfig CONFIG;

    static {
        final Pair<DungeonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(DungeonConfig::new);
        SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
    }


    public ForgeConfigSpec.IntValue MIN_MAP_ROOMS;
    public ForgeConfigSpec.IntValue MAX_MAP_ROOMS;

    public ForgeConfigSpec.DoubleValue DUNGEON_MAP_SPAWN_CHANCE_ON_CHEST_LOOT;
    public ForgeConfigSpec.ConfigValue<List<? extends String>> DIMENSION_CHANCE_MULTI;

    public ForgeConfigSpec.IntValue MOB_MIN;
    public ForgeConfigSpec.IntValue MOB_MAX;

    public ForgeConfigSpec.IntValue PACK_MOB_MIN;
    public ForgeConfigSpec.IntValue PACK_MOB_MAX;

    public ForgeConfigSpec.DoubleValue UBER_FRAG_DROPRATE;

    public static DungeonConfig get() {
        return CONFIG;
    }


    public HashMap<String, Float> dimChanceMap = new HashMap<>();

    public HashMap<String, Float> getDimChanceMap() {
        if (dimChanceMap.isEmpty()) {
            for (String s : DIMENSION_CHANCE_MULTI.get()) {
                String dim = s.split("-")[0];
                Float multi = Float.parseFloat(s.split("-")[1]);
                dimChanceMap.put(dim, multi);
            }
        }

        return dimChanceMap;
    }


    public float getDimChanceMulti(Level level) {
        String dimid = level.dimensionTypeId().location().toString();
        var map = getDimChanceMap();
        return map.getOrDefault(dimid, 1F);
    }

    DungeonConfig(ForgeConfigSpec.Builder b) {
        b.comment("Dungeon Realm Configs")
                .push("general");

        DIMENSION_CHANCE_MULTI = b
                .comment("Dungeon map spawn chance multi per dimension")
                .defineList("DIMENSION_CHANCE_MULTI", () -> Arrays.asList(DungeonMain.DIMENSION_ID + "-1.5"), x -> true);


        DUNGEON_MAP_SPAWN_CHANCE_ON_CHEST_LOOT = b
                .comment("Dungeon Maps have a chance to spawn when you loot chests outside of map dimensions")
                .defineInRange("DUNGEON_MAP_SPAWN_CHANCE_ON_CHEST_LOOT", 5F, 0, 100);

        MIN_MAP_ROOMS = b.defineInRange("MIN_MAP_ROOMS", 12, 1, 100);
        MAX_MAP_ROOMS = b.defineInRange("MAX_MAP_ROOMS", 20, 1, 100);

        MOB_MIN = b.defineInRange("mob_min", 1, 0, 20);
        MOB_MAX = b.defineInRange("mob_max", 2, 0, 20);

        PACK_MOB_MIN = b.defineInRange("pack_mob_min", 3, 0, 20);
        PACK_MOB_MAX = b.defineInRange("pack_mob_max", 6, 0, 20);

        UBER_FRAG_DROPRATE = b.comment("Uber fragments can drop from A map boss, default chance is 10%").defineInRange("UBER_FRAG_DROP_RATE", 10D, 0, 100);

        b.pop();
    }


}
