package com.shikateroken.heavy_water_separator.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HwsItems {
    public static final String MOD_ID = "heavy_water_separator";

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    // ブロックをアイテムとして登録
    public static final RegistryObject<Item> DTHWS_ITEM = ITEMS.register("dthws",
            () -> new BlockItem(HwsBlocks.DTHWS.get(), new Item.Properties()));
}

