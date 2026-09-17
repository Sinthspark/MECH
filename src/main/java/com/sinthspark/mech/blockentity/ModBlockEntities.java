package com.sinthspark.mech.blockentity;

import com.mojang.datafixers.types.Type;
import com.sinthspark.mech.MECH;
import com.sinthspark.mech.blocks.PipeBlock;
import com.sinthspark.mech.blocks.WireBlock;
import com.sinthspark.mech.registration.BlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MECH.MOD_ID);

   private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(
           String name,
           BlockEntityType.BlockEntitySupplier<T> factory,
           Supplier<? extends Block> validBlockSupplier) {
       return BLOCK_ENTITIES.register(name, () ->
               BlockEntityType.Builder.of(factory, validBlockSupplier.get()).build((Type<?>) null)
       );
   }

    public static final RegistryObject<BlockEntityType<WaterWheelTile>> WATER_WHEEL =
            register("water_wheel", WaterWheelTile::new, BlockRegistry.WATER_WHEEL);

    public static final RegistryObject<BlockEntityType<QuernTile>> QUERN =
            register("quern", QuernTile::new, BlockRegistry.QUERN);

    public static final RegistryObject<BlockEntityType<PressTile>> PRESS =
            register("press", PressTile::new, BlockRegistry.PRESS);

    public static final RegistryObject<BlockEntityType<VulcaniteGearTile>> VULCANITE_GEAR =
            register("vulcanite_gear", VulcaniteGearTile::new, BlockRegistry.VULCANITE_GEAR);

    public static final RegistryObject<BlockEntityType<SawmillTile>> SAWMILL =
            register("sawmill", SawmillTile::new, BlockRegistry.SAWMILL);

    public static final RegistryObject<BlockEntityType<VulcaniteBearingTile>> VULCANITE_BEARING =
            register("vulcanite_bearing", VulcaniteBearingTile::new, BlockRegistry.VULCANITE_BEARING);

    public static final RegistryObject<BlockEntityType<ManualKineticGeneratorTile>> MANUAL_KINETIC_GENERATOR =
            register("manual_kinetic_generator", ManualKineticGeneratorTile::new, BlockRegistry.MANUAL_KINETIC_GENERATOR);

    public static final RegistryObject<BlockEntityType<OilPressTile>> OIL_PRESS =
            register("oil_press", OilPressTile::new, BlockRegistry.OIL_PRESS);

    public static final RegistryObject<BlockEntityType<BoilerTankTile>> BOILER_TANK =
            register("boiler_tank", BoilerTankTile::new, BlockRegistry.BOILER_TANK);

    public static final RegistryObject<BlockEntityType<FireboxTile>> FIREBOX =
            register("firebox", FireboxTile::new, BlockRegistry.FIREBOX);

    public static final RegistryObject<BlockEntityType<SteamFurnaceTile>> STEAM_FURNACE =
            register("steam_furnace", SteamFurnaceTile::new, BlockRegistry.STEAM_FURNACE);

    public static final RegistryObject<BlockEntityType<SteamAlloyFurnaceTile>> STEAM_ALLOY_FURNACE =
            register("steam_alloy_furnace", SteamAlloyFurnaceTile::new, BlockRegistry.STEAM_ALLOY_FURNACE);

    public static final RegistryObject<BlockEntityType<SteamGrinderTile>> STEAM_GRINDER =
            register("steam_grinder", SteamGrinderTile::new, BlockRegistry.STEAM_GRINDER);

    public static final RegistryObject<BlockEntityType<SteamCompressorTile>> STEAM_COMPRESSOR =
            register("steam_compressor", SteamCompressorTile::new, BlockRegistry.STEAM_COMPRESSOR);

    public static final RegistryObject<BlockEntityType<SteamPressTile>> STEAM_PRESS =
            register("steam_press", SteamPressTile::new, BlockRegistry.STEAM_PRESS);

    public static final RegistryObject<BlockEntityType<SteamSawmillTile>> STEAM_SAWMILL =
            register("steam_sawmill", SteamSawmillTile::new, BlockRegistry.STEAM_SAWMILL);

    public static final RegistryObject<BlockEntityType<SteamExtractorTile>> STEAM_EXTRACTOR =
            register("steam_extractor", SteamExtractorTile::new, BlockRegistry.STEAM_EXTRACTOR);

    public static final RegistryObject<BlockEntityType<CokeOvenControllerTile>> COKE_OVEN_CONTROLLER =
            register("coke_oven_controller", CokeOvenControllerTile::new, BlockRegistry.COKE_OVEN_CONTROLLER);

    public static final RegistryObject<BlockEntityType<BlastFurnaceControllerTile>> BLAST_FURNACE_CONTROLLER =
            register("blast_furnace_controller", BlastFurnaceControllerTile::new, BlockRegistry.BLAST_FURNACE_CONTROLLER);

    public static final RegistryObject<BlockEntityType<UpgradedBoilerTankTile>> UPGRADED_BOILER_TANK =
            register("upgraded_boiler_tank", UpgradedBoilerTankTile::new, BlockRegistry.UPGRADED_BOILER_TANK);

    public static final RegistryObject<BlockEntityType<UpgradedFireboxTile>> UPGRADED_FIREBOX =
            register("upgraded_firebox", UpgradedFireboxTile::new, BlockRegistry.UPGRADED_FIREBOX);

    public static final RegistryObject<BlockEntityType<UpgradedSteamFurnaceTile>> UPGRADED_STEAM_FURNACE =
            register("upgraded_steam_furnace", UpgradedSteamFurnaceTile::new, BlockRegistry.UPGRADED_STEAM_FURNACE);

    public static final RegistryObject<BlockEntityType<UpgradedSteamAlloyFurnaceTile>> UPGRADED_STEAM_ALLOY_FURNACE =
            register("upgraded_steam_alloy_furnace", UpgradedSteamAlloyFurnaceTile::new, BlockRegistry.UPGRADED_STEAM_ALLOY_FURNACE);

    public static final RegistryObject<BlockEntityType<UpgradedSteamGrinderTile>> UPGRADED_STEAM_GRINDER =
            register("upgraded_steam_grinder", UpgradedSteamGrinderTile::new, BlockRegistry.UPGRADED_STEAM_GRINDER);

    public static final RegistryObject<BlockEntityType<UpgradedSteamCompressorTile>> UPGRADED_STEAM_COMPRESSOR =
            register("upgraded_steam_compressor", UpgradedSteamCompressorTile::new, BlockRegistry.UPGRADED_STEAM_COMPRESSOR);

    public static final RegistryObject<BlockEntityType<UpgradedSteamPressTile>> UPGRADED_STEAM_PRESS =
            register("upgraded_steam_press", UpgradedSteamPressTile::new, BlockRegistry.UPGRADED_STEAM_PRESS);

    public static final RegistryObject<BlockEntityType<UpgradedSteamSawmillTile>> UPGRADED_STEAM_SAWMILL =
            register("upgraded_steam_sawmill", UpgradedSteamSawmillTile::new, BlockRegistry.UPGRADED_STEAM_SAWMILL);

    public static final RegistryObject<BlockEntityType<UpgradedSteamExtractorTile>> UPGRADED_STEAM_EXTRACTOR =
            register("upgraded_steam_extractor", UpgradedSteamExtractorTile::new, BlockRegistry.UPGRADED_STEAM_EXTRACTOR);

    public static final RegistryObject<BlockEntityType<OpenHearthControllerTile>> OPEN_HEARTH_CONTROLLER =
            register("open_hearth_controller", OpenHearthControllerTile::new, BlockRegistry.OPEN_HEARTH_CONTROLLER);

    public static final RegistryObject<BlockEntityType<OpenHearthFluidHatchTile>> OPEN_HEARTH_FLUID_HATCH =
            register("open_hearth_fluid_hatch", OpenHearthFluidHatchTile::new, BlockRegistry.OPEN_HEARTH_FLUID_HATCH);

    public static final RegistryObject<BlockEntityType<AdvancedBoilerTankTile>> ADVANCED_BOILER_TANK =
            register("advanced_boiler_tank", AdvancedBoilerTankTile::new, BlockRegistry.ADVANCED_BOILER_TANK);

    public static final RegistryObject<BlockEntityType<AdvancedFireboxTile>> ADVANCED_FIREBOX =
            register("advanced_firebox", AdvancedFireboxTile::new, BlockRegistry.ADVANCED_FIREBOX);

    public static final RegistryObject<BlockEntityType<AdvancedSteamFurnaceTile>> ADVANCED_STEAM_FURNACE =
            register("advanced_steam_furnace", AdvancedSteamFurnaceTile::new, BlockRegistry.ADVANCED_STEAM_FURNACE);

    public static final RegistryObject<BlockEntityType<AdvancedSteamAlloyFurnaceTile>> ADVANCED_STEAM_ALLOY_FURNACE =
            register("advanced_steam_alloy_furnace", AdvancedSteamAlloyFurnaceTile::new, BlockRegistry.ADVANCED_STEAM_ALLOY_FURNACE);

    public static final RegistryObject<BlockEntityType<AdvancedSteamGrinderTile>> ADVANCED_STEAM_GRINDER =
            register("advanced_steam_grinder", AdvancedSteamGrinderTile::new, BlockRegistry.ADVANCED_STEAM_GRINDER);

    public static final RegistryObject<BlockEntityType<AdvancedSteamCompressorTile>> ADVANCED_STEAM_COMPRESSOR =
            register("advanced_steam_compressor", AdvancedSteamCompressorTile::new, BlockRegistry.ADVANCED_STEAM_COMPRESSOR);

    public static final RegistryObject<BlockEntityType<AdvancedSteamPressTile>> ADVANCED_STEAM_PRESS =
            register("advanced_steam_press", AdvancedSteamPressTile::new, BlockRegistry.ADVANCED_STEAM_PRESS);

    public static final RegistryObject<BlockEntityType<AdvancedSteamSawmillTile>> ADVANCED_STEAM_SAWMILL =
            register("advanced_steam_sawmill", AdvancedSteamSawmillTile::new, BlockRegistry.ADVANCED_STEAM_SAWMILL);

    public static final RegistryObject<BlockEntityType<AdvancedSteamExtractorTile>> ADVANCED_STEAM_EXTRACTOR =
            register("advanced_steam_extractor", AdvancedSteamExtractorTile::new, BlockRegistry.ADVANCED_STEAM_EXTRACTOR);

    public static final RegistryObject<BlockEntityType<SteamTurbineTile>> STEAM_TURBINE =
            register("steam_turbine", SteamTurbineTile::new, BlockRegistry.STEAM_TURBINE);

    public static final RegistryObject<BlockEntityType<ElectricGeneratorTile>> ELECTRIC_GENERATOR =
            register("electric_generator", ElectricGeneratorTile::new, BlockRegistry.ELECTRIC_GENERATOR);

    public static final RegistryObject<BlockEntityType<ElectricFurnaceTile>> ELECTRIC_FURNACE =
            register("electric_furnace", ElectricFurnaceTile::new, BlockRegistry.ELECTRIC_FURNACE);

    public static final RegistryObject<BlockEntityType<ElectricAlloyFurnaceTile>> ELECTRIC_ALLOY_FURNACE =
            register("electric_alloy_furnace", ElectricAlloyFurnaceTile::new, BlockRegistry.ELECTRIC_ALLOY_FURNACE);

    public static final RegistryObject<BlockEntityType<ElectricCompressorTile>> ELECTRIC_COMPRESSOR =
            register("electric_compressor", ElectricCompressorTile::new, BlockRegistry.ELECTRIC_COMPRESSOR);

    public static final RegistryObject<BlockEntityType<ElectricExtractorTile>> ELECTRIC_EXTRACTOR =
            register("electric_extractor", ElectricExtractorTile::new, BlockRegistry.ELECTRIC_EXTRACTOR);

    public static final RegistryObject<BlockEntityType<ElectricGrinderTile>> ELECTRIC_GRINDER =
            register("electric_grinder", ElectricGrinderTile::new, BlockRegistry.ELECTRIC_GRINDER);

    public static final RegistryObject<BlockEntityType<ElectricBenderTile>> ELECTRIC_BENDER =
            register("electric_bender", ElectricBenderTile::new, BlockRegistry.ELECTRIC_BENDER);

    public static final RegistryObject<BlockEntityType<ElectricCutterTile>> ELECTRIC_CUTTER =
            register("electric_cutter", ElectricCutterTile::new, BlockRegistry.ELECTRIC_CUTTER);

    public static final RegistryObject<BlockEntityType<ElectricWiremillTile>> ELECTRIC_WIREMILL =
            register("electric_wiremill", ElectricWiremillTile::new, BlockRegistry.ELECTRIC_WIREMILL);

    public static final RegistryObject<BlockEntityType<ElectricLatheTile>> ELECTRIC_LATHE =
            register("electric_lathe", ElectricLatheTile::new, BlockRegistry.ELECTRIC_LATHE);

    public static final RegistryObject<BlockEntityType<DistillerTile>> DISTILLER =
            register("distiller", DistillerTile::new, BlockRegistry.DISTILLER);

    public static final RegistryObject<BlockEntityType<CondenserTile>> CONDENSER =
            register("condenser", CondenserTile::new, BlockRegistry.CONDENSER);

    public static final RegistryObject<BlockEntityType<ChemicalReactorTile>> CHEMICAL_REACTOR =
            register("chemical_reactor", ChemicalReactorTile::new, BlockRegistry.CHEMICAL_REACTOR);

    public static final RegistryObject<BlockEntityType<ElectrolyzerTile>> ELECTROLYZER =
            register("electrolyzer", ElectrolyzerTile::new, BlockRegistry.ELECTROLYZER);

    public static final RegistryObject<BlockEntityType<WireTile>> WIRE =
            BLOCK_ENTITIES.register("wire",
                    () -> {
                        BlockEntityType.BlockEntitySupplier<WireTile> factory = (pos, state) -> {
                            if (state.getBlock() instanceof WireBlock wireBlock) {
                                return new WireTile(pos, state,
                                        wireBlock.getMaxTransfer(),
                                        wireBlock.getLossPerBlock(),
                                        wireBlock.isInsulated());
                            }
                            throw new IllegalStateException("WireTile factory called with wrong block: " + state.getBlock());
                        };
                        return BlockEntityType.Builder.of(factory,
                                BlockRegistry.TIN_WIRE.get(),
                                BlockRegistry.COPPER_WIRE.get(),
                                BlockRegistry.INSULATED_TIN_WIRE.get()
                        ).build((Type<?>) null);
                    });

    public static final RegistryObject<BlockEntityType<PipeTile>> PIPE =
            BLOCK_ENTITIES.register("pipe",
                    () -> {
                        BlockEntityType.BlockEntitySupplier<PipeTile> factory = (pos, state) -> {
                            if (state.getBlock() instanceof PipeBlock pipeBlock) {
                                return new PipeTile(pos, state,
                                        pipeBlock.getCapacity(),
                                        pipeBlock.getTransferRate(),
                                        pipeBlock.getCondenseRate());
                            }
                            throw new IllegalStateException("PipeTile factory called with wrong block: " + state.getBlock());
                        };
                        return BlockEntityType.Builder.of(factory,
                                BlockRegistry.TIN_PIPE.get(),
                                BlockRegistry.COPPER_PIPE.get(),
                                BlockRegistry.BRONZE_PIPE.get(),
                                BlockRegistry.IRON_PIPE.get(),
                                BlockRegistry.GOLDEN_PIPE.get(),
                                BlockRegistry.CAST_IRON_PIPE.get(),
                                BlockRegistry.STEEL_PIPE.get()
                        ).build((Type<?>) null);
                    });
}
