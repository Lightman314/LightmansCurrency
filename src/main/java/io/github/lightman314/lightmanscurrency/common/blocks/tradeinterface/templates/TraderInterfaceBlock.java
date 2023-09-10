package io.github.lightman314.lightmanscurrency.common.blocks.tradeinterface.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IEasyEntityBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.NonNullSupplier;

public abstract class TraderInterfaceBlock extends RotatableBlock implements IEasyEntityBlock, IOwnableBlock {

	protected TraderInterfaceBlock(Properties properties) { super(properties); }

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult result)
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
	public void setPlacedBy(Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
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
	public void playerWillDestroy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player)
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
	public void onRemove(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, BlockState newState, boolean flag) {

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
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
		return this.createBlockEntity(pos, state);
	}

	protected abstract BlockEntity createBlockEntity(BlockPos pos, BlockState state);
	protected abstract BlockEntityType<?> interfaceType();

	@Override
	public Collection<BlockEntityType<?>> getAllowedTypes() { return ImmutableList.of(this.interfaceType()); }

	protected final TraderInterfaceBlockEntity getBlockEntity(LevelAccessor level, BlockPos pos, BlockState state) {
		BlockEntity be = level.getBlockEntity(pos);
		if(be instanceof TraderInterfaceBlockEntity tibe)
			return tibe;
		return null;
	}

	protected NonNullSupplier<List<Component>> getItemTooltips() { return ArrayList::new; }

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, this.getItemTooltips());
		super.appendHoverText(stack, level, tooltip, flagIn);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isSignalSource(@Nonnull BlockState state) { return true; }

	public ItemStack getDropBlockItem(BlockState state, TraderInterfaceBlockEntity traderInterface) { return new ItemStack(state.getBlock()); }

}