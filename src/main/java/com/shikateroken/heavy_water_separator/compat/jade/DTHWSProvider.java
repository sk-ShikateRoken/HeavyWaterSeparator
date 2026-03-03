package com.shikateroken.heavy_water_separator.compat.jade;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import snownee.jade.api.*;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IBoxStyle;
import snownee.jade.api.ui.IElementHelper;

public class DTHWSProvider implements IBlockComponentProvider {

    public static final DTHWSProvider INSTANCE = new DTHWSProvider();

    // 何もしない枠スタイルを定義 (使い回すため)
    private static final IBoxStyle EMPTY_BOX = new IBoxStyle() {
        @Override
        public float borderWidth() {
            return 0;
        }

        @Override
        public void render(GuiGraphics gui, float x, float y, float w, float h) {
            // 何もしない = 枠を描画しない
        }
    };

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        IElementHelper helper = IElementHelper.get();

        // NBTからデータを読み取ってバーを表示する処理はそのまま
        if (data.contains("InputTank")) {
            // 入力タンク
            FluidStack inputFluid = FluidStack.loadFluidStackFromNBT(data.getCompound("InputTank"));
            int inputCap = data.getInt("InputCapacity");

            // 入力タンク (Input)
            if (inputCap > 0) {
                float ratio = (float) inputFluid.getAmount() / inputCap;
                Component text = Component.literal(inputFluid.getDisplayName().getString() + ": " + inputFluid.getAmount() + " mB");
                if (inputFluid.isEmpty()) text = Component.literal("Input: Empty");

                // 第4引数に EMPTY_BOX を渡す
                tooltip.add(helper.progress(ratio, text,
                        helper.progressStyle().color(0xFF3333FF, 0xFF0000AA),
                        EMPTY_BOX, // ★ここ！
                        true
                ));
            }
            //出力タンク
            FluidStack outputFluid = FluidStack.loadFluidStackFromNBT(data.getCompound("OutputTank"));
            int outputCap = data.getInt("OutputCapacity");

            if (outputCap > 0) {
                float ratio = (float) outputFluid.getAmount() / outputCap;
                Component text = Component.literal(outputFluid.getDisplayName().getString() + ": " + outputFluid.getAmount() + " mB");
                if (outputFluid.isEmpty()) text = Component.literal("Output: Empty");

                // 第4引数に EMPTY_BOX を渡す
                tooltip.add(helper.progress(ratio, text,
                        helper.progressStyle().color(0xFFFF8800, 0xFFAA4400),
                        EMPTY_BOX, // ★ここ！
                        true
                ));
            }
        }
        //エネルギー
        if (data.contains("EnergyStored")) {
            int energyStored = data.getInt("EnergyStored");
            int maxEnergy = data.getInt("MaxEnergy");

            if (maxEnergy > 0) {
                // 比率計算
                float ratio = (float) energyStored / maxEnergy;

                // テキスト作成 (例: "Energy: 1000 FE")
                Component text = Component.literal("Energy: " + energyStored + " FE");
                if (energyStored == 0) text = Component.literal("Energy: Empty");

                // バーを描画 (色は赤: 0xFFFF0000)
                tooltip.add(helper.progress(ratio, text,
                        helper.progressStyle().color(0xFFFF0000, 0xFF550000), // 赤色 / 暗い赤色
                        EMPTY_BOX, // さっき作った空のボックススタイル
                        true
                ));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("heavy_water_separator", "dthws_provider");
    }
}
