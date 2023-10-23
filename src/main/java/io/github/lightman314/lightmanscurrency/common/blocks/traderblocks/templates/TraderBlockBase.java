package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.CapabilityInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IDeprecatedBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IEasyEntityBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockEntityValidator;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.NonNullSupplier;

public abstract class TraderBlockBase extends Block implements ITraderBlock, IEasyEntityBlock {

	private final VoxelShape shape;

	public TraderBlockBase(Properties properties) { this(properties, LazyShapes.BOX_T); }

	public TraderBlockBase(Properties properties, VoxelShape shape)
	{
		super(properties);
		this.shape = shape != null ? shape : LazyShapes.BOX_T;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) { return this.shape; }

	protected boolean shouldMakeTrader(BlockState state) { return true; }
	protected abstract BlockEntity makeTrader(BlockPos pos, BlockState state);
	protected BlockEntity makeDummy(BlockPos pos, BlockState state) { return new CapabilityInterfaceBlockEntity(pos, state); }
	protected abstract BlockEntityType<?> traderType();
	protected List<BlockEntityType<?>> validTraderTypes() { return ImmutableList.of(this.traderType()); }

	@Override
	public Collection<BlockEntityType<?>> getAllowedTypes() { return this.validTraderTypes(); }

	@Override
	public final BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
		if(this.shouldMakeTrader(state))
			return this.makeTrader(pos, state);
		return this.makeDummy(pos, state);
	}
	@Override
	@Nonnull
	@SuppressWarnings("deprecation")
	public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
			if(blockEntity instanceof TraderBlockEntity<?> traderSource)
			{
				TraderData trader = traderSource.getTraderData();
				if(trader == null)
				{
					LightmansCurrency.LogWarning("Trader Data for block at " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " had to be re-initialized on interaction.");
					player.sendSystemMessage(Component.translatable("trader.warning.reinitialized").withStyle(ChatFormatting.RED));
					traderSource.initialize(player, ItemStack.EMPTY);
					trader = traderSource.getTraderData();
				}
				if(trader != null) //Open the trader menu
				{
					if(trader.shouldAlwaysShowOnTerminal())
						trader.openStorageMenu(player, BlockEntityValidator.of(traderSource));
					else
						trader.openTraderMenu(player, BlockEntityValidator.of(traderSource));
				}

			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
	{
		this.setPlacedByBase(level, pos, state, player, stack);
	}

	public final void setPlacedByBase(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack)
	{
		if(!level.isClientSide && entity instanceof Player player)
		{
			BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
			if(blockEntity instanceof TraderBlockEntity<?> traderSource)
			{
				traderSource.initialize(player, stack);
			}
			else
			{
				LightmansCurrency.LogError("Trader Block returned block entity of type '" + (blockEntity == null ? "null" : blockEntity.getClass().getName()) + "' when placing the block.");
			}
		}
	}

	@Override
	public void playerWillDestroy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player)
	{
		this.playerWillDestroyBase(level, pos, state, player);
	}

	public final void playerWillDestroyBase(Level level, BlockPos pos, BlockState state, Player player)
	{
		BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof TraderBlockEntity<?> traderSource)
		{
			if(!traderSource.canBreak(player))
				return;
			else
			{
				traderSource.flagAsLegitBreak();
				TraderData trader = traderSource.getTraderData();
				if(trader != null)
					InventoryUtil.dumpContents(level, pos, trader.getContents(level, pos, state, !player.isCreative()));
			}
		}
		else
		{
			LightmansCurrency.LogError("Trader Block returned block entity of type '" + (blockEntity == null ? "null" : blockEntity.getClass().getName()) + "' when destroying the block.");
		}
		super.playerWillDestroy(level, pos, state, player);
	}


	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, BlockState newState, boolean flag) {

		//Ignore if the block is the same.
		if(state.is(newState.getBlock()))
		{
			super.onRemove(state, level, pos, newState, flag);
			return;
		}

		if(state.getBlock() instanceof IDeprecatedBlock db && db.acceptableReplacementState(newState))
			return;

		if(!level.isClientSide)
		{
			BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
			if(blockEntity instanceof TraderBlockEntity<?> traderSource)
			{
				if(!traderSource.legitimateBreak())
				{
					traderSource.flagAsLegitBreak();
					TraderData trader = traderSource.getTraderData();
					if(trader != null)
					{
						LightmansCurrency.LogError("Trader block at " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " was broken by illegal means!");
						LightmansCurrency.LogError("Activating emergency eject protocol.");

						EjectionData data = EjectionData.create(level, pos, state, trader);
						EjectionSaveData.HandleEjectionData(level, pos, data);
					}
					//Remove the rest of the multi-block structure.
					try {
						this.onInvalidRemoval(state, level, pos, trader);
					} catch(Throwable t) { t.printStackTrace(); }
				}
				else
					LightmansCurrency.LogInfo("Trader block was broken by legal means!");

				//Flag the block as broken, so that the trader gets deleted.
				traderSource.onBreak();
			}
		}

		super.onRemove(state, level, pos, newState, flag);
	}

	protected abstract void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderData trader);

	public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) { return false; }

	@Override
	public BlockEntity getBlockEntity(BlockState state, LevelAccessor level, BlockPos pos) { return level == null ? null : level.getBlockEntity(pos); }

	protected NonNullSupplier<List<Component>> getItemTooltips() { return ArrayList::new; }

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, this.getItemTooltips());
		super.appendHoverText(stack, level, tooltip, flagIn);
	}

	protected static void replaceTraderBlock(Level level, BlockPos pos, BlockState newState) { level.setBlock(pos, newState, 35); }

}