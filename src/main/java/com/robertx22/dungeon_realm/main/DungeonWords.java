package com.robertx22.dungeon_realm.main;

import com.robertx22.library_of_exile.localization.ExileTranslation;
import com.robertx22.library_of_exile.localization.ITranslated;
import com.robertx22.library_of_exile.localization.TranslationBuilder;
import com.robertx22.library_of_exile.localization.TranslationType;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public enum DungeonWords implements ITranslated {
    MAP_ITEM_USE_INFO("Right Click the [Map Device Block] with the map to start it."),
    CREATIVE_TAB("Dungeon Realm");

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
}
