package com.jamieswhiteshirt.bagoffeathers.mixin.client;

import com.jamieswhiteshirt.bagoffeathers.BagOfFeathers;
import com.jamieswhiteshirt.bagoffeathers.common.UseWithContextItem;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @ModifyArg(
        method = "interactItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V")
    )
    private Packet<?> modifyInteractionPacket(Packet<?> packet) {
        if (packet instanceof PlayerInteractItemC2SPacket) {
            Hand hand = ((PlayerInteractItemC2SPacket) packet).getHand();
            PlayerEntity player = MinecraftClient.getInstance().player;
            ItemStack stack = player.getStackInHand(hand);
            Item item = stack.getItem();
            if (item instanceof UseWithContextItem) {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeVarInt(BagOfFeathers.USE_WITH_CONTEXT_C2S);
                buf.writeEnumConstant(hand);
                ((UseWithContextItem) item).prepareClientUse(buf, player, MinecraftClient.getInstance().world, hand);
                return new CustomPayloadC2SPacket(BagOfFeathers.CHANNEL, buf);
            }
        }
        return packet;
    }
}
