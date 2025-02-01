package io.github.lightman314.lightmanscurrency.api.traders.blocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.SafeEjectionAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderState;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeableBlock;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.CapabilityInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IEasyEntityBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.blocks.EasyBlock;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.TraderEjectionData;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockEntityValidator;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class TraderBlockBase extends EasyBlock implements ITraderBlock, IEasyEntityBlock, IUpgradeableBlock {

	private final VoxelShape shape;
	
	public TraderBlockBase(Properties properties) { this(properties, LazyShapes.BOX); }
	
	public TraderBlockBase(Properties properties, VoxelShape shape)
	{
		super(properties);
		this.shape = shape != null ? shape : LazyShapes.BOX;
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

	@Nonnull
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
					player.sendSystemMessage(LCText.MESSAGE_TRADER_WARNING_MISSING_DATA.getWithStyle(ChatFormatting.RED));
					traderSource.initialize(player, ItemStack.EMPTY);
					trader = traderSource.getTraderData();
				}
				if(trader != null) //Open the trader menu
				{
					if(trader.getState() == TraderState.EJECTED)
					{
						LightmansCurrency.LogWarning("Trader was somehow flagged as ejected, yet it is still present in the world!\nFlagging trader as 'normal'");
						trader.move(level,blockEntity.getBlockPos());
						trader.setState(TraderState.NORMAL);
						//Remove an ejection data for this trader, as it's clearly still here
						for(EjectionData data : SafeEjectionAPI.getApi().getAllData(false))
						{
							if(data instanceof TraderEjectionData d && d.getTraderID() == trader.getID())
								d.delete();
						}
					}
					if(trader.shouldAlwaysShowOnTerminal() && trader.hasPermission(player,Permissions.OPEN_STORAGE))
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
		
		if(!level.isClientSide)
		{
			BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
			if(blockEntity instanceof TraderBlockEntity<?> traderSource)
			{
				if(traderSource.isSelfPickup())
				{
					super.onRemove(state,level,pos,newState,flag);
					return;
				}
				if(!traderSource.legitimateBreak())
				{
					//Flag as pickup so that it doesn't try to destroy the trader data when a multi-block is ejected,
					//as the other block would then be considered a legit break and delete the trader
					traderSource.flagAsPickup();
					TraderData trader = traderSource.getTraderData();
					if(trader != null && trader.getState().shouldEject())
					{
						if(!LCConfig.SERVER.anarchyMode.get())
						{
							LightmansCurrency.LogError("Trader block at " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " was broken by illegal means!");
							LightmansCurrency.LogError("Activating emergency eject protocol.");
						}

						EjectionData data = trader.buildEjectionData(level,pos,state);
						SafeEjectionAPI.getApi().handleEjection(level,pos,data);
						//Do not flag the block as broken here as building the ejection data will flag it as an "Ejected" trader and will not be deleted until/unless split
					}
					//Remove the rest of the multi-block structure.
					this.onInvalidRemoval(state, level, pos, trader);
					this.removeOtherBlocks(level,state,pos);
				}
				else
				{
					LightmansCurrency.LogInfo("Trader block was broken by legal means!");
					//Flag the block as broken, so that the trader gets deleted.
					traderSource.onBreak();
				}
			}
		}
		
		super.onRemove(state, level, pos, newState, flag);
	}

	public final void removeAllBlocks(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockPos pos)
	{
		this.setAir(level,pos,null);
		this.removeOtherBlocks(level,state,pos);
	}

	public void removeOtherBlocks(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockPos pos) {}

	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderData trader) {}
	
	public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) { return false; }
	
	@Nullable
	@Override
	public BlockEntity getBlockEntity(@Nonnull BlockState state, @Nonnull LevelAccessor level, @Nonnull BlockPos pos) { return level.getBlockEntity(pos); }
	
	protected Supplier<List<Component>> getItemTooltips() { return ArrayList::new; }
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, this.getItemTooltips());
		super.appendHoverText(stack, level, tooltip, flagIn);
	}

	protected static void replaceTraderBlock(Level level, BlockPos pos, BlockState newState) { level.setBlock(pos, newState, 35); }

	protected final void setAir(Level level, BlockPos pos, Player player)
	{
		BlockState state = level.getBlockState(pos);
		if(state.getBlock() == this)
		{
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
			if(player != null)
				level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
		}
	}

	@Nullable
	@Override
	public IUpgradeable getUpgradeable(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
		if(this.getBlockEntity(state,level,pos) instanceof IUpgradeableBlockEntity be)
			return be.getUpgradeable();
		return null;
	}

	@Override
	public boolean canUseUpgradeItem(@Nonnull IUpgradeable upgradeable, @Nonnull ItemStack stack, @Nullable Player player) {
		if(upgradeable instanceof TraderData trader)
			return player != null && trader.hasPermission(player, Permissions.OPEN_STORAGE);
		return false;
	}
}
