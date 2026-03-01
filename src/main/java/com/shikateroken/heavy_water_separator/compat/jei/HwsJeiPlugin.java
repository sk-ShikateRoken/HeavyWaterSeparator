package com.shikateroken.heavy_water_separator.compat.jei;

import com.shikateroken.heavy_water_separator.recipe.FluidToFluidrecipe;
import com.shikateroken.heavy_water_separator.registry.HwsBlocks;
import com.shikateroken.heavy_water_separator.registry.HwsRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class HwsJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation("heavy_water_separator", "jei_plugin");
    }

    // 1. カテゴリの登録
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DTHWSRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    // 2. レシピの登録
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        // HwsRecipes.FLUID_TO_FLUID_TYPE.get() はあなたのレシピタイプです
        List<FluidToFluidrecipe> recipes = recipeManager.getAllRecipesFor(HwsRecipes.FLUID_TO_FLUID_TYPE.get());

        registration.addRecipes(DTHWSRecipeCategory.RECIPE_TYPE, recipes);
    }

    // 3. 触媒（Catalyst）の登録
    // 「この機械（ブロック）をクリックすると、このレシピカテゴリが開くよ」という設定
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(HwsBlocks.DTHWS.get()), DTHWSRecipeCategory.RECIPE_TYPE);
    }
}
