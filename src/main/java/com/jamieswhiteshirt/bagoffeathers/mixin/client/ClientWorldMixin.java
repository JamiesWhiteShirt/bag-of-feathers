package com.jamieswhiteshirt.bagoffeathers.mixin.client;

import com.jamieswhiteshirt.bagoffeathers.client.Particles;
import com.jamieswhiteshirt.bagoffeathers.common.FeatherCloudHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin implements FeatherCloudHandler {
    @Override
    public void bag_of_feathers$onFeathersPlaced(BlockPos pos, LivingEntity actor) {
        Particles.spawnCloudParticles((World) (Object) this, pos);
    }

    @Override
    public void bag_of_feathers$onFeathersBroken(BlockPos pos, LivingEntity actor) {
        Particles.disperseCloudParticles((World) (Object) this, pos);
    }
}
