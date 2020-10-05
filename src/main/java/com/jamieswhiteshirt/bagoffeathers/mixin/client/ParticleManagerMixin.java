package com.jamieswhiteshirt.bagoffeathers.mixin.client;

import com.jamieswhiteshirt.bagoffeathers.BagOfFeathers;
import com.jamieswhiteshirt.bagoffeathers.client.Particles;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @Inject(
        method = "addBlockBreakParticles(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void addBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (state.getBlock() == BagOfFeathers.FEATHER_CLOUD) {
            ci.cancel();
        }
    }
}
