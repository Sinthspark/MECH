package com.sinthspark.mech.registration;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.base.FluidRegistryContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidRegistry {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MECH.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, MECH.MOD_ID);

    //=====================================Пар==========================================================================

    public static final FluidRegistryContainer DRY_STEAM = new FluidRegistryContainer(
            "dry_steam",
            FluidType.Properties.create()
                    .density(-500)
                    .viscosity(1)
                    .temperature(450),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "dry_steam")
                            .tint(0xFFE8E8E8)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    public static final FluidRegistryContainer STEAM = new FluidRegistryContainer(
            "steam",
            FluidType.Properties.create()
                    .density(-1000)
                    .viscosity(1)
                    .temperature(373),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "steam")
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    public static final FluidRegistryContainer SULFUR_DIOXIDE = new FluidRegistryContainer(
            "sulfur_dioxide",
            FluidType.Properties.create()
                    .density(-600)
                    .viscosity(2)
                    .temperature(600),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "sulfur_dioxide")
                            .tint(0xFFFFF200)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    public static final FluidRegistryContainer SULFUR_STEAM = new FluidRegistryContainer(
            "sulfur_steam",
            FluidType.Properties.create()
                    .density(-600)
                    .viscosity(2)
                    .temperature(600),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "sulfur_steam")
                            .tint(0xFFFFF200)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    //=================================================Жидкости=========================================================

    //Химические элементы
    public static final FluidRegistryContainer CHLORINE = new FluidRegistryContainer(
            "chlorine",
            FluidType.Properties.create()
                    .density(-1200)
                    .viscosity(1)
                    .temperature(240)
                    .canSwim(false)
                    .canDrown(true)
                    .canPushEntity(false)
                    .supportsBoating(false),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "chlorine")
                            .tint(0xFF88FF88)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    public static final FluidRegistryContainer HYDROGEN = new FluidRegistryContainer(
            "hydrogen",
            FluidType.Properties.create()
                    .density(-900)
                    .viscosity(0)
                    .temperature(20)
                    .canSwim(false)
                    .canDrown(true)
                    .canPushEntity(false)
                    .supportsBoating(false),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "hydrogen")
                            .tint(0x88CCFFFF)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    public static final FluidRegistryContainer OXYGEN = new FluidRegistryContainer(
            "oxygen",
            FluidType.Properties.create()
                    .density(-700)
                    .viscosity(0)
                    .temperature(90)
                    .canSwim(false)
                    .canDrown(true)
                    .canPushEntity(false)
                    .supportsBoating(false),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "oxygen")
                            .tint(0x88DDFFFF)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    //Жидкости для химии
    public static final FluidRegistryContainer DISTILLED_WATER = new FluidRegistryContainer(
            "distilled_water",
            FluidType.Properties.create()
                    .density(1000)
                    .viscosity(1)
                    .temperature(300)
                    .canSwim(true)
                    .canDrown(true)
                    .canPushEntity(true)
                    .supportsBoating(true),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "distilled_water")
                            .tint(0xFFCCCCFF)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    public static final FluidRegistryContainer HYDROGEN_PEROXIDE = new FluidRegistryContainer(
            "hydrogen_peroxide",
            FluidType.Properties.create()
                    .density(1200)
                    .viscosity(1)
                    .temperature(300)
                    .canSwim(false)
                    .canDrown(true)
                    .canPushEntity(false)
                    .supportsBoating(false),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "hydrogen_peroxide")
                            .tint(0xFFCCFFFF)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    //Кислоты
    public static final FluidRegistryContainer SULFURIC_ACID = new FluidRegistryContainer(
            "sulfuric_acid",
            FluidType.Properties.create()
                    .density(1840)
                    .viscosity(25)
                    .temperature(300)
                    .canSwim(false)
                    .canDrown(true)
                    .canPushEntity(false)
                    .supportsBoating(false),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "sulfuric_acid")
                            .tint(0xFFDDDD88)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    public static final FluidRegistryContainer SULFUROUS_ACID = new FluidRegistryContainer(
            "sulfurous_acid",
            FluidType.Properties.create()
                    .density(1030)
                    .viscosity(2)
                    .temperature(300)
                    .canSwim(false)
                    .canDrown(true)
                    .canPushEntity(false)
                    .supportsBoating(false),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "sulfurous_acid")
                            .tint(0xFFCCCC88)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    //Топливо
    public static final FluidRegistryContainer CREOSOTE = new FluidRegistryContainer(
            "creosote",
            FluidType.Properties.create()
                    .density(1100)
                    .viscosity(80)
                    .temperature(300)
                    .canSwim(false)
                    .canDrown(true)
                    .canPushEntity(false)
                    .supportsBoating(false)
                    .lightLevel(0)
                    .motionScale(0.003),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "creosote")
                            .tint(0xFF3A2E1E)
                            .fogColor(0.2f, 0.1f, 0.05f)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    //Масло
    public static final FluidRegistryContainer RAPESEED_OIL = new FluidRegistryContainer(
            "rapeseed_oil",
            FluidType.Properties.create()
                    .density(920)
                    .viscosity(50)
                    .temperature(300)
                    .canSwim(false)
                    .canDrown(true)
                    .canPushEntity(false)
                    .supportsBoating(false)
                    .lightLevel(0)
                    .motionScale(0.004),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "rapeseed_oil")
                            .tint(0xFFD0BE69)
                            .fogColor(0.7f, 0.6f, 0.1f)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    //Латекс
    public static final FluidRegistryContainer LATEX = new FluidRegistryContainer(
            "latex",
            FluidType.Properties.create()
                    .density(1200)
                    .viscosity(1)
                    .temperature(300)
                    .canSwim(false)
                    .canDrown(true)
                    .canPushEntity(false)
                    .supportsBoating(false),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "latex")
                            .tint(0xFFCCFFFF)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            null
    );

    //==================================Затвердевающие жидкости=========================================================

    public static final FluidRegistryContainer CEMENT = new FluidRegistryContainer(
            "fluid_cement",
            FluidType.Properties.create()
                    .density(2000)
                    .viscosity(3000)
                    .temperature(300)
                    .canSwim(false)
                    .canDrown(true)
                    .canPushEntity(false)
                    .supportsBoating(false)
                    .lightLevel(0)
                    .motionScale(0.005),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "fluid_cement")
                            .tint(0xFFC8BC98)
                            .fogColor(0.8f, 0.7f, 0.5f)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WATER)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            BlockRegistry.CEMENT_BLOCK,
            600
    );

    public static final FluidRegistryContainer CONCRETE = new FluidRegistryContainer(
            "fluid_concrete",
            FluidType.Properties.create()
                    .density(2400)
                    .viscosity(4000)
                    .temperature(300)
                    .canSwim(false)
                    .canDrown(true)
                    .canPushEntity(false)
                    .supportsBoating(false)
                    .lightLevel(0)
                    .motionScale(0.003),
            () -> FluidRegistryContainer.createExtension(
                    new FluidRegistryContainer.ClientExtensions(MECH.MOD_ID, "fluid_concrete")
                            .tint(0xFF888888)
                            .fogColor(0.5f, 0.5f, 0.5f)
            ),
            null,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WATER)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .liquid(),
            new Item.Properties().stacksTo(1),
            BlockRegistry.CONCRETE_BLOCK,
            600
    );
}