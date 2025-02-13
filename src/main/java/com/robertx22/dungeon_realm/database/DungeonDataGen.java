package com.robertx22.dungeon_realm.database;

import com.robertx22.dungeon_realm.main.DungeonEntries;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.dungeon_realm.main.DungeonWords;
import com.robertx22.library_of_exile.localization.ExileLangFile;
import com.robertx22.library_of_exile.localization.ExileTranslation;
import com.robertx22.library_of_exile.localization.ITranslated;
import com.robertx22.library_of_exile.localization.TranslationBuilder;
import com.robertx22.library_of_exile.registry.ExileRegistryType;
import net.minecraft.ChatFormatting;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DungeonDataGen implements DataProvider {

    public DungeonDataGen() {

    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        List<ITranslated> tra = new ArrayList<>();
        tra.addAll(Arrays.stream(DungeonWords.values()).toList());
        for (ITranslated t : tra) {
            t.createTranslationBuilder().build();
        }
        TranslationBuilder.of(DungeonMain.MODID).name(ExileTranslation.item(DungeonEntries.DUNGEON_MAP_ITEM.get(), ChatFormatting.DARK_PURPLE + "Dungeon Map")).build();
        TranslationBuilder.of(DungeonMain.MODID).name(ExileTranslation.item(DungeonEntries.MAP_DEVICE_ITEM.get(), "Map Device")).build();
        TranslationBuilder.of(DungeonMain.MODID).name(ExileTranslation.item(DungeonEntries.UBER_FRAGMENT.get(), "Uber Fragment")).build();

        TranslationBuilder.of(DungeonMain.MODID).name(ExileTranslation.block(DungeonEntries.MAP_DEVICE_BLOCK.get(), "Map Device")).build();

        ExileLangFile.createFile(DungeonMain.MODID, "");

        // todo  new RecipeGenerator().generateAll(pOutput, Obe.MODID);

        for (ExileRegistryType type : ExileRegistryType.getAllInRegisterOrder()) {
            type.getDatapackGenerator().run(pOutput);
        }

        // RecipeGenerator.generateAll(pOutput, DungeonMain.MODID);

        //DataProvider.saveStable(pOutput, x.serializeRecipe(), target);

        return CompletableFuture.completedFuture(null); // todo this is bad, but would it work?
        // i think this is only needed if you dont directly save the jsons yourself?
    }


    @Override
    public String getName() {
        return "obelisk_data";
    }
}
