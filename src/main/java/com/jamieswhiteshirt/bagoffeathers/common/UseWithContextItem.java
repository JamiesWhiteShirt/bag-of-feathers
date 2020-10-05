package com.jamieswhiteshirt.bagoffeathers.common;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * When this interface is implemented on an item, it allows the client to send additional contextual data to the server
 * when the item is used.
 */
public interface UseWithContextItem {
    @Environment(EnvType.CLIENT)
    void prepareClientUse(PacketByteBuf buf, PlayerEntity user, World world, Hand hand);

    void prepareServerUse(PacketByteBuf buf, PlayerEntity user, World world, Hand hand);
}
