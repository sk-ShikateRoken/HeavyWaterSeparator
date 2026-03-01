package com.shikateroken.heavy_water_separator.registry;


import com.shikateroken.heavy_water_separator.recipe.FluidToFluidrecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HwsRecipes {
    public static final String Mod_ID = "heavy_water_separator";
//    / シリアライザ (JSONの読み書き) 用のRegister
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Mod_ID);

    // レシピタイプ (種類) 用のRegister (※1.20.1では Registries.RECIPE_TYPE を指定します)
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Mod_ID);

    // シリアライザの登録
    public static final RegistryObject<RecipeSerializer<FluidToFluidrecipe>> FLUID_TO_FLUID_SERIALIZER =
            SERIALIZERS.register("fluid_to_fluid", () -> FluidToFluidrecipe.Serializer.INSTANCE);

    // レシピタイプの登録
    public static final RegistryObject<RecipeType<FluidToFluidrecipe>> FLUID_TO_FLUID_TYPE =
            TYPES.register("fluid_to_fluid", () -> FluidToFluidrecipe.Type.INSTANCE);
}

