package com.shikateroken.heavy_water_separator.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class FluidToFluidrecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final FluidStack input;
    private final FluidStack output;

    public FluidToFluidrecipe(ResourceLocation id, FluidStack input, FluidStack output) {
        this.id = id;
        this.input = input;
        this.output = output;
    }

    // タンクの中身がレシピの入力条件（液体の種類と量）を満たしているかチェックする独自メソッド
    public boolean matches(FluidStack tankFluid) {
        return tankFluid.getFluid() == input.getFluid() && tankFluid.getAmount() >= input.getAmount();
    }

    public FluidStack getInput() {
        return input;
    }

    public FluidStack getOutput() {
        return output;
    }

    // ------------------------------------------------------------------
    // 以下のメソッドはバニラのRecipeシステムに合わせるための必須実装（今回はアイテムを使わないので空の処理が多いです）
    // ------------------------------------------------------------------
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) { return false; }
    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) { return ItemStack.EMPTY; }
    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) { return true; }
    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) { return ItemStack.EMPTY; }
    @Override
    public ResourceLocation getId() { return id; }
    @Override
    public RecipeSerializer<?> getSerializer() { return Serializer.INSTANCE; }
    @Override
    public RecipeType<?> getType() { return Type.INSTANCE; }

    // ==================================================================
    // レシピの種類 (Recipe Type) の定義
    // ==================================================================
    public static class Type implements RecipeType<FluidToFluidrecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "fluid_to_fluid";
    }

    // ==================================================================
    // JSONファイルやネットワーク通信からレシピを読み書きするシリアライザ
    // ==================================================================
    public static class Serializer implements RecipeSerializer<FluidToFluidrecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public FluidToFluidrecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            // JSONの "ingredient" ブロックから入力液体を読み込む
            JsonObject ingredientJson = GsonHelper.getAsJsonObject(pSerializedRecipe, "ingredient");
            FluidStack input = deserializeFluid(ingredientJson);

            // JSONの "result" ブロックから出力液体を読み込む
            JsonObject resultJson = GsonHelper.getAsJsonObject(pSerializedRecipe, "result");
            FluidStack output = deserializeFluid(resultJson);

            return new FluidToFluidrecipe(pRecipeId, input, output);
        }

        @Override
        public @Nullable FluidToFluidrecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            FluidStack input = pBuffer.readFluidStack();
            FluidStack output = pBuffer.readFluidStack();
            return new FluidToFluidrecipe(pRecipeId, input, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, FluidToFluidrecipe pRecipe) {
            pBuffer.writeFluidStack(pRecipe.getInput());
            pBuffer.writeFluidStack(pRecipe.getOutput());
        }

        // JSONの {"fluid": "minecraft:water", "amount": 100} という形式を FluidStack に変換する独自メソッド
        private FluidStack deserializeFluid(JsonObject json) {
            String fluidName = GsonHelper.getAsString(json, "fluid");
            int amount = GsonHelper.getAsInt(json, "amount");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
            if (fluid == null) {
                throw new com.google.gson.JsonSyntaxException("Unknown fluid: " + fluidName);
            }
            return new FluidStack(fluid, amount);
        }
    }
}