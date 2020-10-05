package com.jamieswhiteshirt.bagoffeathers.client;

import com.jamieswhiteshirt.bagoffeathers.BagOfFeathers;
import com.jamieswhiteshirt.bagoffeathers.common.FeatherCloudHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class BagOfFeathersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSidePacketRegistry.INSTANCE.register(BagOfFeathers.CHANNEL, (ctx, buf) -> {
            int messageType = buf.readVarInt();
            switch (messageType) {
                case BagOfFeathers.BLOCK_PARTICLE_EVENT_S2C:
                    BlockPos pos = buf.readBlockPos();
                    boolean placed = buf.readBoolean();
                    ctx.getTaskQueue().execute(() -> {
                        if (placed) {
                            ((FeatherCloudHandler) ctx.getPlayer().world).bag_of_feathers$onFeathersPlaced(pos, ctx.getPlayer());
                        } else {
                            ((FeatherCloudHandler) ctx.getPlayer().world).bag_of_feathers$onFeathersBroken(pos, ctx.getPlayer());
                        }
                    });
                    break;
                case BagOfFeathers.ENTITY_PARTICLE_EVENT_S2C:
                    int entityId = buf.readVarInt();
                    ctx.getTaskQueue().execute(() -> {
                        Entity entity = ctx.getPlayer().world.getEntityById(entityId);
                        if (entity != null) {
                            MinecraftClient.getInstance().particleManager.addEmitter(entity, BagOfFeathers.FEATHER);
                        }
                    });
            }
        });

        ParticleFactoryRegistry.getInstance().register(BagOfFeathers.FEATHER, FeatherParticle.Factory::new);
    }
}
