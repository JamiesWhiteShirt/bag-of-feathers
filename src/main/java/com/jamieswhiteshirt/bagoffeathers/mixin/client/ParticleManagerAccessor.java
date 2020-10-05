package com.jamieswhiteshirt.bagoffeathers.mixin.client;

import net.minecraft.client.particle.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Queue;

@Mixin(ParticleManager.class)
public interface ParticleManagerAccessor {
    @Accessor("particles")
    Map<ParticleTextureSheet, Queue<Particle>> bag_of_feathers$getParticles();
}
