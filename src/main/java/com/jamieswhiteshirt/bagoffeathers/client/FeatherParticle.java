package com.jamieswhiteshirt.bagoffeathers.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class FeatherParticle extends SpriteBillboardParticle {
    public static ParticleTextureSheet SHEET = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;

    protected FeatherParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.gravityStrength = 0.0025F;
        float lightness = 0.75F + random.nextFloat() * 0.25F;
        this.colorRed = lightness;
        this.colorGreen = lightness;
        this.colorBlue = lightness;
        this.maxAge = 40 + random.nextInt(20);
    }

    @Override
    public ParticleTextureSheet getType() {
        return SHEET;
    }

    @Override
    public void tick() {
        this.velocityX *= 0.8D;
        this.velocityY *= 0.8D;
        this.velocityZ *= 0.8D;
        super.tick();
    }

    public void disperse(BlockPos pos) {
        if (new BlockPos(this.x, this.y, this.z).equals(pos)) {
            this.velocityX += (random.nextDouble() - random.nextDouble()) * 0.5D;
            this.velocityY += (random.nextDouble() - random.nextDouble()) * 0.5D;
            this.velocityZ += (random.nextDouble() - random.nextDouble()) * 0.5D;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public @Nullable Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            FeatherParticle particle = new FeatherParticle(world, x, y, z, velocityX, velocityY, velocityZ);
            particle.setSprite(spriteProvider.getSprite(world.random));
            return particle;
        }
    }
}
