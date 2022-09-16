package io.github.lightman314.lightmanscurrency.blocks.tradeinterface.templates;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.TickableBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.interfaces.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.blocks.util.TickerUtil;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.NonNullSupplier;

public abstract class TraderInterfaceBlock extends RotatableBlock implements EntityBlock, IOwnableBlock {

	protected TraderInterfaceBlock(Properties properties) { super(properties); }
	
	@Nullable 
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return TickerUtil.createTickerHelper(type, this.interfaceType(), TickableBlockEntity::tickHandler);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			TraderInterfaceBlockEntity blockEntity = this.getBlockEntity(level, pos, state);
			if(blockEntity != null)
			{
				//Send update packet for safety, and open the menu
				BlockEntityUtil.sendUpdatePacket(blockEntity);
				blockEntity.openMenu(player);
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!level.isClientSide)
		{
			TraderInterfaceBlockEntity blockEntity = this.getBlockEntity(level, pos, state);
			if(blockEntity != null)
			{
				blockEntity.initOwner(player);
			}
		}
	}
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		TraderInterfaceBlockEntity blockEntity = this.getBlockEntity(level, pos, state);
		if(blockEntity != null)
		{
			if(!blockEntity.isOwner(player))
				return;
			InventoryUtil.dumpContents(level, pos, blockEntity.getContents(level, pos, state, !player.isCreative()));
			blockEntity.flagAsRemovable();
		}
		super.playerWillDestroy(level, pos, state, player);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean flag) {
		
		//Ignore if the block is the same.
		if(state.getBlock() == newState.getBlock())
		    return;
		
		if(!level.isClientSide)
		{
			TraderInterfaceBlockEntity blockEntity = this.getBlockEntity(level, pos, state);
			if(blockEntity != null)
			{
				if(!blockEntity.allowRemoval())
				{
					LightmansCurrency.LogError("Trader block at " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " was broken by illegal means!");
					LightmansCurrency.LogError("Activating emergency eject protocol.");
					EjectionData data = EjectionData.create(level, pos, state, blockEntity);
					EjectionSaveData.HandleEjectionData(level, pos, data);
					blockEntity.flagAsRemovable();
					//Remove the rest of the multi-block structure.
					try {
						this.onInvalidRemoval(state, level, pos, blockEntity);
					} catch(Throwable t) { t.printStackTrace(); }
				}
				else
					LightmansCurrency.LogInfo("Trader block was broken by legal means!");
			}
		}
		
		super.onRemove(state, level, pos, newState, flag);
	}
	
	protected abstract void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderInterfaceBlockEntity trader);

	@Override
	public boolean canBreak(Player player, LevelAccessor level, BlockPos pos, BlockState state) {
		TraderInterfaceBlockEntity be = this.getBlockEntity(level, pos, state);
		if(be == null)
			return true;
		return be.isOwner(player);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return this.createBlockEntity(pos, state);
	}
	
	protected abstract BlockEntity createBlockEntity(BlockPos pos, BlockState state);
	protected abstract BlockEntityType<?> interfaceType();
	
	protected final TraderInterfaceBlockEntity getBlockEntity(LevelAccessor level, BlockPos pos, BlockState state) {
		BlockEntity be = level.getBlockEntity(pos);
		if(be instanceof TraderInterfaceBlockEntity)
			return (TraderInterfaceBlockEntity)be;
		return null;
	}
	
	protected NonNullSupplier<List<Component>> getItemTooltips() { return () -> new ArrayList<>(); }
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, this.getItemTooltips());
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
	@Override
	public boolean isSignalSource(BlockState state) { return true; }
	
	public ItemStack getDropBlockItem(BlockState state, TraderInterfaceBlockEntity traderInterface) { return new ItemStack(state.getBlock()); }
	
}
