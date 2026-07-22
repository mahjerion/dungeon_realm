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

    public ForgeConfigSpec.IntValue ELITE_MOB_COMPLETION_WEIGHT;
    public ForgeConfigSpec.IntValue MINI_BOSS_COMPLETION_WEIGHT;

    public ForgeConfigSpec.IntValue KILL_COMPLETION_DATA_BLOCK_LEEWAY;

    public ForgeConfigSpec.IntValue MAP_PERCENT_COMPLETE_NEEDED_FOR_BOSS_ARENA;

    public ForgeConfigSpec.IntValue MIN_LEVEL_FOR_PERFECTED_SEED;

    public ForgeConfigSpec.IntValue IMPRISONED_MONSTER_CURRENCY_REWARD;
    public ForgeConfigSpec.DoubleValue IMPRISONED_MONSTER_SEED_DROP_CHANCE;
    public ForgeConfigSpec.DoubleValue IMPRISONED_MONSTER_PERFECTED_SEED_CHANCE;

    public ForgeConfigSpec.IntValue STRONGBOX_GUARDIAN_COUNT;
    public ForgeConfigSpec.IntValue STRONGBOX_LOOT_ROLLS;

    public ForgeConfigSpec.DoubleValue SHRINE_BUFF_RADIUS;

    public ForgeConfigSpec.IntValue STRONGBOX_CATEGORY_ITEM_MIN;
    public ForgeConfigSpec.IntValue STRONGBOX_CATEGORY_ITEM_MAX;

    public ForgeConfigSpec.IntValue STRONGBOX_CATEGORY_WEIGHT_CURRENCY;
    public ForgeConfigSpec.IntValue STRONGBOX_CATEGORY_WEIGHT_WEAPON;
    public ForgeConfigSpec.IntValue STRONGBOX_CATEGORY_WEIGHT_ARMOR;
    public ForgeConfigSpec.IntValue STRONGBOX_CATEGORY_WEIGHT_JEWELRY;
    public ForgeConfigSpec.IntValue STRONGBOX_CATEGORY_WEIGHT_JEWEL;
    public ForgeConfigSpec.IntValue STRONGBOX_CATEGORY_WEIGHT_OMEN;
    public ForgeConfigSpec.IntValue STRONGBOX_CATEGORY_WEIGHT_SUPPORT_GEM;
    public ForgeConfigSpec.IntValue STRONGBOX_CATEGORY_WEIGHT_AURA_GEM;
    public ForgeConfigSpec.IntValue STRONGBOX_CATEGORY_WEIGHT_RUNE;
    public ForgeConfigSpec.IntValue STRONGBOX_CATEGORY_WEIGHT_SOCKETABLE_GEM;
    public ForgeConfigSpec.IntValue STRONGBOX_CATEGORY_WEIGHT_UNIQUE;

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

        ELITE_MOB_COMPLETION_WEIGHT = b.comment("How many mobs should an elite mob count for in terms of map completion? Default is 10, so 10x a normal mob death. Max 9999")
                                       .defineInRange("elite_mob_completion_weight", 10, 1, 9999);

        MINI_BOSS_COMPLETION_WEIGHT = b.comment("How many mobs should a miniboss mob count for in terms of map completion? Default is 20, so 20x a normal mob death. Max 9999")
                                       .defineInRange("mini_boss_completion_weight", 20, 1, 9999);

        KILL_COMPLETION_DATA_BLOCK_LEEWAY = b.comment("Number of data blocks allowed to be potentially miscounted to transition to accurate kill counts. Defaults to 2, between 0-20.")
                .comment("For example, if you have 20 data blocks, and the player has come into range of 18, with a leeway of 2, this means 18 + 2 >= 20, so assume all mobs have been spawned.")
                .comment("This matters *mostly* for the kill completion assumptions. Until all mobs are spawned, it assumes remaining data blocks (command blocks or structure blocks)")
                .comment("will spawn either the MOB_MAX or PACK_MOB_MAX number of mobs. When you cross the threshold + leeway, it assumes all mobs have been spawned, so the")
                .comment("denominator for the calculation shrinks, and kill % goes up. 2 seems to be roughly the right number from testing, but if this proves to be insanely accurate")
                .comment("feel free to crank this back down to 1 or 0.")
                        .defineInRange("kill_completion_data_block_leeway", 2, 0, 20);

        MAP_PERCENT_COMPLETE_NEEDED_FOR_BOSS_ARENA = b.comment("Kill completion percent needed before players can teleport straight to the map boss from the Map gui, without needing to find the boss teleport block.")
                .defineInRange("map_percent_complete_needed_for_boss_arena", 30, 5, 99);

        MIN_LEVEL_FOR_PERFECTED_SEED = b.comment("Minimum loot level required for the Imprisoned Monster's bonus 'Seed' drop to be able to roll the rarer Perfected tier. Below this level, only the base Seed can drop.")
                .defineInRange("min_level_for_perfected_seed", 80, 0, 1000);

        IMPRISONED_MONSTER_CURRENCY_REWARD = b.comment("Guaranteed number of currency items dropped when the Imprisoned Monster is slain.")
                .defineInRange("imprisoned_monster_currency_reward", 3, 0, 20);

        IMPRISONED_MONSTER_SEED_DROP_CHANCE = b.comment("Chance for the Imprisoned Monster to additionally drop a bonus 'Seed' potential-restore currency, default 25%")
                .defineInRange("imprisoned_monster_seed_drop_chance", 25D, 0, 100);

        IMPRISONED_MONSTER_PERFECTED_SEED_CHANCE = b.comment("Of an Imprisoned Monster Seed drop (see imprisoned_monster_seed_drop_chance), the chance it's the rarer Perfected tier instead of the base tier, default 10%")
                .defineInRange("imprisoned_monster_perfected_seed_chance", 10D, 0, 100);

        STRONGBOX_GUARDIAN_COUNT = b.comment("Number of guardian mobs released when a Strongbox is opened.")
                .defineInRange("strongbox_guardian_count", 8, 0, 50);

        STRONGBOX_LOOT_ROLLS = b.comment("Number of independent chest-loot rolls a Strongbox pays out once its guardians are dead.")
                .defineInRange("strongbox_loot_rolls", 3, 0, 20);

        SHRINE_BUFF_RADIUS = b.comment("Radius (blocks) around a Shrine that players must be within to receive its buff.")
                .defineInRange("shrine_buff_radius", 12D, 1, 128);

        STRONGBOX_CATEGORY_ITEM_MIN = b.comment("Minimum number of guaranteed-category items (see strongbox_category_weights) a Strongbox pays out on top of its normal chest loot rolls. Each one independently rolls its own category.")
                .defineInRange("strongbox_category_item_min", 1, 0, 100);

        STRONGBOX_CATEGORY_ITEM_MAX = b.comment("Maximum number of guaranteed-category items a Strongbox pays out (inclusive). Must be >= strongbox_category_item_min.")
                .defineInRange("strongbox_category_item_max", 6, 0, 100);

        b.comment("Relative weights for each guaranteed-category item roll a Strongbox pays out on top of its normal chest loot rolls. Higher = more common.")
                .push("strongbox_category_weights");

        STRONGBOX_CATEGORY_WEIGHT_CURRENCY = b.defineInRange("currency", 500, 0, 100000);
        STRONGBOX_CATEGORY_WEIGHT_WEAPON = b.defineInRange("weapon", 1000, 0, 100000);
        STRONGBOX_CATEGORY_WEIGHT_ARMOR = b.defineInRange("armor", 1000, 0, 100000);
        STRONGBOX_CATEGORY_WEIGHT_JEWELRY = b.defineInRange("jewelry", 1000, 0, 100000);
        STRONGBOX_CATEGORY_WEIGHT_JEWEL = b.defineInRange("jewel", 250, 0, 100000);
        STRONGBOX_CATEGORY_WEIGHT_OMEN = b.defineInRange("omen", 250, 0, 100000);
        STRONGBOX_CATEGORY_WEIGHT_SUPPORT_GEM = b.defineInRange("support_gem", 500, 0, 100000);
        STRONGBOX_CATEGORY_WEIGHT_AURA_GEM = b.defineInRange("aura_gem", 500, 0, 100000);
        STRONGBOX_CATEGORY_WEIGHT_RUNE = b.defineInRange("rune", 250, 0, 100000);
        STRONGBOX_CATEGORY_WEIGHT_SOCKETABLE_GEM = b.defineInRange("socketable_gem", 250, 0, 100000);
        STRONGBOX_CATEGORY_WEIGHT_UNIQUE = b.defineInRange("unique", 50, 0, 100000);

        b.pop();

        b.pop();
    }


}
