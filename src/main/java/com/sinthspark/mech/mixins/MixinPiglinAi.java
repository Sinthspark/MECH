package com.sinthspark.mech.mixins;

import com.sinthspark.mech.registration.EffectRegistry;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public class MixinPiglinAi {

    @Inject(method = "wantsToPickup", at = @At("HEAD"), cancellable = true)
    private static void onWantsToPickup(Piglin piglin, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (piglin.hasEffect(EffectRegistry.SHOCK.get())) {
            cir.setReturnValue(false);
        }
    }
}