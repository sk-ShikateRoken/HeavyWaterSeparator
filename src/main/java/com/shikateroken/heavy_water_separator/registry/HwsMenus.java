package com.shikateroken.heavy_water_separator.registry;

import com.shikateroken.heavy_water_separator.menu.DTHWSMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HwsMenus {public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, "heavy_water_separator");

    public static final RegistryObject<MenuType<DTHWSMenu>> DTHWS_MENU =
            MENUS.register("dthws_menu", () -> IForgeMenuType.create(DTHWSMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

}
