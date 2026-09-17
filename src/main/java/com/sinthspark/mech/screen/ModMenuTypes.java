package com.sinthspark.mech.screen;

import com.sinthspark.mech.MECH;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MECH.MOD_ID);

    public static final RegistryObject<MenuType<PressMenu>> PRESS_MENU =
            MENUS.register("press_menu", () -> IForgeMenuType.create(PressMenu::new));

    public static final RegistryObject<MenuType<SawmillMenu>> SAWMILL_MENU =
            MENUS.register("sawmill_menu", () -> IForgeMenuType.create(SawmillMenu::new));

    public static final RegistryObject<MenuType<QuernMenu>> QUERN_MENU =
            MENUS.register("quern_menu", () -> IForgeMenuType.create(QuernMenu::new));

    public static final RegistryObject<MenuType<OilPressMenu>> OIL_PRESS_MENU =
            MENUS.register("oil_press_menu", () -> IForgeMenuType.create(OilPressMenu::new));

    public static final RegistryObject<MenuType<BoilerMenu>> BOILER_MENU =
            MENUS.register("boiler_menu", () -> IForgeMenuType.create((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                Level level = inv.player.level();
                return new BoilerMenu(windowId, inv, pos, level);
            }));

    public static final RegistryObject<MenuType<SteamFurnaceMenu>> STEAM_FURNACE_MENU =
            MENUS.register("steam_furnace", () -> IForgeMenuType.create(SteamFurnaceMenu::new));

    public static final RegistryObject<MenuType<SteamAlloyFurnaceMenu>> STEAM_ALLOY_FURNACE_MENU =
            MENUS.register("steam_alloy_furnace", () -> IForgeMenuType.create(SteamAlloyFurnaceMenu::new));

    public static final RegistryObject<MenuType<SteamGrinderMenu>> STEAM_GRINDER_MENU =
            MENUS.register("steam_grinder", () -> IForgeMenuType.create(SteamGrinderMenu::new));

    public static final RegistryObject<MenuType<SteamCompressorMenu>> STEAM_COMPRESSOR_MENU =
            MENUS.register("steam_compressor", () -> IForgeMenuType.create(SteamCompressorMenu::new));

    public static final RegistryObject<MenuType<SteamPressMenu>> STEAM_PRESS_MENU =
            MENUS.register("steam_press", () -> IForgeMenuType.create(SteamPressMenu::new));

    public static final RegistryObject<MenuType<SteamSawmillMenu>> STEAM_SAWMILL_MENU =
            MENUS.register("steam_sawmill", () -> IForgeMenuType.create(SteamSawmillMenu::new));

    public static final RegistryObject<MenuType<SteamExtractorMenu>> STEAM_EXTRACTOR_MENU =
            MENUS.register("steam_extractor", () -> IForgeMenuType.create(SteamExtractorMenu::new));

    public static final RegistryObject<MenuType<CokeOvenMenu>> COKE_OVEN_MENU =
            MENUS.register("coke_oven", () -> IForgeMenuType.create(CokeOvenMenu::new));

    public static final RegistryObject<MenuType<BlastFurnaceMenu>> BLAST_FURNACE_MENU =
            MENUS.register("blast_furnace", () -> IForgeMenuType.create(BlastFurnaceMenu::new));

    public static final RegistryObject<MenuType<UpgradedBoilerMenu>> UPGRADED_BOILER_MENU =
            MENUS.register("upgraded_boiler_menu", () -> IForgeMenuType.create((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                Level level = inv.player.level();
                return new UpgradedBoilerMenu(windowId, inv, pos, level);
            }));

    public static final RegistryObject<MenuType<UpgradedSteamFurnaceMenu>> UPGRADED_STEAM_FURNACE_MENU =
            MENUS.register("upgraded_steam_furnace", () -> IForgeMenuType.create(UpgradedSteamFurnaceMenu::new));

    public static final RegistryObject<MenuType<UpgradedSteamAlloyFurnaceMenu>> UPGRADED_STEAM_ALLOY_FURNACE_MENU =
            MENUS.register("upgraded_steam_alloy_furnace", () -> IForgeMenuType.create(UpgradedSteamAlloyFurnaceMenu::new));

    public static final RegistryObject<MenuType<UpgradedSteamGrinderMenu>> UPGRADED_STEAM_GRINDER_MENU =
            MENUS.register("upgraded_steam_grinder", () -> IForgeMenuType.create(UpgradedSteamGrinderMenu::new));

    public static final RegistryObject<MenuType<UpgradedSteamCompressorMenu>> UPGRADED_STEAM_COMPRESSOR_MENU =
            MENUS.register("upgraded_steam_compressor", () -> IForgeMenuType.create(UpgradedSteamCompressorMenu::new));

    public static final RegistryObject<MenuType<UpgradedSteamPressMenu>> UPGRADED_STEAM_PRESS_MENU =
            MENUS.register("upgraded_steam_press", () -> IForgeMenuType.create(UpgradedSteamPressMenu::new));

    public static final RegistryObject<MenuType<UpgradedSteamSawmillMenu>> UPGRADED_STEAM_SAWMILL_MENU =
            MENUS.register("upgraded_steam_sawmill", () -> IForgeMenuType.create(UpgradedSteamSawmillMenu::new));

    public static final RegistryObject<MenuType<UpgradedSteamExtractorMenu>> UPGRADED_STEAM_EXTRACTOR_MENU =
            MENUS.register("upgraded_steam_extractor", () -> IForgeMenuType.create(UpgradedSteamExtractorMenu::new));

    public static final RegistryObject<MenuType<OpenHearthMenu>> OPEN_HEARTH_MENU =
            MENUS.register("open_hearth", () -> IForgeMenuType.create(OpenHearthMenu::new));

    public static final RegistryObject<MenuType<AdvancedBoilerMenu>> ADVANCED_BOILER_MENU =
            MENUS.register("advanced_boiler_menu", () -> IForgeMenuType.create((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                Level level = inv.player.level();
                return new AdvancedBoilerMenu(windowId, inv, pos, level);
            }));

    public static final RegistryObject<MenuType<AdvancedSteamFurnaceMenu>> ADVANCED_STEAM_FURNACE_MENU =
            MENUS.register("advanced_steam_furnace", () -> IForgeMenuType.create(AdvancedSteamFurnaceMenu::new));

    public static final RegistryObject<MenuType<AdvancedSteamAlloyFurnaceMenu>> ADVANCED_STEAM_ALLOY_FURNACE_MENU =
            MENUS.register("advanced_steam_alloy_furnace", () -> IForgeMenuType.create(AdvancedSteamAlloyFurnaceMenu::new));

    public static final RegistryObject<MenuType<AdvancedSteamGrinderMenu>> ADVANCED_STEAM_GRINDER_MENU =
            MENUS.register("advanced_steam_grinder", () -> IForgeMenuType.create(AdvancedSteamGrinderMenu::new));

    public static final RegistryObject<MenuType<AdvancedSteamCompressorMenu>> ADVANCED_STEAM_COMPRESSOR_MENU =
            MENUS.register("advanced_steam_compressor", () -> IForgeMenuType.create(AdvancedSteamCompressorMenu::new));

    public static final RegistryObject<MenuType<AdvancedSteamPressMenu>> ADVANCED_STEAM_PRESS_MENU =
            MENUS.register("advanced_steam_press", () -> IForgeMenuType.create(AdvancedSteamPressMenu::new));

    public static final RegistryObject<MenuType<AdvancedSteamSawmillMenu>> ADVANCED_STEAM_SAWMILL_MENU =
            MENUS.register("advanced_steam_sawmill", () -> IForgeMenuType.create(AdvancedSteamSawmillMenu::new));

    public static final RegistryObject<MenuType<AdvancedSteamExtractorMenu>> ADVANCED_STEAM_EXTRACTOR_MENU =
            MENUS.register("advanced_steam_extractor", () -> IForgeMenuType.create(AdvancedSteamExtractorMenu::new));

    public static final RegistryObject<MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE_MENU =
            MENUS.register("electric_furnace", () -> IForgeMenuType.create(ElectricFurnaceMenu::new));

    public static final RegistryObject<MenuType<ElectricAlloyFurnaceMenu>> ELECTRIC_ALLOY_FURNACE_MENU =
            MENUS.register("electric_alloy_furnace", () -> IForgeMenuType.create(ElectricAlloyFurnaceMenu::new));

    public static final RegistryObject<MenuType<ElectricCompressorMenu>> ELECTRIC_COMPRESSOR_MENU =
            MENUS.register("electric_compressor", () -> IForgeMenuType.create(ElectricCompressorMenu::new));

    public static final RegistryObject<MenuType<ElectricExtractorMenu>> ELECTRIC_EXTRACTOR_MENU =
            MENUS.register("electric_extractor", () -> IForgeMenuType.create(ElectricExtractorMenu::new));

    public static final RegistryObject<MenuType<ElectricGrinderMenu>> ELECTRIC_GRINDER_MENU =
            MENUS.register("electric_grinder", () -> IForgeMenuType.create(ElectricGrinderMenu::new));

    public static final RegistryObject<MenuType<ElectricBenderMenu>> ELECTRIC_BENDER_MENU =
            MENUS.register("electric_bender", () -> IForgeMenuType.create(ElectricBenderMenu::new));

    public static final RegistryObject<MenuType<ElectricCutterMenu>> ELECTRIC_CUTTER_MENU =
            MENUS.register("electric_cutter", () -> IForgeMenuType.create(ElectricCutterMenu::new));

    public static final RegistryObject<MenuType<ElectricLatheMenu>> ELECTRIC_LATHE_MENU =
            MENUS.register("electric_lathe", () -> IForgeMenuType.create(ElectricLatheMenu::new));

    public static final RegistryObject<MenuType<ElectricWiremillMenu>> ELECTRIC_WIREMILL_MENU =
            MENUS.register("electric_wiremill", () -> IForgeMenuType.create(ElectricWiremillMenu::new));

    public static final RegistryObject<MenuType<DistillerMenu>> DISTILLER_MENU =
            MENUS.register("distiller", () -> IForgeMenuType.create(DistillerMenu::new));

    public static final RegistryObject<MenuType<CondenserMenu>> CONDENSER_MENU =
            MENUS.register("condenser", () -> IForgeMenuType.create(CondenserMenu::new));

    public static final RegistryObject<MenuType<ChemicalReactorMenu>> CHEMICAL_REACTOR_MENU =
            MENUS.register("chemical_reactor", () -> IForgeMenuType.create(ChemicalReactorMenu::new));

    public static final RegistryObject<MenuType<ElectrolyzerMenu>> ELECTROLYZER_MENU =
            MENUS.register("electrolyzer", () -> IForgeMenuType.create(ElectrolyzerMenu::new));
}
