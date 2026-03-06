package com.shikateroken.heavy_water_separator.client;

import com.shikateroken.heavy_water_separator.registry.HwsBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "heavy_water_separator", value = Dist.CLIENT)
public class HwsClientEvents {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        // 対象のアイテムが DTHWS ブロックかどうか判定
        if (event.getItemStack().getItem() == HwsBlocks.DTHWS.get().asItem()) {

            // NBTデータ (BlockEntityTag) を持っているかチェック
            CompoundTag tag = event.getItemStack().getTagElement("BlockEntityTag");
            if (tag != null) {
                // --- エネルギー表示 ---
                if (tag.contains("Energy")) {
                    int energy = tag.getInt("Energy");
                    if (energy > 0) {
                        event.getToolTip().add(Component.literal("Energy: " + energy + " FE").withStyle(ChatFormatting.RED));
                    }
                }

                // --- 液体 (Input) ---
                if (tag.contains("InputTank")) {
                    FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("InputTank"));
                    if (!fluid.isEmpty()) {
                        event.getToolTip().add(Component.literal("Input: " + fluid.getDisplayName().getString() + " (" + fluid.getAmount() + " mB)").withStyle(ChatFormatting.BLUE));
                    }
                }

                // --- 液体 (Output) ---
                if (tag.contains("OutputTank")) {
                    FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("OutputTank"));
                    if (!fluid.isEmpty()) {
                        event.getToolTip().add(Component.literal("Output: " + fluid.getDisplayName().getString() + " (" + fluid.getAmount() + " mB)").withStyle(ChatFormatting.GOLD));
                    }
                }
            }
        }
    }
}