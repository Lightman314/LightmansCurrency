package io.github.lightman314.lightmanscurrency.common.blocks.tradeinterface.templates;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.NonNullSupplier;

public abstract class TraderInterfaceBlock extends RotatableBlock implements IOwnableBlock {

	protected TraderInterfaceBlock(Properties properties) { super(properties); }
	
	@Override
	public @Nonnull ActionResultType use(@Nonnull BlockState state, World level, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult result)
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
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public void setPlacedBy(World level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
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
	public void playerWillDestroy(@Nonnull World level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull PlayerEntity player)
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
	public void onRemove(BlockState state, @Nonnull World level, @Nonnull BlockPos pos, BlockState newState, boolean flag) {
		
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
	
	protected abstract void onInvalidRemoval(BlockState state, World level, BlockPos pos, TraderInterfaceBlockEntity trader);

	@Override
	public boolean canBreak(PlayerEntity player, IWorld level, BlockPos pos, BlockState state) {
		TraderInterfaceBlockEntity be = this.getBlockEntity(level, pos, state);
		if(be == null)
			return true;
		return be.isOwner(player);
	}

	@Override
	public boolean hasTileEntity(BlockState state) { return true; }

	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, IBlockReader world) { return this.createBlockEntity(state); }
	
	protected abstract TileEntity createBlockEntity(BlockState state);

	protected final TraderInterfaceBlockEntity getBlockEntity(IWorld level, BlockPos pos, BlockState ignored) {
		TileEntity be = level.getBlockEntity(pos);
		if(be instanceof TraderInterfaceBlockEntity)
			return (TraderInterfaceBlockEntity)be;
		return null;
	}
	
	protected NonNullSupplier<List<ITextComponent>> getItemTooltips() { return ArrayList::new; }
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, this.getItemTooltips());
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
	@Override
	public boolean isSignalSource(@Nonnull BlockState state) { return true; }
	
	public ItemStack getDropBlockItem(BlockState state, TraderInterfaceBlockEntity traderInterface) { return new ItemStack(state.getBlock()); }
	
}