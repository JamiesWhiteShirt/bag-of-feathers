package com.jamieswhiteshirt.bagoffeathers.common;

import com.jamieswhiteshirt.bagoffeathers.BagOfFeathers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.MessageType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BagOfFeathersItem extends Item implements UseWithContextItem {
    private final Block block;

    private static final float AIR_PLACE_RANGE = 3.0F;
    private static final float MAX_AIR_PLACE_RANGE = 5.0F;

    public BagOfFeathersItem(Block block, Settings settings) {
        super(settings);
        this.block = block;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        return place(new ItemPlacementContext(ctx));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        BlockHitResult hit = useHit.get();
        if (!world.isClient) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) user;
            BlockPos pos = hit.getBlockPos();
            Direction direction = hit.getSide();
            ServerWorld serverWorld = (ServerWorld) world;
            MinecraftServer server = world.getServer();
            ActionResult result;
            if (pos.getY() >= server.getWorldHeight() - 1 && (direction == Direction.UP || pos.getY() >= server.getWorldHeight())) {
                Text text = new TranslatableText("build.tooHigh", server.getWorldHeight()).formatted(Formatting.RED);
                serverPlayer.networkHandler.sendPacket(new GameMessageS2CPacket(text, MessageType.GAME_INFO, Util.NIL_UUID));
                result = ActionResult.FAIL;
            } else if (serverPlayer.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < MAX_AIR_PLACE_RANGE * MAX_AIR_PLACE_RANGE && serverWorld.canPlayerModifyAt(serverPlayer, pos)) {
                ItemPlacementContext ctx = new ItemPlacementContext(new ItemUsageContext(user, hand, hit));
                result = place(ctx);
            } else {
                result = ActionResult.FAIL;
            }

            serverPlayer.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, hit.getBlockPos()));
            serverPlayer.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, hit.getBlockPos().offset(hit.getSide())));

            return new TypedActionResult<>(result, user.getStackInHand(hand));
        } else {
            ItemPlacementContext ctx = new ItemPlacementContext(new ItemUsageContext(user, hand, hit));
            return new TypedActionResult<>(place(ctx), user.getStackInHand(hand));
        }
    }

    private ActionResult place(ItemPlacementContext ctx) {
        if (!ctx.canPlace()) return ActionResult.FAIL;

        BlockState state = block.getPlacementState(ctx);
        if (state == null) return ActionResult.FAIL;

        if (!canPlace(ctx, state)) return ActionResult.FAIL;

        BlockPos blockPos = ctx.getBlockPos();
        World world = ctx.getWorld();
        if (!ctx.getWorld().setBlockState(ctx.getBlockPos(), state)) return ActionResult.FAIL;

        PlayerEntity playerEntity = ctx.getPlayer();
        ItemStack itemStack = ctx.getStack();
        BlockState writtenState = world.getBlockState(blockPos);
        Block writtenBlock = writtenState.getBlock();
        if (writtenBlock == state.getBlock()) {
            writtenBlock.onPlaced(world, blockPos, writtenState, playerEntity, itemStack);
            if (playerEntity instanceof ServerPlayerEntity) {
                Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)playerEntity, blockPos, itemStack);
            }
        }

        BlockSoundGroup blockSoundGroup = writtenState.getSoundGroup();
        world.playSound(playerEntity, blockPos, writtenState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
        return ActionResult.SUCCESS;
    }

    private boolean canPlace(ItemPlacementContext ctx, BlockState state) {
        PlayerEntity playerEntity = ctx.getPlayer();
        ShapeContext entityContext = playerEntity == null ? ShapeContext.absent() : ShapeContext.of(playerEntity);
        return state.canPlaceAt(ctx.getWorld(), ctx.getBlockPos()) && ctx.getWorld().canPlace(state, ctx.getBlockPos(), entityContext);
    }

    private final ThreadLocal<BlockHitResult> useHit = new ThreadLocal<>();

    @Environment(EnvType.CLIENT)
    @Override
    public void prepareClientUse(PacketByteBuf buf, PlayerEntity user, World world, Hand hand) {
        Vec3d forwardVec = new Vec3d(user.getX(), user.getEyeY(), user.getZ()).add(user.getRotationVector().multiply(AIR_PLACE_RANGE));
        Direction direction = Direction.getEntityFacingOrder(user)[0];
        BlockHitResult hit = BlockHitResult.createMissed(forwardVec, direction, new BlockPos(forwardVec));
        useHit.set(hit);
        buf.writeBlockHitResult(hit);
    }

    @Override
    public void prepareServerUse(PacketByteBuf buf, PlayerEntity user, World world, Hand hand) {
        BlockHitResult hit = buf.readBlockHitResult();
        useHit.set(hit);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        World world = target.world;
        if (world instanceof ServerWorld) {
            BagOfFeathers.sendFeatherCloudMessage(target);
        }
        return super.postHit(stack, target, attacker);
    }
}
