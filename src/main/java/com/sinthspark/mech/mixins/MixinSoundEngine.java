package com.sinthspark.mech.mixins;

import com.sinthspark.mech.registration.EffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class MixinSoundEngine {

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void onPlay(SoundInstance p_120313_, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.hasEffect(EffectRegistry.SHOCK.get())) {
            // Блокируем все звуки пока игрок под эффектом шока
            ci.cancel();
        }
    }
}
