package com.sinthspark.mech.registration;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.blocks.*;
import com.sinthspark.mech.blocks.PipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MECH.MOD_ID);

    public static final RegistryObject<Block> BRONZE_BLOCK =
            BLOCKS.register("bronze_block",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(5f, 6f)
                            .requiresCorrectToolForDrops()
                    ));

    public static final RegistryObject<Block> TELLURIUM_BLOCK = BLOCKS.register("tellurium_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .requiresCorrectToolForDrops()) {
                @Override
                public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
                    ItemStack stack = player.getMainHandItem();
                    return stack.getItem() instanceof PickaxeItem pickaxe &&
                            pickaxe.getTier().getLevel() >= Tiers.IRON.getLevel();
                }
            });

    public static final RegistryObject<Block> TIN_BLOCK =
            BLOCKS.register("tin_block",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(5f, 6f)
                            .requiresCorrectToolForDrops()
                    ));

    public static final RegistryObject<Block> VULCANITE_BLOCK =
            BLOCKS.register("vulcanite_block",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(5f, 6f)
                            .requiresCorrectToolForDrops()
                    ));

    public static final RegistryObject<Block> BRONZE_BRICK_CASING =
            BLOCKS.register("bronze_brick_casing",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(5f, 6f)
                            .requiresCorrectToolForDrops()
                    ));

    public static final RegistryObject<Block> VULCANITE_GEAR =
            BLOCKS.register("vulcanite_gear",
                    () -> new VulcaniteGearBlock(
                            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                                    .mapColor(MapColor.METAL)
                                    .strength(0.1f, 0.1f)
                                    .requiresCorrectToolForDrops()
                                    .isRedstoneConductor((state, level, pos) -> true)  // блок считается твёрдым
                    ));

    public static final RegistryObject<Block> MANUAL_KINETIC_GENERATOR =
            BLOCKS.register("manual_kinetic_generator",
                    () -> new ManualKineticGeneratorBlock(
                            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                                    .mapColor(MapColor.METAL)
                                    .strength(0.1f, 0.1f)
                                    .requiresCorrectToolForDrops()
                    ));

    public static final RegistryObject<Block> WATER_WHEEL =
            BLOCKS.register("water_wheel",
                    () -> new WaterWheelBlock(
                            BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                                    .mapColor(MapColor.COLOR_BROWN)
                                    .strength(0.1f, 0.1f)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion()
                    ));

    public static final RegistryObject<Block> PRESS =
            BLOCKS.register("press",
                    () -> new PressBlock());

    public static final RegistryObject<Block> QUERN =
            BLOCKS.register("quern",
                    () -> new QuernBlock());

    public static final RegistryObject<Block> SAWMILL =
            BLOCKS.register("sawmill",
                    () -> new SawmillBlock());

    public static final RegistryObject<Block> DEEPSLATE_TELLURIUM_ORE = BLOCKS.register("deepslate_tellurium_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.DEEPSLATE)) {
                @Override
                public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
                    ItemStack stack = player.getMainHandItem();
                    if (stack.getItem() instanceof PickaxeItem pickaxe) {
                        return pickaxe.getTier().getLevel() >= Tiers.IRON.getLevel();
                    }
                    return false;
                }
            });

    public static final RegistryObject<Block> TELLURIUM_ORE = BLOCKS.register("tellurium_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.STONE)) {
                @Override
                public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
                    ItemStack stack = player.getMainHandItem();
                    if (stack.getItem() instanceof PickaxeItem pickaxe) {
                        return pickaxe.getTier().getLevel() >= Tiers.IRON.getLevel();
                    }
                    return false;
                }
            });

    public static final RegistryObject<Block> DEEPSLATE_TIN_ORE = BLOCKS.register("deepslate_tin_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 3.0F)
                    .sound(SoundType.DEEPSLATE)) {
                @Override
                public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
                    ItemStack stack = player.getMainHandItem();
                    if (stack.getItem() instanceof PickaxeItem pickaxe) {
                        return pickaxe.getTier().getLevel() >= Tiers.STONE.getLevel();
                    }
                    return false;
                }
            });

    public static final RegistryObject<Block> TIN_ORE = BLOCKS.register("tin_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 3.0F)
                    .sound(SoundType.DEEPSLATE)) {
                @Override
                public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
                    ItemStack stack = player.getMainHandItem();
                    if (stack.getItem() instanceof PickaxeItem pickaxe) {
                        return pickaxe.getTier().getLevel() >= Tiers.STONE.getLevel();
                    }
                    return false;
                }
            });

    public static final RegistryObject<Block> GYPSUM_STONE =
            BLOCKS.register("gypsum_stone",
                    () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.SAND)
                                    .requiresCorrectToolForDrops()
                                    .strength(1.5F, 6.0F)
                                    .sound(SoundType.STONE)
                    ));

    public static final RegistryObject<Block> LIMESTONE =
            BLOCKS.register("limestone",
                    () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.SAND)
                                    .requiresCorrectToolForDrops()
                                    .strength(1.5F, 6.0F)
                                    .sound(SoundType.STONE)
                    ));

    public static final RegistryObject<Block> CEMENT_BLOCK =
            BLOCKS.register("cement_block",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .strength(5.0f, 6.0f)
                            .requiresCorrectToolForDrops()
                            .mapColor(MapColor.TERRACOTTA_WHITE)
                    ));

    public static final RegistryObject<Block> CONCRETE_BLOCK =
            BLOCKS.register("concrete_block",
                    () -> new Block(
                            BlockBehaviour.Properties.of()
                            .strength(5.0f, 6.0f)
                            .requiresCorrectToolForDrops()
                            .mapColor(MapColor.STONE)));

    public static final RegistryObject<Block> VULCANITE_BEARING =
            BLOCKS.register("vulcanite_bearing",
                    () -> new VulcaniteBearingBlock(
                            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                                    .strength(0.1f, 0.1f)
                                    .requiresCorrectToolForDrops()
                                    .mapColor(MapColor.METAL)
                    ));

    public static final RegistryObject<Block> OIL_PRESS =
            BLOCKS.register("oil_press",
                    () -> new OilPressBlock(
                            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    ));

    public static final RegistryObject<Block> RAPESEED_CROP = BLOCKS.register("rapeseed_crop",
            () -> new CropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT)) {
                @Override
                protected @NotNull Item getBaseSeedId() {
                    return ItemRegistry.RAPESEED.get(); // Вызывается позже, когда предмет уже зарегистрирован
                }
            });
    public static final RegistryObject<Block> OIL_PRESS_DUMMY = BLOCKS.register("oil_press_dummy",
            () -> new OilPressDummyBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .noOcclusion()
                    .instabreak()
                    .noLootTable()));
    
    public static final RegistryObject<Block> FIREBOX = BLOCKS.register("firebox",
            () -> new FireboxBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .mapColor(MapColor.METAL)
            ));

    public static final RegistryObject<Block> BOILER_TANK = BLOCKS.register("boiler_tank",
            () -> new BoilerTankBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .mapColor(MapColor.METAL)
            ));

    public static final RegistryObject<Block> TIN_PIPE = BLOCKS.register("tin_pipe",
            () -> new PipeBlock(BlockBehaviour.Properties.of().noOcclusion(), 4800, 20, 2.0f, 2));

    public static final RegistryObject<Block> COPPER_PIPE = BLOCKS.register("copper_pipe",
            () -> new PipeBlock(BlockBehaviour.Properties.of().noOcclusion(), 4800, 30, 1.5f, 3));

    public static final RegistryObject<Block> BRONZE_PIPE = BLOCKS.register("bronze_pipe",
            () -> new PipeBlock(BlockBehaviour.Properties.of().noOcclusion(), 4800, 60, 1.5f, 4));

    public static final RegistryObject<Block> IRON_PIPE = BLOCKS.register("iron_pipe",
            () -> new PipeBlock(BlockBehaviour.Properties.of().noOcclusion(), 4800, 40, 0.5f, 6));

    public static final RegistryObject<Block> GOLDEN_PIPE = BLOCKS.register("golden_pipe",
            () -> new PipeBlock(BlockBehaviour.Properties.of().noOcclusion(), 4800, 100, 0.2f, 2));

    public static final RegistryObject<Block> CAST_IRON_PIPE = BLOCKS.register("cast_iron_pipe",
            () -> new PipeBlock(BlockBehaviour.Properties.of().noOcclusion(), 4800, 30, 0.8f, 8));

    public static final RegistryObject<Block> STEEL_PIPE = BLOCKS.register("steel_pipe",
            () -> new PipeBlock(BlockBehaviour.Properties.of().noOcclusion(), 4800, 60, 1.0f, 10));

    public static final RegistryObject<Block> PIPE_INSULATOR = BLOCKS.register("pipe_insulator",
            () -> new PipeInsulatorBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .noCollission()
                    .instabreak()
                    .strength(0.3F, 0.03f)
                    .mapColor(MapColor.COLOR_BLACK)
            ));

    public static final RegistryObject<Block> STEAM_FURNACE = BLOCKS.register("steam_furnace",
            () -> new SteamFurnaceBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> STEAM_ALLOY_FURNACE = BLOCKS.register("steam_alloy_furnace",
            () -> new SteamAlloyFurnaceBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> STEAM_GRINDER = BLOCKS.register("steam_grinder",
            () -> new SteamGrinderBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> STEAM_COMPRESSOR = BLOCKS.register("steam_compressor",
            () -> new SteamCompressorBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> STEAM_PRESS = BLOCKS.register("steam_press",
            () -> new SteamPressBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> STEAM_SAWMILL = BLOCKS.register("steam_sawmill",
            () -> new SteamSawmillBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> STEAM_EXTRACTOR = BLOCKS.register("steam_extractor",
            () -> new SteamExtractorBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> COKE_OVEN_BRICKS = BLOCKS.register("coke_oven_bricks",
            () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(5f, 6f)
                            .requiresCorrectToolForDrops()
            ));

    public static final RegistryObject<Block> COKE_OVEN_CONTROLLER = BLOCKS.register("coke_oven_controller",
            () -> new CokeOvenControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5f, 6f)
                    .requiresCorrectToolForDrops()
            ));

    public static final RegistryObject<Block> BLAST_FURNACE_BRICKS = BLOCKS.register("blast_furnace_bricks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5f, 6f)
                    .requiresCorrectToolForDrops()
            ));

    public static final RegistryObject<Block> BLAST_FURNACE_CONTROLLER = BLOCKS.register("blast_furnace_controller",
            () -> new BlastFurnaceControllerBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> CAST_IRON_BLOCK = BLOCKS.register("cast_iron_block",
            () -> new Block(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> CAST_IRON_BRICK_CASING = BLOCKS.register("cast_iron_brick_casing",
            () -> new Block(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> UPGRADED_FIREBOX = BLOCKS.register("upgraded_firebox",
            () -> new UpgradedFireboxBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .mapColor(MapColor.METAL)
            ));

    public static final RegistryObject<Block> UPGRADED_BOILER_TANK = BLOCKS.register("upgraded_boiler_tank",
            () -> new UpgradedBoilerTankBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .mapColor(MapColor.METAL)
            ));

    public static final RegistryObject<Block> UPGRADED_STEAM_FURNACE = BLOCKS.register("upgraded_steam_furnace",
            () -> new UpgradedSteamFurnaceBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> UPGRADED_STEAM_ALLOY_FURNACE = BLOCKS.register("upgraded_steam_alloy_furnace",
            () -> new UpgradedSteamAlloyFurnaceBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> UPGRADED_STEAM_GRINDER = BLOCKS.register("upgraded_steam_grinder",
            () -> new UpgradedSteamGrinderBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> UPGRADED_STEAM_COMPRESSOR = BLOCKS.register("upgraded_steam_compressor",
            () -> new UpgradedSteamCompressorBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> UPGRADED_STEAM_PRESS = BLOCKS.register("upgraded_steam_press",
            () -> new UpgradedSteamPressBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> UPGRADED_STEAM_SAWMILL = BLOCKS.register("upgraded_steam_sawmill",
            () -> new UpgradedSteamSawmillBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> UPGRADED_STEAM_EXTRACTOR = BLOCKS.register("upgraded_steam_extractor",
            () -> new UpgradedSteamExtractorBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> OPEN_HEARTH_BRICKS = BLOCKS.register("open_hearth_bricks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .mapColor(MapColor.METAL)
            ));

    public static final RegistryObject<Block> OPEN_HEARTH_CONTROLLER = BLOCKS.register("open_hearth_controller",
            () -> new OpenHearthControllerBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> OPEN_HEARTH_FLUID_HATCH = BLOCKS.register("open_hearth_fluid_hatch",
            () -> new OpenHearthFluidHatchBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> STEEL_BLOCK = BLOCKS.register("steel_block",
            () -> new Block(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ADVANCED_FIREBOX = BLOCKS.register("advanced_firebox",
            () -> new AdvancedFireboxBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .mapColor(MapColor.METAL)
            ));

    public static final RegistryObject<Block> ADVANCED_BOILER_TANK = BLOCKS.register("advanced_boiler_tank",
            () -> new AdvancedBoilerTankBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .mapColor(MapColor.METAL)
            ));

    public static final RegistryObject<Block> ADVANCED_STEAM_FURNACE = BLOCKS.register("advanced_steam_furnace",
            () -> new AdvancedSteamFurnaceBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ADVANCED_STEAM_ALLOY_FURNACE = BLOCKS.register("advanced_steam_alloy_furnace",
            () -> new AdvancedSteamAlloyFurnaceBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ADVANCED_STEAM_GRINDER = BLOCKS.register("advanced_steam_grinder",
            () -> new AdvancedSteamGrinderBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ADVANCED_STEAM_COMPRESSOR = BLOCKS.register("advanced_steam_compressor",
            () -> new AdvancedSteamCompressorBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ADVANCED_STEAM_PRESS = BLOCKS.register("advanced_steam_press",
            () -> new AdvancedSteamPressBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ADVANCED_STEAM_SAWMILL = BLOCKS.register("advanced_steam_sawmill",
            () -> new AdvancedSteamSawmillBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ADVANCED_STEAM_EXTRACTOR = BLOCKS.register("advanced_steam_extractor",
            () -> new AdvancedSteamExtractorBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> STEEL_BRICK_CASING = BLOCKS.register("steel_brick_casing",
            () -> new Block(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> SULFUR_ORE = BLOCKS.register("sulfur_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NETHER)
                    .strength(1.5F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERRACK)) {
                @Override
                public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
                    ItemStack stack = player.getMainHandItem();
                    if (stack.getItem() instanceof PickaxeItem pickaxe) {
                        return pickaxe.getTier().getLevel() >= Tiers.STONE.getLevel();
                    }
                    return false;
                }
            });

    public static final RegistryObject<Block> CASTILLA_SAPLING = BLOCKS.register("castilla_sapling",
            () -> new SaplingBlock(new AbstractTreeGrower() {
                @Override
                protected @NotNull ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(@NotNull RandomSource random, boolean hasFlowers) {
                    return ResourceKey.create(Registries.CONFIGURED_FEATURE,
                            new ResourceLocation("mech", "castilla_tree"));
                }
            }, BlockBehaviour.Properties.of()
                    .strength(0.0F, 0.0f)
                    .sound(SoundType.GRASS)
                    .noCollission()
                    .noOcclusion()
                    .randomTicks()
                    .mapColor(MapColor.PLANT))
    );

    public static final RegistryObject<Block> CASTILLA_LOG = BLOCKS.register("castilla_log",
            () -> new CastillaLogBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F, 2.0f)
                    .sound(SoundType.WOOD)
                    .requiresCorrectToolForDrops()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
            ));

    public static final RegistryObject<Block> CASTILLA_LEAVES = BLOCKS.register("castilla_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.of()
                    .strength(0.2F, 0.2f)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .isViewBlocking((s, l, p) -> false)
                    .randomTicks()
                    .mapColor(MapColor.GRASS)
            ));

    public static final RegistryObject<Block> CASTILLA_PLANKS = BLOCKS.register("castilla_planks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(2.0F, 2.0f)
                    .sound(SoundType.WOOD)
                    .requiresCorrectToolForDrops()
                    .mapColor(MapColor.WOOD)
            ));

    public static final RegistryObject<Block> STEEL_MACHINE_CASING = BLOCKS.register("steel_machine_casing",
            () -> new Block(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> TIN_WIRE = BLOCKS.register("tin_wire",
            () -> new WireBlock(BlockBehaviour.Properties.of(), 32, 1, false));

    public static final RegistryObject<Block> INSULATED_TIN_WIRE = BLOCKS.register("insulated_tin_wire",
            () -> new WireBlock(BlockBehaviour.Properties.of(), 32, 1, true));

    public static final RegistryObject<Block> COPPER_WIRE = BLOCKS.register("copper_wire",
            () -> new WireBlock(BlockBehaviour.Properties.of(), 64, 1, false));

    public static final RegistryObject<Block> STEAM_TURBINE = BLOCKS.register("steam_turbine",
            () -> new SteamTurbineBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ELECTRIC_GENERATOR = BLOCKS.register("electric_generator",
            () -> new ElectricGeneratorBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ELECTRIC_FURNACE = BLOCKS.register("electric_furnace",
            () -> new ElectricFurnaceBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ELECTRIC_ALLOY_FURNACE = BLOCKS.register("electric_alloy_furnace",
            () -> new ElectricAlloyFurnaceBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ELECTRIC_GRINDER = BLOCKS.register("electric_grinder",
            () -> new ElectricGrinderBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ELECTRIC_COMPRESSOR = BLOCKS.register("electric_compressor",
            () -> new ElectricCompressorBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ELECTRIC_EXTRACTOR = BLOCKS.register("electric_extractor",
            () -> new ElectricExtractorBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ELECTRIC_BENDER = BLOCKS.register("electric_bender",
            () -> new ElectricBenderBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ELECTRIC_CUTTER = BLOCKS.register("electric_cutter",
            () -> new ElectricCutterBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ELECTRIC_WIREMILL = BLOCKS.register("electric_wiremill",
            () -> new ElectricWiremillBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ELECTRIC_LATHE = BLOCKS.register("electric_lathe",
            () -> new ElectricLatheBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> DISTILLER = BLOCKS.register("distiller",
            () -> new DistillerBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> CONDENSER = BLOCKS.register("condenser",
            () -> new CondenserBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> ELECTROLYZER = BLOCKS.register("electrolyzer",
            () -> new ElectrolyzerBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<Block> CHEMICAL_REACTOR = BLOCKS.register("chemical_reactor",
            () -> new ChemicalReactorBlock(BlockBehaviour.Properties.of()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}