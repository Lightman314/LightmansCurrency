package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IEasyEntityBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public class CoinChestBlock extends RotatableBlock implements IEasyEntityBlock, IOwnableBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public CoinChestBlock(Properties properties) { super(properties, SHAPE); this.registerDefaultState(this.getStateDefinition().any().setValue(WATERLOGGED, false)); }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Nonnull
    @Override
    public RenderShape getRenderShape(@Nonnull BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return super.getStateForPlacement(context).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity player, @Nonnull ItemStack stack) {
        if(level.getBlockEntity(pos) instanceof CoinChestBlockEntity be)
        {
            if(stack.hasCustomHoverName())
                be.setCustomName(stack.getHoverName());
        }
        super.setPlacedBy(level, pos, state, player, stack);
    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult result) {
        if(level.getBlockEntity(pos) instanceof CoinChestBlockEntity be && player instanceof ServerPlayer sp)
        {
            if(be.allowAccess(player))
            {
                NetworkHooks.openScreen(sp, CoinChestBlockEntity.getMenuProvider(be), pos);
                PiglinAi.angerNearbyPiglins(player, true);
            }
            else
                player.sendSystemMessage(EasyText.translatable("tooltip.lightmanscurrency.upgrade.coin_chest.protection.warning").withStyle(ChatFormatting.GOLD));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void playerWillDestroy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player) {
        if (level.getBlockEntity(pos) instanceof CoinChestBlockEntity be)
        {
            if(be.allowAccess(player))
                be.onValidBlockRemoval();
            else
                return;
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean flag) {
        if(level.getBlockEntity(pos) instanceof CoinChestBlockEntity be)
        {
            be.onBlockRemoval();
            Containers.dropContents(level, pos, be.getStorage());
            Containers.dropContents(level, pos, be.getUpgrades());
        }
        super.onRemove(state, level, pos, newState, flag);
    }

    @Nonnull
    @Override
    public Collection<BlockEntityType<?>> getAllowedTypes() { return Collections.singleton(ModBlockEntities.COIN_CHEST.get()); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) { return new CoinChestBlockEntity(pos, state); }

    @Override
    public void tick(@Nonnull BlockState state, @Nonnull ServerLevel level, @Nonnull BlockPos pos, @Nonnull RandomSource random) {
        super.tick(state, level, pos, random);
        if(level.getBlockEntity(pos) instanceof CoinChestBlockEntity be)
            be.recheckOpen();
    }

    @Override
    public boolean triggerEvent(@Nonnull BlockState p_49226_, @Nonnull Level p_49227_, @Nonnull BlockPos p_49228_, int p_49229_, int p_49230_) {
        super.triggerEvent(p_49226_, p_49227_, p_49228_, p_49229_, p_49230_);
        BlockEntity blockentity = p_49227_.getBlockEntity(p_49228_);
        return blockentity != null && blockentity.triggerEvent(p_49229_, p_49230_);
    }


    @Override
    public boolean canBreak(@Nonnull Player player, @Nonnull LevelAccessor level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        if(level.getBlockEntity(pos) instanceof CoinChestBlockEntity blockEntity)
            return blockEntity.allowAccess(player);
        return true;
    }
}
