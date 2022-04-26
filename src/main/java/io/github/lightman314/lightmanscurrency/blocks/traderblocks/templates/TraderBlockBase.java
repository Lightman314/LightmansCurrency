package io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.DummyBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TickableBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.blocks.util.TickerUtil;
import io.github.lightman314.lightmanscurrency.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.NonNullSupplier;

public abstract class TraderBlockBase extends Block implements ITraderBlock, EntityBlock {

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
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return this.shape;
	}
	
	protected boolean shouldMakeTrader(BlockState state) { return true; }
	protected abstract BlockEntity makeTrader(BlockPos pos, BlockState state);
	protected BlockEntity makeDummy(BlockPos pos, BlockState state) { return new DummyBlockEntity(pos, state); }
	protected abstract BlockEntityType<?> traderType();
	
	@Nullable 
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return TickerUtil.createTickerHelper(type, this.traderType(), TickableBlockEntity::tickHandler);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		if(this.shouldMakeTrader(state))
			return this.makeTrader(pos, state);
		return this.makeDummy(pos, state);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
			if(blockEntity instanceof TraderBlockEntity)
			{
				TraderBlockEntity trader = (TraderBlockEntity)blockEntity;
				trader.getCoreSettings().updateNames(player);
				//Send update packet for safety, and open the menu
				BlockEntityUtil.sendUpdatePacket(blockEntity);
				trader.openTradeMenu(player);
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		this.setPlacedByBase(level, pos, state, player, stack);
	}
	
	public final void setPlacedByBase(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!level.isClientSide)
		{
			BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
			if(blockEntity instanceof TraderBlockEntity)
			{
				TraderBlockEntity trader = (TraderBlockEntity)blockEntity;
				trader.initOwner(PlayerReference.of(player));
				if(stack.hasCustomHoverName())
					trader.getCoreSettings().setCustomName(null, stack.getHoverName().getString());
			}
			else
			{
				LightmansCurrency.LogError("Trader Block returned block entity of type '" + (blockEntity == null ? "null" : blockEntity.getClass().getName()) + "' when placing the block.");
			}
		}
	}
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		this.playerWillDestroyBase(level, pos, state, player);
	}
	
	public final void playerWillDestroyBase(Level level, BlockPos pos, BlockState state, Player player)
	{
		BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof TraderBlockEntity)
		{
			TraderBlockEntity trader = (TraderBlockEntity)blockEntity;
			if(!trader.canBreak(player))
				return;
			else
				trader.dumpContents(level, pos);
		}
		else
		{
			LightmansCurrency.LogError("Trader Block returned block entity of type '" + (blockEntity == null ? "null" : blockEntity.getClass().getName()) + "' when destroying the block.");
		}
		super.playerWillDestroy(level, pos, state, player);
	}
	
	@Override
	public BlockEntity getBlockEntity(BlockState state, LevelAccessor level, BlockPos pos) {
		return level.getBlockEntity(pos);
	}
	
	protected NonNullSupplier<List<Component>> getItemTooltips() { return () -> new ArrayList<>(); }

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, this.getItemTooltips());
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
}
