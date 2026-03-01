package com.shikateroken.heavy_water_separator.compat;


import com.shikateroken.heavy_water_separator.tile.TileEntityDTHWS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class HwsJadePlugin implements IWailaPlugin {

    // クライアント側（画面の描画）の登録
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(DTHWSProvider.INSTANCE, Block.class);
    }

    // サーバー側（データの送信）の登録
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(DTHWSProvider.INSTANCE, TileEntityDTHWS.class);
    }

    // サーバー処理とクライアント処理を両方担当するプロバイダ
    public static class DTHWSProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        public static final DTHWSProvider INSTANCE = new DTHWSProvider();

        // ① サーバー側の処理：プレイヤーがブロックを見た時、このデータを送る
        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof TileEntityDTHWS tile) {
                // 出力タンクの「液体の種類と量」と「最大容量」をNBTに書き込んで送る
                data.put("OutputFluid", tile.outputTank.getFluid().writeToNBT(new CompoundTag()));
                data.putInt("OutputCapacity", tile.outputTank.getCapacity());
            }
        }

        // ② クライアント側の処理：送られてきたデータを使って画面に描画する
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (accessor.getServerData().contains("OutputFluid")) {
                CompoundTag fluidTag = accessor.getServerData().getCompound("OutputFluid");
                FluidStack outputFluid = FluidStack.loadFluidStackFromNBT(fluidTag);
                int capacity = accessor.getServerData().getInt("OutputCapacity");

                // 確実かつ安全に描画するため、液体の名前と数値をテキストで表示
                // 液体が空の場合は "Empty"、入っている場合はその液体の名前を取得
                Component fluidName = outputFluid.isEmpty() ? Component.literal("Empty") : outputFluid.getDisplayName();

                // "Output: 溶岩 (100 / 8000 mB)" のような形式でツールチップに追加
                tooltip.add(Component.literal("Output: ").append(fluidName).append(" (" + outputFluid.getAmount() + " / " + capacity + " mB)"));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return new ResourceLocation("heavy_water_separator:dthws_provider");
        }
    }
}