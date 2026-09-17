package com.sinthspark.mech.client;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.blockentity.ModBlockEntities;
import com.sinthspark.mech.client.renderer.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MECH.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEventSubscriber {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.VULCANITE_GEAR.get(), VulcaniteGearRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WATER_WHEEL.get(), WaterWheelRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MANUAL_KINETIC_GENERATOR.get(), ManualKineticGeneratorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VULCANITE_BEARING.get(), VulcaniteBearingRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.QUERN.get(), QuernRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SAWMILL.get(), SawmillRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PRESS.get(), PressRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.OIL_PRESS.get(), OilPressRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PIPE.get(), PipeInsulatorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STEAM_TURBINE.get(), SteamTurbineRenderer::new);
    }
}