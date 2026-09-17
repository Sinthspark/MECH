package com.sinthspark.mech.mixins;

import com.sinthspark.mech.blocks.WaterWheelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "pick", at = @At("RETURN"), cancellable = true)
    private void onPick(double range, float partialTicks, boolean hitFluids,
                        CallbackInfoReturnable<BlockHitResult> cir) {
        Entity self = (Entity) (Object) this;
        Level level = self.level();

        Vec3 start = self.getEyePosition(partialTicks);
        Vec3 end = start.add(self.getViewVector(partialTicks).scale(range));

        BlockHitResult original = cir.getReturnValue();

        // Проверяем, может ли колесо быть ближе, чем оригинальный хит
        BlockPos.betweenClosedStream(
                BlockPos.containing(start.x - 5, start.y - 5, start.z - 5),
                BlockPos.containing(end.x + 5, end.y + 5, end.z + 5)
        ).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof WaterWheelBlock) {
                BlockHitResult hit = state.getShape(level, pos).clip(start, end, pos);
                if (hit != null) {
                    // Если оригинального хита нет, или колесо ближе — заменяем
                    if (original == null ||
                            start.distanceToSqr(hit.getLocation()) < start.distanceToSqr(original.getLocation())) {
                        cir.setReturnValue(hit);
                    }
                }
            }
        });
    }
}
