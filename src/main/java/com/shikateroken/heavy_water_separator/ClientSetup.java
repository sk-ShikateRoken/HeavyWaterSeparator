package com.shikateroken.heavy_water_separator;

import com.shikateroken.heavy_water_separator.client.gui.DTHWSScreen;
import com.shikateroken.heavy_water_separator.registry.HwsMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    @Mod.EventBusSubscriber(modid = "heavy_water_separator", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // MenuとScreenを紐付ける
            event.enqueueWork(() -> {
                MenuScreens.register(HwsMenus.DTHWS_MENU.get(), DTHWSScreen::new);
            });
        }
    }
}
