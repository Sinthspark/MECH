package com.sinthspark.mech.entity;

import com.sinthspark.mech.MECH;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MECH.MOD_ID);

    public static final RegistryObject<EntityType<AdvancedBoilerExplosive>> ADVANCED_BOILER_EXPLOSIVE =
            ENTITIES.register("advanced_boiler_explosive", () -> EntityType.Builder
                    .<AdvancedBoilerExplosive>of(AdvancedBoilerExplosive::new, MobCategory.MISC)
                    .sized(0.98f, 0.98f)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("advanced_boiler_explosive"));
}
