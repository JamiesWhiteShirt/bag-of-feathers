package com.jamieswhiteshirt.bagoffeathers.common;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

public interface FeatherCloudHandler {
    void bag_of_feathers$onFeathersPlaced(BlockPos pos, LivingEntity actor);

    void bag_of_feathers$onFeathersBroken(BlockPos pos, LivingEntity actor);
}
