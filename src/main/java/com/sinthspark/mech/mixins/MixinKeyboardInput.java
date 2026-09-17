package com.sinthspark.mech.mixins;

import com.sinthspark.mech.registration.EffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class MixinKeyboardInput extends Input {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(boolean p_234118_, float p_234119_, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        MobEffectInstance effect = player.getEffect(EffectRegistry.SHOCK.get());
        if (effect != null) {
            // Инвертируем WASD
            float origForward = this.forwardImpulse;
            float origLeft = this.leftImpulse;

            this.forwardImpulse = -origForward;
            this.leftImpulse = -origLeft;

            // 30% шанс ещё и поменять местами
            if (player.getRandom().nextFloat() < 0.3f) {
                this.forwardImpulse = -origLeft;
                this.leftImpulse = -origForward;
            }
        }
    }
}