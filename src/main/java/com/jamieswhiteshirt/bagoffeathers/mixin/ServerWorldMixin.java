package com.jamieswhiteshirt.bagoffeathers.mixin;

import com.jamieswhiteshirt.bagoffeathers.BagOfFeathers;
import com.jamieswhiteshirt.bagoffeathers.common.FeatherCloudHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements FeatherCloudHandler {
    @Override
    public void bag_of_feathers$onFeathersPlaced(BlockPos pos, LivingEntity actor) {
        BagOfFeathers.sendFeatherCloudMessage(actor, (ServerWorld) (Object) this, pos, true);
    }

    @Override
    public void bag_of_feathers$onFeathersBroken(BlockPos pos, LivingEntity actor) {
        BagOfFeathers.sendFeatherCloudMessage(actor, (ServerWorld) (Object) this, pos, true);
    }
}
