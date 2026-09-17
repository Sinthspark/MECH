package com.sinthspark.mech.client;

import com.sinthspark.mech.client.screen.*;
import com.sinthspark.mech.screen.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "mech", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(ModMenuTypes.PRESS_MENU.get(), PressScreen::new);
        MenuScreens.register(ModMenuTypes.SAWMILL_MENU.get(), SawmillScreen::new);
        MenuScreens.register(ModMenuTypes.QUERN_MENU.get(), QuernScreen::new);
        MenuScreens.register(ModMenuTypes.OIL_PRESS_MENU.get(), OilPressScreen::new);
        MenuScreens.register(ModMenuTypes.BOILER_MENU.get(), BoilerScreen::new);
        MenuScreens.register(ModMenuTypes.STEAM_FURNACE_MENU.get(), SteamFurnaceScreen::new);
        MenuScreens.register(ModMenuTypes.STEAM_ALLOY_FURNACE_MENU.get(), SteamAlloyFurnaceScreen::new);
        MenuScreens.register(ModMenuTypes.STEAM_GRINDER_MENU.get(), SteamGrinderScreen::new);
        MenuScreens.register(ModMenuTypes.STEAM_COMPRESSOR_MENU.get(), SteamCompressorScreen::new);
        MenuScreens.register(ModMenuTypes.STEAM_PRESS_MENU.get(), SteamPressScreen::new);
        MenuScreens.register(ModMenuTypes.STEAM_SAWMILL_MENU.get(), SteamSawmillScreen::new);
        MenuScreens.register(ModMenuTypes.STEAM_EXTRACTOR_MENU.get(), SteamExtractorScreen::new);
        MenuScreens.register(ModMenuTypes.COKE_OVEN_MENU.get(), CokeOvenScreen::new);
        MenuScreens.register(ModMenuTypes.BLAST_FURNACE_MENU.get(), BlastFurnaceScreen::new);
        MenuScreens.register(ModMenuTypes.UPGRADED_BOILER_MENU.get(), UpgradedBoilerScreen::new);
        MenuScreens.register(ModMenuTypes.UPGRADED_STEAM_FURNACE_MENU.get(), UpgradedSteamFurnaceScreen::new);
        MenuScreens.register(ModMenuTypes.UPGRADED_STEAM_ALLOY_FURNACE_MENU.get(), UpgradedSteamAlloyFurnaceScreen::new);
        MenuScreens.register(ModMenuTypes.UPGRADED_STEAM_GRINDER_MENU.get(), UpgradedSteamGrinderScreen::new);
        MenuScreens.register(ModMenuTypes.UPGRADED_STEAM_COMPRESSOR_MENU.get(), UpgradedSteamCompressorScreen::new);
        MenuScreens.register(ModMenuTypes.UPGRADED_STEAM_PRESS_MENU.get(), UpgradedSteamPressScreen::new);
        MenuScreens.register(ModMenuTypes.UPGRADED_STEAM_SAWMILL_MENU.get(), UpgradedSteamSawmillScreen::new);
        MenuScreens.register(ModMenuTypes.UPGRADED_STEAM_EXTRACTOR_MENU.get(), UpgradedSteamExtractorScreen::new);
        MenuScreens.register(ModMenuTypes.OPEN_HEARTH_MENU.get(), OpenHearthScreen::new);
        MenuScreens.register(ModMenuTypes.ADVANCED_BOILER_MENU.get(), AdvancedBoilerScreen::new);
        MenuScreens.register(ModMenuTypes.ADVANCED_STEAM_FURNACE_MENU.get(), AdvancedSteamFurnaceScreen::new);
        MenuScreens.register(ModMenuTypes.ADVANCED_STEAM_ALLOY_FURNACE_MENU.get(), AdvancedSteamAlloyFurnaceScreen::new);
        MenuScreens.register(ModMenuTypes.ADVANCED_STEAM_GRINDER_MENU.get(), AdvancedSteamGrinderScreen::new);
        MenuScreens.register(ModMenuTypes.ADVANCED_STEAM_COMPRESSOR_MENU.get(), AdvancedSteamCompressorScreen::new);
        MenuScreens.register(ModMenuTypes.ADVANCED_STEAM_PRESS_MENU.get(), AdvancedSteamPressScreen::new);
        MenuScreens.register(ModMenuTypes.ADVANCED_STEAM_SAWMILL_MENU.get(), AdvancedSteamSawmillScreen::new);
        MenuScreens.register(ModMenuTypes.ADVANCED_STEAM_EXTRACTOR_MENU.get(), AdvancedSteamExtractorScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_FURNACE_MENU.get(), ElectricFurnaceScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_ALLOY_FURNACE_MENU.get(), ElectricAlloyFurnaceScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_COMPRESSOR_MENU.get(), ElectricCompressorScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_EXTRACTOR_MENU.get(), ElectricExtractorScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_GRINDER_MENU.get(), ElectricGrinderScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_BENDER_MENU.get(), ElectricBenderScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_CUTTER_MENU.get(), ElectricCutterScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_LATHE_MENU.get(), ElectricLatheScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_WIREMILL_MENU.get(), ElectricWiremillScreen::new);
        MenuScreens.register(ModMenuTypes.DISTILLER_MENU.get(), DistillerScreen::new);
        MenuScreens.register(ModMenuTypes.CONDENSER_MENU.get(), CondenserScreen::new);
        MenuScreens.register(ModMenuTypes.CHEMICAL_REACTOR_MENU.get(), ChemicalReactorScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTROLYZER_MENU.get(), ElectrolyzerScreen::new);
    }
}
