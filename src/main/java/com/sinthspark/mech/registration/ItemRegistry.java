package com.sinthspark.mech.registration;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.items.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MECH.MOD_ID);

    // Киянки
    public static final RegistryObject<Item> WOODEN_MALLET =
            ITEMS.register("wooden_mallet",
                    () -> new MalletItem(59));

    //Молоты
    public static final RegistryObject<Item> BRONZE_HAMMER =
            ITEMS.register("bronze_hammer",
                    () -> new HammerItem(326));

    public static final RegistryObject<Item> COPPER_HAMMER =
            ITEMS.register("copper_hammer",
                    () -> new HammerItem(190));

    public static final RegistryObject<Item> DIAMOND_HAMMER =
            ITEMS.register("diamond_hammer",
                    () -> new HammerItem(1561));

    public static final RegistryObject<Item> GOLDEN_HAMMER =
            ITEMS.register("golden_hammer",
                    () -> new HammerItem(32));

    public static final RegistryObject<Item> IRON_HAMMER =
            ITEMS.register("iron_hammer",
                    () -> new HammerItem(250));

    public static final RegistryObject<Item> TELLURIUM_HAMMER =
            ITEMS.register("tellurium_hammer",
                    () -> new HammerItem(80));

    public static final RegistryObject<Item> TIN_HAMMER =
            ITEMS.register("tin_hammer",
                    () -> new HammerItem(136));

    public static final RegistryObject<Item> VULCANITE_HAMMER =
            ITEMS.register("vulcanite_hammer",
                    () -> new HammerItem(300));

    public static final RegistryObject<Item> CAST_IRON_HAMMER =
            ITEMS.register("cast_iron_hammer",
                    () -> new HammerItem(450));

    public static final RegistryObject<Item> STEEL_HAMMER =
            ITEMS.register("steel_hammer",
                    () -> new HammerItem(800));

    //Напильники
    public static final RegistryObject<Item> BRONZE_FILE =
            ITEMS.register("bronze_file",
                    () -> new FileItem(326));

    public static final RegistryObject<Item> COPPER_FILE =
            ITEMS.register("copper_file",
                    () -> new FileItem(190));

    public static final RegistryObject<Item> GOLDEN_FILE =
            ITEMS.register("golden_file",
                    () -> new FileItem(32));

    public static final RegistryObject<Item> IRON_FILE =
            ITEMS.register("iron_file",
                    () -> new FileItem(250));

    public static final RegistryObject<Item> TIN_FILE =
            ITEMS.register("tin_file",
                    () -> new FileItem(136));

    public static final RegistryObject<Item> CAST_IRON_FILE =
            ITEMS.register("cast_iron_file",
                    () -> new FileItem(450));

    public static final RegistryObject<Item> STEEL_FILE =
            ITEMS.register("steel_file",
                    () -> new FileItem(800));

    //Ступки
    public static final RegistryObject<Item> FLINT_MORTAR =
            ITEMS.register("flint_mortar",
                    () -> new MortarItem(16));

    public static final RegistryObject<Item> BRONZE_MORTAR =
            ITEMS.register("bronze_mortar",
                    () -> new MortarItem(326));

    public static final RegistryObject<Item> COPPER_MORTAR =
            ITEMS.register("copper_mortar",
                    () -> new MortarItem(190));

    public static final RegistryObject<Item> DIAMOND_MORTAR =
            ITEMS.register("diamond_mortar",
                    () -> new FileItem(1561));

    public static final RegistryObject<Item> GOLDEN_MORTAR =
            ITEMS.register("golden_mortar",
                    () -> new MortarItem(32));

    public static final RegistryObject<Item> IRON_MORTAR =
            ITEMS.register("iron_mortar",
                    () -> new MortarItem(250));

    public static final RegistryObject<Item> TELLURIUM_MORTAR =
            ITEMS.register("tellurium_mortar",
                    () -> new MortarItem(80));

    public static final RegistryObject<Item> TIN_MORTAR =
            ITEMS.register("tin_mortar",
                    () -> new MortarItem(136));

    public static final RegistryObject<Item> VULCANITE_MORTAR =
            ITEMS.register("vulcanite_mortar",
                    () -> new MortarItem(300));

    public static final RegistryObject<Item> CAST_IRON_MORTAR =
            ITEMS.register("cast_iron_mortar",
                    () -> new MortarItem(450));

    public static final RegistryObject<Item> STEEL_MORTAR =
            ITEMS.register("steel_mortar",
                    () -> new MortarItem(800));

    //Пилы
    public static final RegistryObject<Item> BRONZE_SAW =
            ITEMS.register("bronze_saw",
                    () -> new SawItem(326));

    public static final RegistryObject<Item> COPPER_SAW =
            ITEMS.register("copper_saw",
                    () -> new SawItem(190));

    public static final RegistryObject<Item> GOLDEN_SAW =
            ITEMS.register("golden_saw",
                    () -> new SawItem(32));

    public static final RegistryObject<Item> IRON_SAW =
            ITEMS.register("iron_saw",
                    () -> new SawItem(250));

    public static final RegistryObject<Item> TIN_SAW =
            ITEMS.register("tin_saw",
                    () -> new SawItem(136));

    public static final RegistryObject<Item> CAST_IRON_SAW =
            ITEMS.register("cast_iron_saw",
                    () -> new SawItem(450));

    public static final RegistryObject<Item> STEEL_SAW =
            ITEMS.register("steel_saw",
                    () -> new SawItem(800));

    //Отвертки
    public static final RegistryObject<Item> BRONZE_SCREWDRIVER =
            ITEMS.register("bronze_screwdriver",
                    () -> new ScrewdriverItem(326));

    public static final RegistryObject<Item> COPPER_SCREWDRIVER =
            ITEMS.register("copper_screwdriver",
                    () -> new ScrewdriverItem(190));

    public static final RegistryObject<Item> GOLDEN_SCREWDRIVER =
            ITEMS.register("golden_screwdriver",
                    () -> new ScrewdriverItem(32));

    public static final RegistryObject<Item> IRON_SCREWDRIVER =
            ITEMS.register("iron_screwdriver",
                    () -> new ScrewdriverItem(250));

    public static final RegistryObject<Item> TIN_SCREWDRIVER =
            ITEMS.register("tin_screwdriver",
                    () -> new ScrewdriverItem(136));

    public static final RegistryObject<Item> CAST_IRON_SCREWDRIVER =
            ITEMS.register("cast_iron_screwdriver",
                    () -> new ScrewdriverItem(450));

    public static final RegistryObject<Item> STEEL_SCREWDRIVER =
            ITEMS.register("steel_screwdriver",
                    () -> new ScrewdriverItem(800));

    //Кусачки
    public static final RegistryObject<Item> BRONZE_WIRE_CUTTERS =
            ITEMS.register("bronze_wire_cutters",
                    () -> new WireCuttersItem(326));

    public static final RegistryObject<Item> COPPER_WIRE_CUTTERS =
            ITEMS.register("copper_wire_cutters",
                    () -> new WireCuttersItem(190));

    public static final RegistryObject<Item> GOLDEN_WIRE_CUTTERS =
            ITEMS.register("golden_wire_cutters",
                    () -> new WireCuttersItem(32));

    public static final RegistryObject<Item> IRON_WIRE_CUTTERS =
            ITEMS.register("iron_wire_cutters",
                    () -> new WireCuttersItem(250));

    public static final RegistryObject<Item> TIN_WIRE_CUTTERS =
            ITEMS.register("tin_wire_cutters",
                    () -> new WireCuttersItem(136));

    public static final RegistryObject<Item> CAST_IRON_WIRE_CUTTERS =
            ITEMS.register("cast_iron_wire_cutters",
                    () -> new WireCuttersItem(450));

    public static final RegistryObject<Item> STEEL_WIRE_CUTTERS =
            ITEMS.register("steel_wire_cutters",
                    () -> new WireCuttersItem(800));

    //Ключи
    public static final RegistryObject<Item> BRONZE_WRENCH =
            ITEMS.register("bronze_wrench",
                    () -> new WrenchItem(326));

    public static final RegistryObject<Item> COPPER_WRENCH =
            ITEMS.register("copper_wrench",
                    () -> new WrenchItem(190));

    public static final RegistryObject<Item> GOLDEN_WRENCH =
            ITEMS.register("golden_wrench",
                    () -> new WrenchItem(32));

    public static final RegistryObject<Item> IRON_WRENCH =
            ITEMS.register("iron_wrench",
                    () -> new WrenchItem(250));

    public static final RegistryObject<Item> TIN_WRENCH =
            ITEMS.register("tin_wrench",
                    () -> new WrenchItem(136));

    public static final RegistryObject<Item> CAST_IRON_WRENCH =
            ITEMS.register("cast_iron_wrench",
                    () -> new WrenchItem(450));

    public static final RegistryObject<Item> STEEL_WRENCH =
            ITEMS.register("steel_wrench",
                    () -> new WrenchItem(800));

    public static final RegistryObject<Item> KNIFE = ITEMS.register("knife",
            () -> new KnifeItem());

    //Руда
    public static final RegistryObject<Item> RAW_TELLURIUM =
            ITEMS.register("raw_tellurium",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RAW_TIN =
            ITEMS.register("raw_tin",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SULFUR =
            ITEMS.register("sulfur",
                    () -> new Item(new Item.Properties()));

    //Слитки
    public static final RegistryObject<Item> BRONZE_INGOT =
            ITEMS.register("bronze_ingot",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TELLURIUM =
            ITEMS.register("tellurium",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TIN_INGOT =
            ITEMS.register("tin_ingot",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> VULCANITE_INGOT =
            ITEMS.register("vulcanite_ingot",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CAST_IRON_INGOT =
            ITEMS.register("cast_iron_ingot",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_INGOT =
            ITEMS.register("steel_ingot",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RUBBER =
            ITEMS.register("rubber",
                    () -> new Item(new Item.Properties()));

    //Пыли
    public static final RegistryObject<Item> BRONZE_DUST =
            ITEMS.register("bronze_dust",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_DUST =
            ITEMS.register("copper_dust",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GOLDEN_DUST =
            ITEMS.register("golden_dust",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_DUST =
            ITEMS.register("iron_dust",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SAND_DUST =
            ITEMS.register("sand_dust",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TELLURIUM_DUST =
            ITEMS.register("tellurium_dust",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TIN_DUST =
            ITEMS.register("tin_dust",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> VULCANITE_DUST =
            ITEMS.register("vulcanite_dust",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CAST_IRON_DUST =
            ITEMS.register("cast_iron_dust",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_DUST =
            ITEMS.register("steel_dust",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COAL_DUST =
            ITEMS.register("coal_dust",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SODIUM =
            ITEMS.register("sodium",
                    () -> new Item(new Item.Properties()));

    //Циркулярные лезвия пил
    public static final RegistryObject<Item> BRONZE_CIRCULAR_SAW_BLADE =
            ITEMS.register("bronze_circular_saw_blade",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_CIRCULAR_SAW_BLADE =
            ITEMS.register("copper_circular_saw_blade",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GOLDEN_CIRCULAR_SAW_BLADE =
            ITEMS.register("golden_circular_saw_blade",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_CIRCULAR_SAW_BLADE =
            ITEMS.register("iron_circular_saw_blade",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TIN_CIRCULAR_SAW_BLADE =
            ITEMS.register("tin_circular_saw_blade",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CAST_IRON_CIRCULAR_SAW_BLADE =
            ITEMS.register("cast_iron_circular_saw_blade",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_CIRCULAR_SAW_BLADE =
            ITEMS.register("steel_circular_saw_blade",
                    () -> new Item(new Item.Properties()));

    //Пластины
    public static final RegistryObject<Item> BRONZE_PLATE =
            ITEMS.register("bronze_plate",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_PLATE =
            ITEMS.register("copper_plate",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GOLDEN_PLATE =
            ITEMS.register("golden_plate",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_PLATE =
            ITEMS.register("iron_plate",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TIN_PLATE =
            ITEMS.register("tin_plate",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CAST_IRON_PLATE =
            ITEMS.register("cast_iron_plate",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_PLATE =
            ITEMS.register("steel_plate",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> WOODEN_PLATE =
            ITEMS.register("wooden_plate",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CERAMIC_PLATE =
            ITEMS.register("ceramic_plate",
                    () -> new Item(new Item.Properties()));

    //Стержни
    public static final RegistryObject<Item> BRONZE_ROD =
            ITEMS.register("bronze_rod",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_ROD =
            ITEMS.register("copper_rod",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GOLDEN_ROD =
            ITEMS.register("golden_rod",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_ROD =
            ITEMS.register("iron_rod",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TIN_ROD =
            ITEMS.register("tin_rod",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CAST_IRON_ROD =
            ITEMS.register("cast_iron_rod",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_ROD =
            ITEMS.register("steel_rod",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> QUICKLIME_ROD =
            ITEMS.register("quicklime_rod",
                    () -> new Item(new Item.Properties()));

    //Кольца
    public static final RegistryObject<Item> BRONZE_RING =
            ITEMS.register("bronze_ring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_RING =
            ITEMS.register("copper_ring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GOLDEN_RING =
            ITEMS.register("golden_ring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_RING =
            ITEMS.register("iron_ring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TIN_RING =
            ITEMS.register("tin_ring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CAST_IRON_RING =
            ITEMS.register("cast_iron_ring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_RING =
            ITEMS.register("steel_ring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> WOODEN_RING =
            ITEMS.register("wooden_ring",
                    () -> new Item(new Item.Properties()));

    //Пружины
    public static final RegistryObject<Item> BRONZE_SPRING =
            ITEMS.register("bronze_spring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_SPRING =
            ITEMS.register("copper_spring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GOLDEN_SPRING =
            ITEMS.register("golden_spring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_SPRING =
            ITEMS.register("iron_spring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TIN_SPRING =
            ITEMS.register("tin_spring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CAST_IRON_SPRING =
            ITEMS.register("cast_iron_spring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_SPRING =
            ITEMS.register("steel_spring",
                    () -> new Item(new Item.Properties()));

    //Большие пружины
    public static final RegistryObject<Item> BRONZE_LARGE_SPRING =
            ITEMS.register("bronze_large_spring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_LARGE_SPRING =
            ITEMS.register("copper_large_spring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GOLDEN_LARGE_SPRING =
            ITEMS.register("golden_large_spring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_LARGE_SPRING =
            ITEMS.register("iron_large_spring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TIN_LARGE_SPRING =
            ITEMS.register("tin_large_spring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CAST_IRON_LARGE_SPRING =
            ITEMS.register("cast_iron_large_spring",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_LARGE_SPRING =
            ITEMS.register("steel_large_spring",
                    () -> new Item(new Item.Properties()));

    //Лезвия пил
    public static final RegistryObject<Item> BRONZE_SAW_BLADE =
            ITEMS.register("bronze_saw_blade",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_SAW_BLADE =
            ITEMS.register("copper_saw_blade",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GOLDEN_SAW_BLADE =
            ITEMS.register("golden_saw_blade",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_SAW_BLADE =
            ITEMS.register("iron_saw_blade",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TIN_SAW_BLADE =
            ITEMS.register("tin_saw_blade",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CAST_IRON_SAW_BLADE =
            ITEMS.register("cast_iron_saw_blade",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_SAW_BLADE =
            ITEMS.register("steel_saw_blade",
                    () -> new Item(new Item.Properties()));

    //Винты
    public static final RegistryObject<Item> BRONZE_SCREW =
            ITEMS.register("bronze_screw",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_SCREW =
            ITEMS.register("copper_screw",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GOLDEN_SCREW =
            ITEMS.register("golden_screw",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_SCREW =
            ITEMS.register("iron_screw",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TIN_SCREW =
            ITEMS.register("tin_screw",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CAST_IRON_SCREW =
            ITEMS.register("cast_iron_screw",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_SCREW =
            ITEMS.register("steel_screw",
                    () -> new Item(new Item.Properties()));

    //шестеренки
    public static final RegistryObject<Item> IRON_GEAR =
            ITEMS.register("iron_gear",
                    () -> new Item(new Item.Properties()));

    //Катушки
    public static final RegistryObject<Item> COPPER_COIL =
            ITEMS.register("copper_coil",
                    () -> new Item(new Item.Properties()));

    //Нити
    public static final RegistryObject<Item> COAL_FILAMENT =
            ITEMS.register("coal_filament",
                    () -> new Item(new Item.Properties()));

    //Сетки
    public static final RegistryObject<Item> IRON_MESH =
            ITEMS.register("iron_mesh",
                    () -> new Item(new Item.Properties()));

    //Клинкер
    public static final RegistryObject<Item> CLINKER =
            ITEMS.register("clinker",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RAW_CLINKER =
            ITEMS.register("raw_clinker",
                    () -> new Item(new Item.Properties()));

    //Известняк
    public static final RegistryObject<Item> LIMESTONE_CHUNK =
            ITEMS.register("limestone_chunk",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> QUICKLIME =
            ITEMS.register("quicklime",
                    () -> new Quicklime(new Item.Properties()));

    public static final RegistryObject<Item> SLAKED_LIME =
            ITEMS.register("slaked_lime",
                    () -> new Item(new Item.Properties()));

    //Заполнители
    public static final RegistryObject<Item> CRUSHED_STONE =
            ITEMS.register("crushed_stone",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GYPSUM_POWDER =
            ITEMS.register("gypsum_powder",
                    () -> new Item(new Item.Properties()));

    // Части колеса
    public static final RegistryObject<Item> WHEEL_ARC_WITH_STICK =
            ITEMS.register("wheel_arc_with_stick",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> WHEEL_ARC_WITHOUT_STICK =
            ITEMS.register("wheel_arc_without_stick",
                    () -> new Item(new Item.Properties()));

    //Топливо
    public static final RegistryObject<Item> COKE =
            ITEMS.register("coke", () -> new Item(new Item.Properties()) {
                @Override
                public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                    return 3200; // уголь = 1600, кокс = 3200
                }
            });

    //растения
    public static final RegistryObject<Item> RAPESEED =
            ITEMS.register("rapeseed",
                    () -> new ItemNameBlockItem(BlockRegistry.RAPESEED_CROP.get(), new Item.Properties()));

    public static final RegistryObject<Item> RAPESEED_CLEANED =
            ITEMS.register("rapeseed_cleaned",
                    () -> new Item(new Item.Properties()));

    //Компоненты механизмов
    public static final RegistryObject<Item> REDUCTOR =
            ITEMS.register("reductor",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STATOR =
            ITEMS.register("stator",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ROTOR =
            ITEMS.register("rotor",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_MOTOR =
            ITEMS.register("electric_motor",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RESISTOR =
            ITEMS.register("resistor",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> VACUUM_TUBE =
            ITEMS.register("vacuum_tube",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CAPACITOR =
            ITEMS.register("capacitor",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RAW_PRINTED_CIRCUIT_BOARD =
            ITEMS.register("raw_printed_circuit_board",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PRINTED_CIRCUIT_BOARD =
            ITEMS.register("printed_circuit_board",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BASIC_CIRCUIT =
            ITEMS.register("basic_circuit",
                    () -> new Item(new Item.Properties()));

    //Хлам
    public static final RegistryObject<Item> SLAG =
            ITEMS.register("slag",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SALT =
            ITEMS.register("salt",
                    () -> new Item(new Item.Properties()));

    //===============================================БЛОКИ=====================================================
    //Компоненты механизмов
    public static final RegistryObject<Item> BRONZE_BRICK_CASING_ITEM =
            ITEMS.register("bronze_brick_casing",
                    () -> new BlockItem(BlockRegistry.BRONZE_BRICK_CASING.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> CAST_IRON_BRICK_CASING_ITEM =
            ITEMS.register("cast_iron_brick_casing",
                    () -> new BlockItem(BlockRegistry.CAST_IRON_BRICK_CASING.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> STEEL_BRICK_CASING_ITEM =
            ITEMS.register("steel_brick_casing",
                    () -> new BlockItem(BlockRegistry.STEEL_BRICK_CASING.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> STEEL_MACHINE_CASING_ITEM =
            ITEMS.register("steel_machine_casing",
                    () -> new BlockItem(BlockRegistry.STEEL_MACHINE_CASING.get(),
                            new Item.Properties()));

    //Строительные блоки
    public static final RegistryObject<Item> BRONZE_BLOCK_ITEM =
            ITEMS.register("bronze_block",
                    () -> new BlockItem(BlockRegistry.BRONZE_BLOCK.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> TELLURIUM_BLOCK_ITEM =
            ITEMS.register("tellurium_block",
                    () -> new BlockItem(BlockRegistry.TELLURIUM_BLOCK.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> TIN_BLOCK_ITEM =
            ITEMS.register("tin_block",
                    () -> new BlockItem(BlockRegistry.TIN_BLOCK.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> VULCANITE_BLOCK_ITEM =
            ITEMS.register("vulcanite_block",
                    () -> new BlockItem(BlockRegistry.VULCANITE_BLOCK.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> CAST_IRON_BLOCK_ITEM =
            ITEMS.register("cast_iron_block",
                    () -> new BlockItem(BlockRegistry.CAST_IRON_BLOCK.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> STEEL_BLOCK_ITEM =
            ITEMS.register("steel_block",
                    () -> new BlockItem(BlockRegistry.STEEL_BLOCK.get(),
                            new Item.Properties()));

    //Шестерни
    public static final RegistryObject<Item> VULCANITE_GEAR_ITEM =
            ITEMS.register("vulcanite_gear",
                    () -> new BlockItem(BlockRegistry.VULCANITE_GEAR.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> VULCANITE_BEARING_ITEM =
            ITEMS.register("vulcanite_bearing",
                    () -> new BlockItem(BlockRegistry.VULCANITE_BEARING.get(),
                            new Item.Properties()));

    //Трубы
    public static final RegistryObject<Item> PIPE_INSULATOR_ITEM =
            ITEMS.register("pipe_insulator",
                    () -> new BlockItem(BlockRegistry.PIPE_INSULATOR.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> BRONZE_PIPE_ITEM =
            ITEMS.register("bronze_pipe",
                    () -> new BlockItem(BlockRegistry.BRONZE_PIPE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> TIN_PIPE_ITEM =
            ITEMS.register("tin_pipe",
                    () -> new BlockItem(BlockRegistry.TIN_PIPE.get(),
                            new Item.Properties()));
    public static final RegistryObject<Item> IRON_PIPE_ITEM =
            ITEMS.register("iron_pipe",
                    () -> new BlockItem(BlockRegistry.IRON_PIPE.get(),
                            new Item.Properties()));
    public static final RegistryObject<Item> GOLDEN_PIPE_ITEM =
            ITEMS.register("golden_pipe",
                    () -> new BlockItem(BlockRegistry.GOLDEN_PIPE.get(),
                            new Item.Properties()));
    public static final RegistryObject<Item> COPPER_PIPE_ITEM =
            ITEMS.register("copper_pipe",
                    () -> new BlockItem(BlockRegistry.COPPER_PIPE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> CAST_IRON_PIPE_ITEM =
            ITEMS.register("cast_iron_pipe",
                    () -> new BlockItem(BlockRegistry.CAST_IRON_PIPE.get(),
                            new Item.Properties()));
    public static final RegistryObject<Item> STEEL_PIPE_ITEM =
            ITEMS.register("steel_pipe",
                    () -> new BlockItem(BlockRegistry.STEEL_PIPE.get(),
                            new Item.Properties()));
    //Кинетические генераторы
    public static final RegistryObject<Item> MANUAL_KINETIC_GENERATOR_ITEM =
            ITEMS.register("manual_kinetic_generator",
                    () -> new BlockItem(BlockRegistry.MANUAL_KINETIC_GENERATOR.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> WATER_WHEEL_ITEM =
            ITEMS.register("water_wheel",
                    () -> new BlockItem(BlockRegistry.WATER_WHEEL.get(),
                            new Item.Properties()));

    //Паровые генераторы низкого давления
    public static final RegistryObject<Item> FIREBOX_ITEM =
            ITEMS.register("firebox",
                    () -> new BlockItem(BlockRegistry.FIREBOX.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> BOILER_TANK_ITEM =
            ITEMS.register("boiler_tank",
                    () -> new BlockItem(BlockRegistry.BOILER_TANK.get(),
                            new Item.Properties()));

    //Камни
    public static final RegistryObject<Item> GYPSUM_STONE_ITEM =
            ITEMS.register("gypsum_stone",
                    () -> new BlockItem(BlockRegistry.GYPSUM_STONE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> LIMESTONE_ITEM =
            ITEMS.register("limestone",
                    () -> new BlockItem(BlockRegistry.LIMESTONE.get(),
                            new Item.Properties()));

    //Каменные руды
    public static final RegistryObject<Item> TELLURIUM_ORE_ITEM =
            ITEMS.register("tellurium_ore",
                    () -> new BlockItem(BlockRegistry.TELLURIUM_ORE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> TIN_ORE_ITEM =
            ITEMS.register("tin_ore",
                    () -> new BlockItem(BlockRegistry.TIN_ORE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> SULFUR_ORE_ITEM =
            ITEMS.register("sulfur_ore",
                    () -> new BlockItem(BlockRegistry.SULFUR_ORE.get(),
                            new Item.Properties()));

    //Глубинносланцевые руды
    public static final RegistryObject<Item> DEEPSLATE_TELLURIUM_ORE_ITEM =
            ITEMS.register("deepslate_tellurium_ore",
                    () -> new BlockItem(BlockRegistry.DEEPSLATE_TELLURIUM_ORE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> DEEPSLATE_TIN_ORE_ITEM =
            ITEMS.register("deepslate_tin_ore",
                    () -> new BlockItem(BlockRegistry.DEEPSLATE_TIN_ORE.get(),
                            new Item.Properties()));

    //Кинетические станки
    public static final RegistryObject<Item> PRESS_ITEM =
            ITEMS.register("press",
                    () -> new BlockItem(BlockRegistry.PRESS.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> QUERN_ITEM =
            ITEMS.register("quern",
                    () -> new BlockItem(BlockRegistry.QUERN.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> SAWMILL_ITEM =
            ITEMS.register("sawmill",
                    () -> new BlockItem(BlockRegistry.SAWMILL.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> OIL_PRESS_ITEM =
            ITEMS.register("oil_press",
                    () -> new BlockItem(BlockRegistry.OIL_PRESS.get(),
                            new Item.Properties()));

    //Паровые станки низкого давления
    public static final RegistryObject<Item> STEAM_FURNACE_ITEM =
            ITEMS.register("steam_furnace",
                    () -> new BlockItem(BlockRegistry.STEAM_FURNACE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> STEAM_ALLOY_FURNACE_ITEM =
            ITEMS.register("steam_alloy_furnace",
                    () -> new BlockItem(BlockRegistry.STEAM_ALLOY_FURNACE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> STEAM_GRINDER_ITEM =
            ITEMS.register("steam_grinder",
                    () -> new BlockItem(BlockRegistry.STEAM_GRINDER.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> STEAM_COMPRESSOR_ITEM =
            ITEMS.register("steam_compressor",
                    () -> new BlockItem(BlockRegistry.STEAM_COMPRESSOR.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> STEAM_PRESS_ITEM =
            ITEMS.register("steam_press",
                    () -> new BlockItem(BlockRegistry.STEAM_PRESS.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> STEAM_SAWMILL_ITEM =
            ITEMS.register("steam_sawmill",
                    () -> new BlockItem(BlockRegistry.STEAM_SAWMILL.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> STEAM_EXTRACTOR_ITEM =
            ITEMS.register("steam_extractor",
                    () -> new BlockItem(BlockRegistry.STEAM_EXTRACTOR.get(),
                            new Item.Properties()));

    //Строительные индустриальные блоки
    public static final RegistryObject<Item> CEMENT_BLOCK_ITEM =
            ITEMS.register("cement_block",
                    () -> new BlockItem(BlockRegistry.CEMENT_BLOCK.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> CONCRETE_BLOCK_ITEM =
            ITEMS.register("concrete_block",
                    () -> new BlockItem(BlockRegistry.CONCRETE_BLOCK.get(),
                            new Item.Properties()));

    //Части мультиблоков
    public static final RegistryObject<Item> COKE_OVEN_BRICKS_ITEM =
            ITEMS.register("coke_oven_bricks",
                    () -> new BlockItem(BlockRegistry.COKE_OVEN_BRICKS.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> COKE_OVEN_CONTROLLER_ITEM =
            ITEMS.register("coke_oven_controller",
                    () -> new BlockItem(BlockRegistry.COKE_OVEN_CONTROLLER.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> BLAST_FURNACE_BRICKS_ITEM =
            ITEMS.register("blast_furnace_bricks",
                    () -> new BlockItem(BlockRegistry.BLAST_FURNACE_BRICKS.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> BLAST_FURNACE_CONTROLLER_ITEM =
            ITEMS.register("blast_furnace_controller",
                    () -> new BlockItem(BlockRegistry.BLAST_FURNACE_CONTROLLER.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> OPEN_HEARTH_BRICKS_ITEM =
            ITEMS.register("open_hearth_bricks",
                    () -> new BlockItem(BlockRegistry.OPEN_HEARTH_BRICKS.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> OPEN_HEARTH_CONTROLLER_ITEM =
            ITEMS.register("open_hearth_controller",
                    () -> new BlockItem(BlockRegistry.OPEN_HEARTH_CONTROLLER.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> OPEN_HEARTH_FLUID_HATCH_ITEM =
            ITEMS.register("open_hearth_fluid_hatch",
                    () -> new BlockItem(BlockRegistry.OPEN_HEARTH_FLUID_HATCH.get(),
                            new Item.Properties()));

    //Паровые генераторы среднего давления
    public static final RegistryObject<Item> UPGRADED_FIREBOX_ITEM =
            ITEMS.register("upgraded_firebox",
                    () -> new BlockItem(BlockRegistry.UPGRADED_FIREBOX.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> UPGRADED_BOILER_TANK_ITEM =
            ITEMS.register("upgraded_boiler_tank",
                    () -> new BlockItem(BlockRegistry.UPGRADED_BOILER_TANK.get(),
                            new Item.Properties()));

    //Паровые станки среднего давления
    public static final RegistryObject<Item> UPGRADED_STEAM_FURNACE_ITEM =
            ITEMS.register("upgraded_steam_furnace",
                    () -> new BlockItem(BlockRegistry.UPGRADED_STEAM_FURNACE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> UPGRADED_STEAM_ALLOY_FURNACE_ITEM =
            ITEMS.register("upgraded_steam_alloy_furnace",
                    () -> new BlockItem(BlockRegistry.UPGRADED_STEAM_ALLOY_FURNACE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> UPGRADED_STEAM_GRINDER_ITEM =
            ITEMS.register("upgraded_steam_grinder",
                    () -> new BlockItem(BlockRegistry.UPGRADED_STEAM_GRINDER.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> UPGRADED_STEAM_COMPRESSOR_ITEM =
            ITEMS.register("upgraded_steam_compressor",
                    () -> new BlockItem(BlockRegistry.UPGRADED_STEAM_COMPRESSOR.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> UPGRADED_STEAM_PRESS_ITEM =
            ITEMS.register("upgraded_steam_press",
                    () -> new BlockItem(BlockRegistry.UPGRADED_STEAM_PRESS.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> UPGRADED_STEAM_SAWMILL_ITEM =
            ITEMS.register("upgraded_steam_sawmill",
                    () -> new BlockItem(BlockRegistry.UPGRADED_STEAM_SAWMILL.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> UPGRADED_STEAM_EXTRACTOR_ITEM =
            ITEMS.register("upgraded_steam_extractor",
                    () -> new BlockItem(BlockRegistry.UPGRADED_STEAM_EXTRACTOR.get(),
                            new Item.Properties()));

    //Паровые генераторы высокого давления
    public static final RegistryObject<Item> ADVANCED_FIREBOX_ITEM =
            ITEMS.register("advanced_firebox",
                    () -> new BlockItem(BlockRegistry.ADVANCED_FIREBOX.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_BOILER_TANK_ITEM =
            ITEMS.register("advanced_boiler_tank",
                    () -> new BlockItem(BlockRegistry.ADVANCED_BOILER_TANK.get(),
                            new Item.Properties()));

    //Паровые станки высокого давления
    public static final RegistryObject<Item> ADVANCED_STEAM_FURNACE_ITEM =
            ITEMS.register("advanced_steam_furnace",
                    () -> new BlockItem(BlockRegistry.ADVANCED_STEAM_FURNACE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_STEAM_ALLOY_FURNACE_ITEM =
            ITEMS.register("advanced_steam_alloy_furnace",
                    () -> new BlockItem(BlockRegistry.ADVANCED_STEAM_ALLOY_FURNACE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_STEAM_GRINDER_ITEM =
            ITEMS.register("advanced_steam_grinder",
                    () -> new BlockItem(BlockRegistry.ADVANCED_STEAM_GRINDER.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_STEAM_COMPRESSOR_ITEM =
            ITEMS.register("advanced_steam_compressor",
                    () -> new BlockItem(BlockRegistry.ADVANCED_STEAM_COMPRESSOR.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_STEAM_PRESS_ITEM =
            ITEMS.register("advanced_steam_press",
                    () -> new BlockItem(BlockRegistry.ADVANCED_STEAM_PRESS.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_STEAM_SAWMILL_ITEM =
            ITEMS.register("advanced_steam_sawmill",
                    () -> new BlockItem(BlockRegistry.ADVANCED_STEAM_SAWMILL.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_STEAM_EXTRACTOR_ITEM =
            ITEMS.register("advanced_steam_extractor",
                    () -> new BlockItem(BlockRegistry.ADVANCED_STEAM_EXTRACTOR.get(),
                            new Item.Properties()));

    //Бревна
    public static final RegistryObject<Item> CASTILLA_LOG_ITEM =
            ITEMS.register("castilla_log",
                    () -> new BlockItem(BlockRegistry.CASTILLA_LOG.get(),
                            new Item.Properties()));

    //Доски
    public static final RegistryObject<Item> CASTILLA_PLANKS_ITEM =
            ITEMS.register("castilla_planks",
                    () -> new BlockItem(BlockRegistry.CASTILLA_PLANKS.get(),
                            new Item.Properties()));

    //Листья
    public static final RegistryObject<Item> CASTILLA_LEAVES_ITEM =
            ITEMS.register("castilla_leaves",
                    () -> new BlockItem(BlockRegistry.CASTILLA_LEAVES.get(),
                            new Item.Properties()));

    //Саженцы
    public static final RegistryObject<Item> CASTILLA_SAPLING_ITEM =
            ITEMS.register("castilla_sapling",
                    () -> new BlockItem(BlockRegistry.CASTILLA_SAPLING.get(),
                            new Item.Properties()));

    //Провода
    public static final RegistryObject<Item> TIN_WIRE_ITEM =
            ITEMS.register("tin_wire",
                    () -> new BlockItem(BlockRegistry.TIN_WIRE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> COPPER_WIRE_ITEM =
            ITEMS.register("copper_wire",
                    () -> new BlockItem(BlockRegistry.COPPER_WIRE.get(),
                            new Item.Properties()));

    //Изолированные провода
    public static final RegistryObject<Item> INSULATED_TIN_WIRE_ITEM =
            ITEMS.register("insulated_tin_wire",
                    () -> new BlockItem(BlockRegistry.INSULATED_TIN_WIRE.get(),
                            new Item.Properties()));

    //Электрические генераторы
    public static final RegistryObject<Item> STEAM_TURBINE_ITEM =
            ITEMS.register("steam_turbine",
                    () -> new BlockItem(BlockRegistry.STEAM_TURBINE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_GENERATOR_ITEM =
            ITEMS.register("electric_generator",
                    () -> new BlockItem(BlockRegistry.ELECTRIC_GENERATOR.get(),
                            new Item.Properties()));

    //Электрические станки
    public static final RegistryObject<Item> ELECTRIC_FURNACE_ITEM =
            ITEMS.register("electric_furnace",
                    () -> new BlockItem(BlockRegistry.ELECTRIC_FURNACE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_ALLOY_FURNACE_ITEM =
            ITEMS.register("electric_alloy_furnace",
                    () -> new BlockItem(BlockRegistry.ELECTRIC_ALLOY_FURNACE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_GRINDER_ITEM =
            ITEMS.register("electric_grinder",
                    () -> new BlockItem(BlockRegistry.ELECTRIC_GRINDER.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_COMPRESSOR_ITEM =
            ITEMS.register("electric_compressor",
                    () -> new BlockItem(BlockRegistry.ELECTRIC_COMPRESSOR.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_EXTRACTOR_ITEM =
            ITEMS.register("electric_extractor",
                    () -> new BlockItem(BlockRegistry.ELECTRIC_EXTRACTOR.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_BENDER_ITEM =
            ITEMS.register("electric_bender",
                    () -> new BlockItem(BlockRegistry.ELECTRIC_BENDER.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_CUTTER_ITEM =
            ITEMS.register("electric_cutter",
                    () -> new BlockItem(BlockRegistry.ELECTRIC_CUTTER.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_WIREMILL_ITEM =
            ITEMS.register("electric_wiremill",
                    () -> new BlockItem(BlockRegistry.ELECTRIC_WIREMILL.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_LATHE_ITEM =
            ITEMS.register("electric_lathe",
                    () -> new BlockItem(BlockRegistry.ELECTRIC_LATHE.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> DISTILLER_ITEM =
            ITEMS.register("distiller",
                    () -> new BlockItem(BlockRegistry.DISTILLER.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> CONDENSER_ITEM =
            ITEMS.register("condenser",
                    () -> new BlockItem(BlockRegistry.CONDENSER.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> ELECTROLYZER_ITEM =
            ITEMS.register("electrolyzer",
                    () -> new BlockItem(BlockRegistry.ELECTROLYZER.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> CHEMICAL_REACTOR_ITEM =
            ITEMS.register("chemical_reactor",
                    () -> new BlockItem(BlockRegistry.CHEMICAL_REACTOR.get(),
                            new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
