package com.shikateroken.heavy_water_separator.compat.jei;

import com.shikateroken.heavy_water_separator.recipe.FluidToFluidrecipe;
import com.shikateroken.heavy_water_separator.registry.HwsBlocks;
import mekanism.api.recipes.FluidToFluidRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DTHWSRecipeCategory implements IRecipeCategory<FluidToFluidrecipe> {
    // JEI用のレシピID（一意であれば何でもOK）
    public static final RecipeType<FluidToFluidrecipe> RECIPE_TYPE =
            RecipeType.create("heavy_water_separator", "separating", FluidToFluidrecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public DTHWSRecipeCategory(IGuiHelper helper) {
        // GUI画像のパス
        ResourceLocation TEXTURE = new ResourceLocation("heavy_water_separator", "textures/gui/dthws_gui.png");

        // 背景画像の設定: texture, u, v, width, height
        // GUIのタンクと矢印が含まれる範囲を切り抜きます
        // 例: x=28, y=18 の位置から、幅120px, 高さ54px を切り抜く (座標はGUI画像に合わせて調整してください)
        this.background = helper.createDrawable(TEXTURE, 28, 18, 120, 54);

        // タブに表示されるアイコン（機械ブロックそのもの）
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(HwsBlocks.DTHWS.get()));
    }

    @Override
    public RecipeType<FluidToFluidrecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.heavy_water_separator.dthws");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FluidToFluidrecipe recipe, IFocusGroup focuses) {
        // 1. 入力タンクの設定 (GUIの座標に合わせて配置)
        // ※この座標は「backgroundで切り抜いた範囲」の中での相対座標です
        builder.addSlot(RecipeIngredientRole.INPUT, 3, 2) // x, y (微調整してください)
                .addIngredient(ForgeTypes.FLUID_STACK, recipe.getInput())
                .setFluidRenderer(8000, false, 16, 50); // 容量, 下から上へ, 幅, 高さ

        // 2. 出力タンクの設定
        builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 2) // x, y (微調整してください)
                .addIngredient(ForgeTypes.FLUID_STACK, recipe.getOutput())
                .setFluidRenderer(8000, false, 16, 50);
    }
}
