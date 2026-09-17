package com.sinthspark.mech.items;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.registration.ItemRegistry;
import com.sinthspark.mech.registration.BlockRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MECH.MOD_ID);

    @SuppressWarnings("unused")
    public static final RegistryObject<CreativeModeTab> MECH_BLOCKS_TAB = CREATIVE_MODE_TABS.register("blocks_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(BlockRegistry.BRONZE_BRICK_CASING.get()))
                    .title(Component.translatable("item_group.mech.blocks"))
                    .displayItems((params, output) -> {
                        output.accept(BlockRegistry.MANUAL_KINETIC_GENERATOR.get());
                        output.accept(BlockRegistry.VULCANITE_GEAR.get());
                        output.accept(BlockRegistry.QUERN.get());
                        output.accept(BlockRegistry.PRESS.get());
                        output.accept(BlockRegistry.SAWMILL.get());
                        output.accept(BlockRegistry.OIL_PRESS.get());
                        output.accept(BlockRegistry.WATER_WHEEL.get());
                        output.accept(BlockRegistry.VULCANITE_BEARING.get());
                        output.accept(BlockRegistry.IRON_PIPE.get());
                        output.accept(BlockRegistry.GOLDEN_PIPE.get());
                        output.accept(BlockRegistry.COPPER_PIPE.get());
                        output.accept(BlockRegistry.TIN_PIPE.get());
                        output.accept(BlockRegistry.BRONZE_PIPE.get());
                        output.accept(BlockRegistry.CAST_IRON_PIPE.get());
                        output.accept(BlockRegistry.STEEL_PIPE.get());
                        output.accept(BlockRegistry.BRONZE_BRICK_CASING.get());
                        output.accept(BlockRegistry.FIREBOX.get());
                        output.accept(BlockRegistry.BOILER_TANK.get());
                        output.accept(BlockRegistry.STEAM_FURNACE.get());
                        output.accept(BlockRegistry.STEAM_ALLOY_FURNACE.get());
                        output.accept(BlockRegistry.STEAM_GRINDER.get());
                        output.accept(BlockRegistry.STEAM_COMPRESSOR.get());
                        output.accept(BlockRegistry.STEAM_PRESS.get());
                        output.accept(BlockRegistry.STEAM_SAWMILL.get());
                        output.accept(BlockRegistry.STEAM_EXTRACTOR.get());
                        output.accept(BlockRegistry.COKE_OVEN_BRICKS.get());
                        output.accept(BlockRegistry.COKE_OVEN_CONTROLLER.get());
                        output.accept(BlockRegistry.BLAST_FURNACE_BRICKS.get());
                        output.accept(BlockRegistry.BLAST_FURNACE_CONTROLLER.get());
                        output.accept(BlockRegistry.CAST_IRON_BRICK_CASING.get());
                        output.accept(BlockRegistry.UPGRADED_FIREBOX.get());
                        output.accept(BlockRegistry.UPGRADED_BOILER_TANK.get());
                        output.accept(BlockRegistry.UPGRADED_STEAM_FURNACE.get());
                        output.accept(BlockRegistry.UPGRADED_STEAM_ALLOY_FURNACE.get());
                        output.accept(BlockRegistry.UPGRADED_STEAM_GRINDER.get());
                        output.accept(BlockRegistry.UPGRADED_STEAM_COMPRESSOR.get());
                        output.accept(BlockRegistry.UPGRADED_STEAM_PRESS.get());
                        output.accept(BlockRegistry.UPGRADED_STEAM_SAWMILL.get());
                        output.accept(BlockRegistry.UPGRADED_STEAM_EXTRACTOR.get());
                        output.accept(BlockRegistry.OPEN_HEARTH_BRICKS.get());
                        output.accept(BlockRegistry.OPEN_HEARTH_CONTROLLER.get());
                        output.accept(BlockRegistry.OPEN_HEARTH_FLUID_HATCH.get());
                        output.accept(BlockRegistry.STEEL_BRICK_CASING.get());
                        output.accept(BlockRegistry.ADVANCED_FIREBOX.get());
                        output.accept(BlockRegistry.ADVANCED_BOILER_TANK.get());
                        output.accept(BlockRegistry.ADVANCED_STEAM_FURNACE.get());
                        output.accept(BlockRegistry.ADVANCED_STEAM_ALLOY_FURNACE.get());
                        output.accept(BlockRegistry.ADVANCED_STEAM_GRINDER.get());
                        output.accept(BlockRegistry.ADVANCED_STEAM_COMPRESSOR.get());
                        output.accept(BlockRegistry.ADVANCED_STEAM_PRESS.get());
                        output.accept(BlockRegistry.ADVANCED_STEAM_SAWMILL.get());
                        output.accept(BlockRegistry.ADVANCED_STEAM_EXTRACTOR.get());
                        output.accept(BlockRegistry.STEEL_MACHINE_CASING.get());
                        output.accept(BlockRegistry.TIN_WIRE.get());
                        output.accept(BlockRegistry.COPPER_WIRE.get());
                        output.accept(BlockRegistry.INSULATED_TIN_WIRE.get());
                        output.accept(BlockRegistry.STEAM_TURBINE.get());
                        output.accept(BlockRegistry.ELECTRIC_GENERATOR.get());
                        output.accept(BlockRegistry.ELECTRIC_FURNACE.get());
                        output.accept(BlockRegistry.ELECTRIC_ALLOY_FURNACE.get());
                        output.accept(BlockRegistry.ELECTRIC_COMPRESSOR.get());
                        output.accept(BlockRegistry.ELECTRIC_EXTRACTOR.get());
                        output.accept(BlockRegistry.ELECTRIC_GRINDER.get());
                        output.accept(BlockRegistry.ELECTRIC_BENDER.get());
                        output.accept(BlockRegistry.ELECTRIC_CUTTER.get());
                        output.accept(BlockRegistry.ELECTRIC_LATHE.get());
                        output.accept(BlockRegistry.ELECTRIC_WIREMILL.get());
                        output.accept(BlockRegistry.DISTILLER.get());
                        output.accept(BlockRegistry.CONDENSER.get());
                        output.accept(BlockRegistry.CHEMICAL_REACTOR.get());
                        output.accept(BlockRegistry.ELECTROLYZER.get());
                    })
                    .build());

    @SuppressWarnings("unused")
    public static final RegistryObject<CreativeModeTab> MECH_TOOLS_TAB = CREATIVE_MODE_TABS.register("tools_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ItemRegistry.IRON_HAMMER.get()))
                    .title(Component.translatable("item_group.mech.tools"))
                    .displayItems((params, output) -> {
                        // Hammers
                        output.accept(ItemRegistry.IRON_HAMMER.get());
                        output.accept(ItemRegistry.GOLDEN_HAMMER.get());
                        output.accept(ItemRegistry.COPPER_HAMMER.get());
                        output.accept(ItemRegistry.DIAMOND_HAMMER.get());
                        output.accept(ItemRegistry.TIN_HAMMER.get());
                        output.accept(ItemRegistry.TELLURIUM_HAMMER.get());
                        output.accept(ItemRegistry.VULCANITE_HAMMER.get());
                        output.accept(ItemRegistry.BRONZE_HAMMER.get());
                        output.accept(ItemRegistry.CAST_IRON_HAMMER.get());
                        output.accept(ItemRegistry.STEEL_HAMMER.get());
                        // Files
                        output.accept(ItemRegistry.IRON_FILE.get());
                        output.accept(ItemRegistry.GOLDEN_FILE.get());
                        output.accept(ItemRegistry.COPPER_FILE.get());
                        output.accept(ItemRegistry.TIN_FILE.get());
                        output.accept(ItemRegistry.BRONZE_FILE.get());
                        output.accept(ItemRegistry.CAST_IRON_FILE.get());
                        output.accept(ItemRegistry.STEEL_FILE.get());
                        // Wrenches
                        output.accept(ItemRegistry.IRON_WRENCH.get());
                        output.accept(ItemRegistry.GOLDEN_WRENCH.get());
                        output.accept(ItemRegistry.COPPER_WRENCH.get());
                        output.accept(ItemRegistry.TIN_WRENCH.get());
                        output.accept(ItemRegistry.BRONZE_WRENCH.get());
                        output.accept(ItemRegistry.CAST_IRON_WRENCH.get());
                        output.accept(ItemRegistry.STEEL_WRENCH.get());
                        // Wire cutters
                        output.accept(ItemRegistry.IRON_WIRE_CUTTERS.get());
                        output.accept(ItemRegistry.GOLDEN_WIRE_CUTTERS.get());
                        output.accept(ItemRegistry.COPPER_WIRE_CUTTERS.get());
                        output.accept(ItemRegistry.TIN_WIRE_CUTTERS.get());
                        output.accept(ItemRegistry.BRONZE_WIRE_CUTTERS.get());
                        output.accept(ItemRegistry.CAST_IRON_WIRE_CUTTERS.get());
                        output.accept(ItemRegistry.STEEL_WIRE_CUTTERS.get());
                        // Screwdrivers
                        output.accept(ItemRegistry.IRON_SCREWDRIVER.get());
                        output.accept(ItemRegistry.GOLDEN_SCREWDRIVER.get());
                        output.accept(ItemRegistry.COPPER_SCREWDRIVER.get());
                        output.accept(ItemRegistry.TIN_SCREWDRIVER.get());
                        output.accept(ItemRegistry.BRONZE_SCREWDRIVER.get());
                        output.accept(ItemRegistry.CAST_IRON_SCREWDRIVER.get());
                        output.accept(ItemRegistry.STEEL_SCREWDRIVER.get());
                        // Saws
                        output.accept(ItemRegistry.IRON_SAW.get());
                        output.accept(ItemRegistry.GOLDEN_SAW.get());
                        output.accept(ItemRegistry.COPPER_SAW.get());
                        output.accept(ItemRegistry.TIN_SAW.get());
                        output.accept(ItemRegistry.BRONZE_SAW.get());
                        output.accept(ItemRegistry.CAST_IRON_SAW.get());
                        output.accept(ItemRegistry.STEEL_SAW.get());
                        // Mortars
                        output.accept(ItemRegistry.FLINT_MORTAR.get());
                        output.accept(ItemRegistry.IRON_MORTAR.get());
                        output.accept(ItemRegistry.GOLDEN_MORTAR.get());
                        output.accept(ItemRegistry.COPPER_MORTAR.get());
                        output.accept(ItemRegistry.DIAMOND_MORTAR.get());
                        output.accept(ItemRegistry.TIN_MORTAR.get());
                        output.accept(ItemRegistry.TELLURIUM_MORTAR.get());
                        output.accept(ItemRegistry.VULCANITE_MORTAR.get());
                        output.accept(ItemRegistry.BRONZE_MORTAR.get());
                        output.accept(ItemRegistry.CAST_IRON_MORTAR.get());
                        output.accept(ItemRegistry.STEEL_MORTAR.get());
                        // Mallet
                        output.accept(ItemRegistry.WOODEN_MALLET.get());
                        // Knife
                        output.accept(ItemRegistry.KNIFE.get());
                    })
                    .build());

    @SuppressWarnings("unused")
    public static final RegistryObject<CreativeModeTab> MECH_MATERIALS_TAB = CREATIVE_MODE_TABS.register("materials_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ItemRegistry.VULCANITE_INGOT.get()))
                    .title(Component.translatable("item_group.mech.materials"))
                    .displayItems((params, output) -> {
                        // Ingots
                        output.accept(ItemRegistry.TIN_INGOT.get());
                        output.accept(ItemRegistry.TELLURIUM.get());
                        output.accept(ItemRegistry.VULCANITE_INGOT.get());
                        output.accept(ItemRegistry.BRONZE_INGOT.get());
                        output.accept(ItemRegistry.CAST_IRON_INGOT.get());
                        output.accept(ItemRegistry.STEEL_INGOT.get());
                        output.accept(ItemRegistry.RUBBER.get());
                        // Blocks
                        output.accept(BlockRegistry.TIN_BLOCK.get());
                        output.accept(BlockRegistry.TELLURIUM_BLOCK.get());
                        output.accept(BlockRegistry.VULCANITE_BLOCK.get());
                        output.accept(BlockRegistry.BRONZE_BLOCK.get());
                        output.accept(BlockRegistry.CAST_IRON_BLOCK.get());
                        output.accept(BlockRegistry.STEEL_BLOCK.get());
                        // Raw ores
                        output.accept(ItemRegistry.RAW_TIN.get());
                        output.accept(ItemRegistry.RAW_TELLURIUM.get());
                        output.accept(ItemRegistry.SULFUR.get());
                        // Dusts
                        output.accept(ItemRegistry.IRON_DUST.get());
                        output.accept(ItemRegistry.GOLDEN_DUST.get());
                        output.accept(ItemRegistry.COPPER_DUST.get());
                        output.accept(ItemRegistry.TIN_DUST.get());
                        output.accept(ItemRegistry.TELLURIUM_DUST.get());
                        output.accept(ItemRegistry.VULCANITE_DUST.get());
                        output.accept(ItemRegistry.BRONZE_DUST.get());
                        output.accept(ItemRegistry.CAST_IRON_DUST.get());
                        output.accept(ItemRegistry.STEEL_DUST.get());
                        output.accept(ItemRegistry.COAL_DUST.get());
                        output.accept(ItemRegistry.SODIUM.get());
                        // Plates
                        output.accept(ItemRegistry.IRON_PLATE.get());
                        output.accept(ItemRegistry.GOLDEN_PLATE.get());
                        output.accept(ItemRegistry.COPPER_PLATE.get());
                        output.accept(ItemRegistry.TIN_PLATE.get());
                        output.accept(ItemRegistry.BRONZE_PLATE.get());
                        output.accept(ItemRegistry.CAST_IRON_PLATE.get());
                        output.accept(ItemRegistry.STEEL_PLATE.get());
                        output.accept(ItemRegistry.WOODEN_PLATE.get());
                        output.accept(ItemRegistry.CERAMIC_PLATE.get());
                        // Rods
                        output.accept(ItemRegistry.IRON_ROD.get());
                        output.accept(ItemRegistry.GOLDEN_ROD.get());
                        output.accept(ItemRegistry.COPPER_ROD.get());
                        output.accept(ItemRegistry.TIN_ROD.get());
                        output.accept(ItemRegistry.BRONZE_ROD.get());
                        output.accept(ItemRegistry.CAST_IRON_ROD.get());
                        output.accept(ItemRegistry.STEEL_ROD.get());
                        output.accept(ItemRegistry.QUICKLIME_ROD.get());
                        // Rings
                        output.accept(ItemRegistry.WOODEN_RING.get());
                        output.accept(ItemRegistry.IRON_RING.get());
                        output.accept(ItemRegistry.GOLDEN_RING.get());
                        output.accept(ItemRegistry.COPPER_RING.get());
                        output.accept(ItemRegistry.TIN_RING.get());
                        output.accept(ItemRegistry.BRONZE_RING.get());
                        output.accept(ItemRegistry.CAST_IRON_RING.get());
                        output.accept(ItemRegistry.STEEL_RING.get());
                        // Springs
                        output.accept(ItemRegistry.IRON_SPRING.get());
                        output.accept(ItemRegistry.GOLDEN_SPRING.get());
                        output.accept(ItemRegistry.COPPER_SPRING.get());
                        output.accept(ItemRegistry.TIN_SPRING.get());
                        output.accept(ItemRegistry.BRONZE_SPRING.get());
                        output.accept(ItemRegistry.CAST_IRON_SPRING.get());
                        output.accept(ItemRegistry.STEEL_SPRING.get());
                        // Large springs
                        output.accept(ItemRegistry.IRON_LARGE_SPRING.get());
                        output.accept(ItemRegistry.GOLDEN_LARGE_SPRING.get());
                        output.accept(ItemRegistry.COPPER_LARGE_SPRING.get());
                        output.accept(ItemRegistry.TIN_LARGE_SPRING.get());
                        output.accept(ItemRegistry.BRONZE_LARGE_SPRING.get());
                        output.accept(ItemRegistry.CAST_IRON_LARGE_SPRING.get());
                        output.accept(ItemRegistry.STEEL_LARGE_SPRING.get());
                        // Screws
                        output.accept(ItemRegistry.IRON_SCREW.get());
                        output.accept(ItemRegistry.GOLDEN_SCREW.get());
                        output.accept(ItemRegistry.COPPER_SCREW.get());
                        output.accept(ItemRegistry.TIN_SCREW.get());
                        output.accept(ItemRegistry.BRONZE_SCREW.get());
                        output.accept(ItemRegistry.CAST_IRON_SCREW.get());
                        output.accept(ItemRegistry.STEEL_SCREW.get());
                        // Saw blades
                        output.accept(ItemRegistry.IRON_SAW_BLADE.get());
                        output.accept(ItemRegistry.GOLDEN_SAW_BLADE.get());
                        output.accept(ItemRegistry.COPPER_SAW_BLADE.get());
                        output.accept(ItemRegistry.TIN_SAW_BLADE.get());
                        output.accept(ItemRegistry.BRONZE_SAW_BLADE.get());
                        output.accept(ItemRegistry.CAST_IRON_SAW_BLADE.get());
                        output.accept(ItemRegistry.STEEL_SAW_BLADE.get());
                        // Circular saw blades
                        output.accept(ItemRegistry.IRON_CIRCULAR_SAW_BLADE.get());
                        output.accept(ItemRegistry.GOLDEN_CIRCULAR_SAW_BLADE.get());
                        output.accept(ItemRegistry.COPPER_CIRCULAR_SAW_BLADE.get());
                        output.accept(ItemRegistry.TIN_CIRCULAR_SAW_BLADE.get());
                        output.accept(ItemRegistry.BRONZE_CIRCULAR_SAW_BLADE.get());
                        output.accept(ItemRegistry.CAST_IRON_CIRCULAR_SAW_BLADE.get());
                        output.accept(ItemRegistry.STEEL_CIRCULAR_SAW_BLADE.get());
                        // Gears
                        output.accept(ItemRegistry.IRON_GEAR.get());
                        // Meshes
                        output.accept(ItemRegistry.IRON_MESH.get());
                        // Coils
                        output.accept(ItemRegistry.COPPER_COIL.get());
                        // Filaments
                        output.accept(ItemRegistry.COAL_FILAMENT.get());
                    })
                    .build());

    @SuppressWarnings("unused")
    public static final RegistryObject<CreativeModeTab> MECH_INDUSTRIAL_TAB = CREATIVE_MODE_TABS.register("industrial_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.CEMENT.source.get(), 1000)))
                    .title(Component.translatable("item_group.mech.industrial"))
                    .displayItems((params, output) -> {
                        output.accept(ItemRegistry.LIMESTONE_CHUNK.get());
                        output.accept(ItemRegistry.QUICKLIME.get());
                        output.accept(ItemRegistry.SLAKED_LIME.get());
                        output.accept(ItemRegistry.CLINKER.get());
                        output.accept(ItemRegistry.GYPSUM_POWDER.get());
                        output.accept(ItemRegistry.RAW_CLINKER.get());
                        output.accept(FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.CEMENT.source.get(), 1000)).getItem());
                        output.accept(ItemRegistry.CRUSHED_STONE.get());
                        output.accept(ItemRegistry.SAND_DUST.get());
                        output.accept(FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.CONCRETE.source.get(), 1000)).getItem());
                        output.accept(BlockRegistry.CEMENT_BLOCK.get());
                        output.accept(BlockRegistry.CONCRETE_BLOCK.get());
                        output.accept(ItemRegistry.REDUCTOR.get());
                        output.accept(ItemRegistry.WHEEL_ARC_WITH_STICK.get());
                        output.accept(ItemRegistry.WHEEL_ARC_WITHOUT_STICK.get());
                        output.accept(FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.RAPESEED_OIL.source.get(), 1000)).getItem());
                        output.accept(BlockRegistry.PIPE_INSULATOR.get());
                        output.accept(ItemRegistry.COKE.get());
                        output.accept(FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.CREOSOTE.source.get(), 1000)).getItem());
                        output.accept(ItemRegistry.SLAG.get());
                        output.accept(FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.LATEX.source.get(), 1000)).getItem());
                        output.accept(ItemRegistry.STATOR.get());
                        output.accept(ItemRegistry.ROTOR.get());
                        output.accept(ItemRegistry.ELECTRIC_MOTOR.get());
                        output.accept(ItemRegistry.RESISTOR.get());
                        output.accept(ItemRegistry.CAPACITOR.get());
                        output.accept(ItemRegistry.RAW_PRINTED_CIRCUIT_BOARD.get());
                        output.accept(ItemRegistry.PRINTED_CIRCUIT_BOARD.get());
                        output.accept(ItemRegistry.VACUUM_TUBE.get());
                        output.accept(ItemRegistry.BASIC_CIRCUIT.get());
                        output.accept(FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.DISTILLED_WATER.source.get(), 1000)).getItem());
                        output.accept(FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.HYDROGEN_PEROXIDE.source.get(), 1000)).getItem());
                        output.accept(FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.OXYGEN.source.get(), 1000)).getItem());
                        output.accept(FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.HYDROGEN.source.get(), 1000)).getItem());
                        output.accept(FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.CHLORINE.source.get(), 1000)).getItem());
                        output.accept(FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.SULFURIC_ACID.source.get(), 1000)).getItem());
                        output.accept(FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.SULFUROUS_ACID.source.get(), 1000)).getItem());
                    })
                    .build());

    @SuppressWarnings("unused")
    public static final RegistryObject<CreativeModeTab> MECH_ORES_TAB = CREATIVE_MODE_TABS.register("ores_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(BlockRegistry.TELLURIUM_ORE.get()))
                    .title(Component.translatable("item_group.mech.ores"))
                    .displayItems((params, output) -> {
                        output.accept(BlockRegistry.TIN_ORE.get());
                        output.accept(BlockRegistry.DEEPSLATE_TIN_ORE.get());
                        output.accept(BlockRegistry.TELLURIUM_ORE.get());
                        output.accept(BlockRegistry.DEEPSLATE_TELLURIUM_ORE.get());
                        output.accept(BlockRegistry.SULFUR_ORE.get());
                    })
                    .build());

    @SuppressWarnings("unused")
    public static final RegistryObject<CreativeModeTab> MECH_NATURE = CREATIVE_MODE_TABS.register("nature_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ItemRegistry.RAPESEED.get()))
                    .title(Component.translatable("item_group.mech.nature"))
                    .displayItems((params, output) -> {
                        output.accept(BlockRegistry.GYPSUM_STONE.get());
                        output.accept(BlockRegistry.LIMESTONE.get());
                        output.accept(ItemRegistry.RAPESEED.get());
                        output.accept(ItemRegistry.RAPESEED_CLEANED.get());
                        output.accept(BlockRegistry.CASTILLA_PLANKS.get());
                        output.accept(BlockRegistry.CASTILLA_LEAVES.get());
                        output.accept(BlockRegistry.CASTILLA_SAPLING.get());
                        output.accept(BlockRegistry.CASTILLA_LOG.get());
                    })
                    .build());
}
