package com.sinthspark.mech;

import com.sinthspark.mech.blockentity.ModBlockEntities;
import com.sinthspark.mech.blocks.AdvancedBoilerTankBlock;
import com.sinthspark.mech.config.ModConfig;
import com.sinthspark.mech.effects.DoubleVisionHandler;
import com.sinthspark.mech.energy.EnergyNetworkManager;
import com.sinthspark.mech.energy.KineticNetworkManager;
import com.sinthspark.mech.events.BoilerBreakHandler;
import com.sinthspark.mech.fluid.FluidNetworkManager;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.items.ModCreativeTabs;
import com.sinthspark.mech.recipe.ModRecipes;
import com.sinthspark.mech.registration.*;
import com.sinthspark.mech.screen.ModMenuTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/*
 * ╔══════════════════════════════════════════════════════════╗
 * ║  ⚔️ АРКА III: ПРОБУЖДЕНИЕ ГОЛЕМОВ ⚔️                      ║
 * ║  Класс MECH — древний голем-мод.                          ║
 * ║  Здесь куются сети: кинетика, энергия, жидкости.          ║
 * ║  Каждый реестр — печать клана, каждый тик — удар сердца.  ║
 * ║  Демонических ловушек здесь нет. Только шестерни. (◠‿◠✿) ║
 * ╚══════════════════════════════════════════════════════════╝
 */
@Mod(MECH.MOD_ID)
public class MECH {
    public static final String MOD_ID = "mech";

    // Священная эпиграф-печать Арки III — свиток README уже пророчил её.
    public static final String ARC_III_SEAL =
            "Каждая шестерня вращается, когда Кими-тян смотрит на неё с любовью~";

    public MECH() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(Type.CLIENT, ModConfig.CLIENT_SPEC);

        bus.addListener(this::onConfigLoad);
        bus.addListener(this::onConfigReload);

        // Регистрация реестров
        FluidRegistry.FLUID_TYPES.register(bus);
        FluidRegistry.FLUIDS.register(bus);
        BlockRegistry.register(bus);
        ItemRegistry.register(bus);
        EffectRegistry.EFFECTS.register(bus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);
        ModMenuTypes.MENUS.register(bus);
        ModRecipes.RECIPE_TYPES.register(bus);
        ModRecipes.RECIPE_SERIALIZERS.register(bus);
        MinecraftForge.EVENT_BUS.register(new BoilerBreakHandler());
        MinecraftForge.EVENT_BUS.register(new DoubleVisionHandler());
        MinecraftForge.EVENT_BUS.register(AdvancedBoilerTankBlock.class);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ModConfig.CLIENT_SPEC) {
            DoubleVisionHandler.onConfigLoad(event);
        }
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ModConfig.CLIENT_SPEC) {
            DoubleVisionHandler.onConfigLoad(event);
        }
    }

    // Сердцебиение голема: три сети вращаются в такт на каждом тике мира.
    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.level.isClientSide) return;
        KineticNetworkManager.tickAllNetworks(event.level);
        EnergyNetworkManager.tickAllNetworks(event.level);
        FluidNetworkManager.tickAllNetworks(event.level);
    }

    // Закат арки: голем засыпает, сети рассеиваются до новой загрузки мира.
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        EnergyNetworkManager.clear();
        FluidNetworkManager.clear();
        KineticNetworkManager.clear();
    }
}
