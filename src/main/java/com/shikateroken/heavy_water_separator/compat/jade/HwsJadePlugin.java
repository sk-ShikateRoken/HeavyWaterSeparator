package com.shikateroken.heavy_water_separator.compat.jade;

import com.shikateroken.heavy_water_separator.tile.TileDTHWS;
import com.shikateroken.heavy_water_separator.tile.TileEntityDTHWS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.*;

@WailaPlugin
public class HwsJadePlugin implements IWailaPlugin {

    public void register(IWailaCommonRegistration registration) {

        // 【修正】ジェネリクスを <BlockAccessor> に指定します
        registration.registerBlockDataProvider(new IServerDataProvider<BlockAccessor>() {

            // 【修正】引数は (Tag, Accessor) です
            @Override
            public void appendServerData(CompoundTag data, BlockAccessor accessor) {

                // Accessor から BlockEntity を取得します
                BlockEntity entity = accessor.getBlockEntity();

                if (entity instanceof TileEntityDTHWS) {
                    TileEntityDTHWS tile = (TileEntityDTHWS) entity;

                    // 入力タンク
                    CompoundTag inputTag = new CompoundTag();
                    tile.inputTank.writeToNBT(inputTag);
                    data.put("InputTank", inputTag);
                    data.putInt("InputCapacity", tile.inputTank.getCapacity());

                    // 出力タンク
                    CompoundTag outputTag = new CompoundTag();
                    tile.outputTank.writeToNBT(outputTag);
                    data.put("OutputTank", outputTag);
                    data.putInt("OutputCapacity", tile.outputTank.getCapacity());

                    //エネルギー
                    var energy = tile.getEnergyStorage();
                    if (energy != null) {
                        data.putInt("EnergyStored", energy.getEnergyStored());
                        data.putInt("MaxEnergy", energy.getMaxEnergyStored());
                    }
                }
            }

            @Override
            public ResourceLocation getUid() {
                return new ResourceLocation("heavy_water_separator", "dthws_server_data");
            }

        }, TileEntityDTHWS.class);
    }

    // 2. クライアント側の表示
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(DTHWSProvider.INSTANCE, TileDTHWS.class);
    }
}

