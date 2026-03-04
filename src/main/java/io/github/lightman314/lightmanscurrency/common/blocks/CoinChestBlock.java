package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeableBlock;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IEasyEntityBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

public class CoinChestBlock extends RotatableBlock implements IEasyEntityBlock, IOwnableBlock, IUpgradeableBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public CoinChestBlock(Properties properties) { super(properties, SHAPE); this.registerDefaultState(this.getStateDefinition().any().setValue(WATERLOGGED, false)); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return super.getStateForPlacement(context).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity player, ItemStack stack) {
        if(level.getBlockEntity(pos) instanceof CoinChestBlockEntity be)
        {
            if(stack.has(DataComponents.CUSTOM_NAME))
                be.setCustomName(stack.getHoverName());
        }
        super.setPlacedBy(level, pos, state, player, stack);
    }

    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if(!level.isClientSide && level.getBlockEntity(pos) instanceof CoinChestBlockEntity be)
        {
            if(be.allowAccess(player))
            {
                player.openMenu(CoinChestBlockEntity.getMenuProvider(be),pos);
                PiglinAi.angerNearbyPiglins(player, true);
            }
            else
                player.sendSystemMessage(LCText.MESSAGE_COIN_CHEST_PROTECTION_WARNING.get().withStyle(ChatFormatting.GOLD));
        }
        return InteractionResult.CONSUME;
    }

    
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof CoinChestBlockEntity be)
        {
            if(be.allowAccess(player))
                be.onValidBlockRemoval();
            else
                return state;
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean flag) {
        if(level.getBlockEntity(pos) instanceof CoinChestBlockEntity be)
        {
            be.onBlockRemoval();
            Containers.dropContents(level,pos,be.getStorage());
            InventoryUtil.dropContents(level,pos,be.getUpgrades());
        }
        super.onRemove(state, level, pos, newState, flag);
    }

    
    @Override
    public Collection<BlockEntityType<?>> getAllowedTypes() { return Collections.singleton(ModBlockEntities.COIN_CHEST.get()); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CoinChestBlockEntity(pos, state); }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if(level.getBlockEntity(pos) instanceof CoinChestBlockEntity be)
            be.recheckOpen();
    }

    @Override
    public boolean triggerEvent(BlockState p_49226_, Level p_49227_, BlockPos p_49228_, int p_49229_, int p_49230_) {
        super.triggerEvent(p_49226_, p_49227_, p_49228_, p_49229_, p_49230_);
        BlockEntity blockentity = p_49227_.getBlockEntity(p_49228_);
        return blockentity != null && blockentity.triggerEvent(p_49229_, p_49230_);
    }


    @Override
    public boolean canBreak(Player player, LevelAccessor level, BlockPos pos, BlockState state) {
        if(level.getBlockEntity(pos) instanceof CoinChestBlockEntity blockEntity)
            return blockEntity.allowAccess(player);
        return true;
    }

    @Override
    public boolean canUseUpgradeItem(IUpgradeable upgradeable, ItemStack stack, @Nullable Player player) {
        if(upgradeable instanceof CoinChestBlockEntity be)
            return be.allowAccess(player);
        return false;
    }

}
