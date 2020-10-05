package com.jamieswhiteshirt.bagoffeathers;

import com.jamieswhiteshirt.bagoffeathers.common.BagOfFeathersItem;
import com.jamieswhiteshirt.bagoffeathers.common.FeatherCloudBlock;
import com.jamieswhiteshirt.bagoffeathers.common.UseWithContextItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class BagOfFeathers implements ModInitializer {
	public static Material FEATHER_CLOUD_MATERIAL = (new FabricMaterialBuilder(MaterialColor.CLEAR)).destroyedByPiston().allowsMovement().lightPassesThrough().burnable().notSolid().build();
	public static FeatherCloudBlock FEATHER_CLOUD = Registry.register(Registry.BLOCK, new Identifier("bag_of_feathers", "feather_cloud"), new FeatherCloudBlock(FabricBlockSettings.of(FEATHER_CLOUD_MATERIAL).noCollision().breakInstantly().sounds(BlockSoundGroup.WOOL).dropsNothing()));
	public static BagOfFeathersItem BAG_OF_FEATHERS = Registry.register(Registry.ITEM, new Identifier("bag_of_feathers", "bag_of_feathers"), new BagOfFeathersItem(FEATHER_CLOUD, new Item.Settings()));
	public static DefaultParticleType FEATHER = Registry.register(Registry.PARTICLE_TYPE, new Identifier("bag_of_feathers", "feather"), FabricParticleTypes.simple(true));

	public static final Identifier CHANNEL = new Identifier("bag_of_feathers", "net");
	public static final int BLOCK_PARTICLE_EVENT_S2C = 0;
	public static final int ENTITY_PARTICLE_EVENT_S2C = 1;
	public static final int USE_WITH_CONTEXT_C2S = 0;

	@Override
	public void onInitialize() {
		ServerSidePacketRegistry.INSTANCE.register(CHANNEL, (ctx, buf) -> {
			switch (buf.readVarInt()) {
				case USE_WITH_CONTEXT_C2S:
					Hand hand = buf.readEnumConstant(Hand.class);
					buf.retain();
					ctx.getTaskQueue().execute(() -> {
						ServerPlayerEntity user = (ServerPlayerEntity) ctx.getPlayer();
						ItemStack stack = user.getStackInHand(hand);
						Item item = stack.getItem();
						if (item instanceof UseWithContextItem) {
							((UseWithContextItem) item).prepareServerUse(buf, user, user.world, hand);
						}
						buf.release();
						PlayerInteractItemC2SPacket dummyPacket = new PlayerInteractItemC2SPacket(hand);
						dummyPacket.apply(user.networkHandler);
					});
			}
		});
	}

	public static void sendFeatherCloudMessage(LivingEntity actor, ServerWorld world, BlockPos pos, boolean placed) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeVarInt(BLOCK_PARTICLE_EVENT_S2C);
		buf.writeBlockPos(pos);
		buf.writeBoolean(placed);
		CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(CHANNEL, buf);
        PlayerStream.around(world, pos, 32D)
			.filter(player -> player != actor)
			.forEach(player -> ((ServerPlayerEntity)player).networkHandler.sendPacket(packet));
	}

	public static void sendFeatherCloudMessage(LivingEntity target) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeVarInt(ENTITY_PARTICLE_EVENT_S2C);
		buf.writeVarInt(target.getEntityId());
		CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(CHANNEL, buf);
		PlayerStream.around(target.world, target.getPos(), 32D)
			.forEach(player -> ((ServerPlayerEntity)player).networkHandler.sendPacket(packet));
	}
}
