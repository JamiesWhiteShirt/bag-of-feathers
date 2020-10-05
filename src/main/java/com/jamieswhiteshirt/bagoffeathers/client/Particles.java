package com.jamieswhiteshirt.bagoffeathers.client;

import com.jamieswhiteshirt.bagoffeathers.BagOfFeathers;
import com.jamieswhiteshirt.bagoffeathers.mixin.client.ParticleManagerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Queue;

@Environment(EnvType.CLIENT)
public class Particles {
    public static void spawnCloudParticles(World world, BlockPos pos) {
        final int RESOLUTION = 4;
        for (int x = 0; x < RESOLUTION; ++x) {
            float xf = pos.getX() + (x + 0.5F) / RESOLUTION;
            for (int y = 0; y < RESOLUTION; ++y) {
                float yf = pos.getY() + (y + 0.5F) / RESOLUTION;
                for (int z = 0; z < RESOLUTION; ++z) {
                    float zf = pos.getZ() + (z + 0.5F) / RESOLUTION;
                    float x1 = xf + (world.random.nextFloat() - world.random.nextFloat()) * 0.1F;
                    float y1 = yf + (world.random.nextFloat() - world.random.nextFloat()) * 0.1F;
                    float z1 = zf + (world.random.nextFloat() - world.random.nextFloat()) * 0.1F;
                    world.addParticle(BagOfFeathers.FEATHER, x1, y1, z1, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    public static void disperseCloudParticles(World world, BlockPos pos) {
        Queue<Particle> particles = ((ParticleManagerAccessor) MinecraftClient.getInstance().particleManager).bag_of_feathers$getParticles().get(FeatherParticle.SHEET);

        if (particles == null) {
            return;
        }

        for (Particle particle : particles) {
            if (particle instanceof FeatherParticle) {
                ((FeatherParticle) particle).disperse(pos);
            }
        }
    }
}
