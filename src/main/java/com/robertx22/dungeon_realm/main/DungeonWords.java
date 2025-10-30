package com.robertx22.dungeon_realm.main;

import com.robertx22.library_of_exile.localization.ExileTranslation;
import com.robertx22.library_of_exile.localization.ITranslated;
import com.robertx22.library_of_exile.localization.TranslationBuilder;
import com.robertx22.library_of_exile.localization.TranslationType;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public enum DungeonWords implements ITranslated {
    USABLE_ONLY_IN_DUNGEON_REALM("This is only usable in the Dungeon Realm"),
    NEED_HOME_PEARL("You need to have at least one %1$s in your inventory to enter."),
    HOME_PEARL_DESC("Use to Exit the Dungeon Realm"),
    MAP_HAS_UBER_ARENA("- { Area Contains an Uber Boss Portal } -"),
    MAP_COMPLETE_RARITY_UPGRADE("Your Map Exploration is now %1$s"),
    MAP_ITEM_DESC("This item allows you to enter the Dungeon Realm"),
    MAP_ITEM_USE_INFO("Right Click the [Map Device Block] with the map to start it."),
    RELIC_CONTAINER("Map Relics Inventory"),
    RELIC_ITEM_INFO("Relics are placed inside the Map Device"),
    RELIC_ITEM_INFO2("A Relic Key Item opens the Map Device"),
    RELIC_MAX_COUNT("Maximum [%1$s] of this type can be used"),
    CREATIVE_TAB("Dungeon Realm"),
    SHOW_RELIC_STATS_HINT("Press '%1$s' to display all Relic Stats"),
    MAP_LAYOUT("Map layout: %1$s"),
    MAP_NAME_BASTION("Bastion"),
    MAP_NAME_BRICK("Brick"),
    MAP_NAME_CEMENT("Cement"),
    MAP_NAME_END("The End"),
    MAP_NAME_IT("Ice Temple"),
    MAP_NAME_MINE("Mines"),
    MAP_NAME_MISC("Miscellaneous"),
    MAP_NAME_MOSSY_BRICK("Mossy Brick"),
    MAP_NAME_NATURE("Nature"),
    MAP_NAME_NETHER("Nether"),
    MAP_NAME_NIGHT_TERROR("Night Terror"),
    MAP_NAME_PYRAMID("Pyramid"),
    MAP_NAME_SANDSTONE("Sandstone"),
    MAP_NAME_SEWER2("Sewers II"),
    MAP_NAME_SEWERS("Sewers"),
    MAP_NAME_SPIDER_NEST("Spider's Nest"),
    MAP_NAME_SPRUCE_MANSION("Spruce Mansion"),
    MAP_NAME_STEAMPUNK("Steampunk"),
    MAP_NAME_STONE_BRICK("Stone Brick"),
    MAP_NAME_TENT("Tents"),
    MAP_NAME_TEST("Test"),
    MAP_NAME_WARPED("Warped"),
    MAP_NAME_WN("Whispering Night"),
    MAP_NAME_UNDEFINED("<undefined>"),
    ;

    public String name;

    DungeonWords(String name) {
        this.name = name;
    }

    @Override
    public TranslationBuilder createTranslationBuilder() {
        return new TranslationBuilder(DungeonMain.MODID).name(ExileTranslation.of(DungeonMain.MODID + ".words." + this.name().toLowerCase(Locale.ROOT), name));
    }

    public MutableComponent get(Object... obj) {
        return getTranslation(TranslationType.NAME).getTranslatedName(obj);
    }

    @Override
    public String GUID() {
        return name();
    }

    public static String MapGUID(String name) {
        if (name == null) {
            name = "undefined";
        }
        return DungeonMain.MODID + ".words.map_name_" + name.toLowerCase(Locale.ROOT);
    }
}
