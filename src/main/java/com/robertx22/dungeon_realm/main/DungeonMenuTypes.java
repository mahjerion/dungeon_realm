package com.robertx22.dungeon_realm.main;

import com.robertx22.dungeon_realm.block_entity.MapDeviceMenu;
import com.robertx22.dungeon_realm.block_entity.MapDeviceScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegistryObject;

public class DungeonMenuTypes {
    public static final RegistryObject<MenuType<MapDeviceMenu>> MAP_DEVICE_MENU_TYPE = registerMenuType("map_device_menu", MapDeviceMenu::new);

    public static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return DungeonEntries.MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        DungeonEntries.MENUS.register(eventBus);
    }

    @Mod.EventBusSubscriber(modid = "dungeon_realm", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> MenuScreens.register(MAP_DEVICE_MENU_TYPE.get(), MapDeviceScreen::new));
        }
    }
}
