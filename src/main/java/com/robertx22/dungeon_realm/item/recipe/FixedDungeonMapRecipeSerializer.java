package com.robertx22.dungeon_realm.item.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class FixedDungeonMapRecipeSerializer implements RecipeSerializer<FixedDungeonMapRecipe> {

    @Override
    public FixedDungeonMapRecipe fromJson(ResourceLocation id, JsonObject json) {
        String targetDungeon = GsonHelper.getAsString(json, "target_dungeon");
        // reuse vanilla's own shaped-recipe parsing for pattern/key/result entirely
        ShapedRecipe inner = RecipeSerializer.SHAPED_RECIPE.fromJson(id, json);
        return new FixedDungeonMapRecipe(inner, targetDungeon);
    }

    @Override
    public FixedDungeonMapRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        ShapedRecipe inner = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf);
        String targetDungeon = buf.readUtf();
        return new FixedDungeonMapRecipe(inner, targetDungeon);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, FixedDungeonMapRecipe recipe) {
        RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe.getInner());
        buf.writeUtf(recipe.getTargetDungeon());
    }
}