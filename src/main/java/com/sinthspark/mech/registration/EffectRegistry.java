package com.sinthspark.mech.registration;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.effects.ShockEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EffectRegistry {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MECH.MOD_ID);

    public static final RegistryObject<MobEffect> SHOCK =
            EFFECTS.register("shock", ShockEffect::new);
}