package com.shikateroken.heavy_water_separator.registry;

import com.shikateroken.heavy_water_separator.tile.TileDTHWS;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HwsBlocks {
    public static final String MOD_ID = "heavy_water_separator";

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

    // ブロックの登録（鉄ブロックの性質をコピー）
    public static final RegistryObject<Block> DTHWS = BLOCKS.register("dtwhs",
            () -> new TileDTHWS(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
}

