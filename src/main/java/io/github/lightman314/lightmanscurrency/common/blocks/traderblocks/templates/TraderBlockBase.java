package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.CapabilityInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.NonNullSupplier;

public abstract class TraderBlockBase extends Block implements ITraderBlock  {

	private final VoxelShape shape;
	
	public TraderBlockBase(Properties properties)
	{
		this(properties, LazyShapes.BOX_T);
	}
	
	public TraderBlockBase(Properties properties, VoxelShape shape)
	{
		super(properties);
		this.shape = shape != null ? shape : LazyShapes.BOX_T;
	}
	
	@Override
	public @Nonnull VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader level, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) { return this.shape; }
	
	protected boolean shouldMakeTrader(BlockState state) { return true; }
	protected abstract TileEntity makeTrader();
	protected TileEntity makeDummy() { return new CapabilityInterfaceBlockEntity(); }

	@Override
	public boolean hasTileEntity(BlockState state) { return true; }

	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, IBlockReader world)
	{
		if(this.shouldMakeTrader(state))
			return this.makeTrader();
		return this.makeDummy();
	}
	
	@Override
	public @Nonnull ActionResultType use(@Nonnull BlockState state, World level, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult result)
	{
		if(!level.isClientSide)
		{
			TileEntity blockEntity = this.getBlockEntity(state, level, pos);
			if(blockEntity instanceof TraderBlockEntity<?>)
			{
				TraderBlockEntity<?> traderSource = (TraderBlockEntity<?>)blockEntity;
				TraderData trader = traderSource.getTraderData();
				if(trader == null)
				{
					LightmansCurrency.LogWarning("Trader Data for block at " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " had to be re-initialized on interaction.");
					player.sendMessage(EasyText.translatable("trader.warning.reinitialized").withStyle(TextFormatting.RED), new UUID(0,0));
					traderSource.initialize(player, ItemStack.EMPTY);
					trader = traderSource.getTraderData();
				}
				if(trader != null) //Open the trader menu
				{
					if(trader.shouldAlwaysShowOnTerminal())
						trader.openStorageMenu(player);
					else
						trader.openTraderMenu(player);
				}

			}
		}
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public void setPlacedBy(@Nonnull World level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity entity, @Nonnull ItemStack stack)
	{
		this.setPlacedByBase(level, pos, state, entity, stack);
	}
	
	public final void setPlacedByBase(World level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack)
	{
		if(!level.isClientSide && entity instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity)entity;
			TileEntity blockEntity = this.getBlockEntity(state, level, pos);
			if(blockEntity instanceof TraderBlockEntity<?>)
			{
				TraderBlockEntity<?> traderSource = (TraderBlockEntity<?>)blockEntity;
				traderSource.initialize(player, stack);
			}
			else
			{
				LightmansCurrency.LogError("Trader Block returned block entity of type '" + (blockEntity == null ? "null" : blockEntity.getClass().getName()) + "' when placing the block.");
			}
		}
	}
	
	@Override
	public void playerWillDestroy(@Nonnull World level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull PlayerEntity player)
	{
		this.playerWillDestroyBase(level, pos, state, player);
	}
	
	public final void playerWillDestroyBase(World level, BlockPos pos, BlockState state, PlayerEntity player)
	{
		TileEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof TraderBlockEntity<?>)
		{
			TraderBlockEntity<?> traderSource = (TraderBlockEntity<?>)blockEntity;
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
	public void onRemove(BlockState state, @Nonnull World level, @Nonnull BlockPos pos, BlockState newState, boolean flag) {
		
		//Ignore if the block is the same.
		if(state.getBlock() == newState.getBlock())
		    return;
		
		if(!level.isClientSide)
		{
			TileEntity blockEntity = this.getBlockEntity(state, level, pos);
			if(blockEntity instanceof TraderBlockEntity<?>)
			{
				TraderBlockEntity<?> traderSource = (TraderBlockEntity<?>)blockEntity;
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
	
	protected void onInvalidRemoval(BlockState state, World level, BlockPos pos, TraderData trader) {}
	
	public boolean canEntityDestroy(BlockState state, IBlockReader level, BlockPos pos, Entity entity) { return false; }
	
	@Override
	public TileEntity getBlockEntity(BlockState state, IWorld level, BlockPos pos) {
		return level == null ? null : level.getBlockEntity(pos);
	}
	
	protected NonNullSupplier<List<ITextComponent>> getItemTooltips() { return ArrayList::new; }
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, this.getItemTooltips());
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
}