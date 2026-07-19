package com.robertx22.dungeon_realm.item.recipe;

import com.robertx22.dungeon_realm.item.DungeonItemMapData;
import com.robertx22.dungeon_realm.main.DungeonEntries;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class FixedDungeonMapRecipe implements CraftingRecipe {

    private final ShapedRecipe inner;
    private final String targetDungeon;

    public FixedDungeonMapRecipe(ShapedRecipe inner, String targetDungeon) {
        this.inner = inner;
        this.targetDungeon = targetDungeon;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return inner.matches(container, level);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack stack = new ItemStack(DungeonEntries.FIXED_DUNGEON_MAP_ITEM.get());
        DungeonItemMapData.withTargetDungeon(stack, targetDungeon);
        return stack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return inner.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        ItemStack stack = new ItemStack(DungeonEntries.FIXED_DUNGEON_MAP_ITEM.get());
        DungeonItemMapData.withTargetDungeon(stack, targetDungeon);
        return stack;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inner.getIngredients();
    }

    @Override
    public ResourceLocation getId() {
        return inner.getId();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return DungeonEntries.FIXED_DUNGEON_MAP.get();
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    public String getTargetDungeon() {
        return targetDungeon;
    }

    public ShapedRecipe getInner() {
        return inner;
    }
}