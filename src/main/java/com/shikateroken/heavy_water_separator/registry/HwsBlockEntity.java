package com.shikateroken.heavy_water_separator.registry;

import com.shikateroken.heavy_water_separator.tile.TileEntityDTHWS;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HwsBlockEntity {
    public static final String MOD_ID = "heavy_water_separator";

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);

    // BlockEntityの登録と、どのブロックに紐づくかの設定
    public static final RegistryObject<BlockEntityType<TileEntityDTHWS>>
            TE_DTHWS = BLOCK_ENTITIES.register("dtwhs",
                    () -> BlockEntityType.Builder.of(TileEntityDTHWS::new, HwsBlocks.DTHWS.get()).build(null));
}
