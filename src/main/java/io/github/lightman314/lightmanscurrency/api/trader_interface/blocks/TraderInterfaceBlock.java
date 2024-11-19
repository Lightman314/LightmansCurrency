package io.github.lightman314.lightmanscurrency.api.trader_interface.blocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.SafeEjectionAPI;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IEasyEntityBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.RotatableBlock;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeableBlock;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class TraderInterfaceBlock extends RotatableBlock implements IEasyEntityBlock, IOwnableBlock, IUpgradeableBlock {

	protected TraderInterfaceBlock(Properties properties) { super(properties); }

	@Override
	public @Nonnull InteractionResult useWithoutItem(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			if(QuarantineAPI.IsDimensionQuarantined(level))
				EasyText.sendMessage(player, LCText.MESSAGE_DIMENSION_QUARANTINED_TERMINAL.getWithStyle(ChatFormatting.GOLD));
			else
			{
				TraderInterfaceBlockEntity blockEntity = this.getBlockEntity(level, pos, state);
				if(blockEntity != null)
				{
					//Send update packet for safety, and open the menu
					BlockEntityUtil.sendUpdatePacket(blockEntity);
					blockEntity.openMenu(player);
				}
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
	
	@Nonnull
	@Override
	public BlockState playerWillDestroy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player)
	{
		TraderInterfaceBlockEntity blockEntity = this.getBlockEntity(level, pos, state);
		if(blockEntity != null)
		{
			if(!blockEntity.isOwner(player))
				return state;
			InventoryUtil.dumpContents(level, pos, blockEntity.getContents(level, pos, state, !player.isCreative()));
			blockEntity.flagAsRemovable();
		}
		return super.playerWillDestroy(level, pos, state, player);
	}
	
	@Override
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
					EjectionData data = blockEntity.buildEjectionData(level,pos,state);
					SafeEjectionAPI.getApi().handleEjection(level,pos,data);
					blockEntity.flagAsRemovable();
					//Remove the rest of the multi-block structure.
					try {
						this.onInvalidRemoval(state, level, pos, blockEntity);
					} catch(Throwable t) { LightmansCurrency.LogError("Error while triggering invalid removal code!",t); }
				}
				else
					LightmansCurrency.LogInfo("Trader block was broken by legal means!");
			}
		}
		
		super.onRemove(state, level, pos, newState, flag);
	}
	
	protected abstract void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderInterfaceBlockEntity trader);

	@Override
	public boolean canBreak(@Nonnull Player player, @Nonnull LevelAccessor level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
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

	@Nonnull
	@Override
	public Collection<BlockEntityType<?>> getAllowedTypes() { return ImmutableList.of(this.interfaceType()); }

	protected final TraderInterfaceBlockEntity getBlockEntity(LevelAccessor level, BlockPos pos, BlockState state) {
		BlockEntity be = level.getBlockEntity(pos);
		if(be instanceof TraderInterfaceBlockEntity tibe)
			return tibe;
		return null;
	}
	
	protected Supplier<List<Component>> getItemTooltips() { return ArrayList::new; }
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable Item.TooltipContext level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, this.getItemTooltips());
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
	@Override
	public boolean isSignalSource(@Nonnull BlockState state) { return true; }
	
	public ItemStack getDropBlockItem(BlockState state, TraderInterfaceBlockEntity traderInterface) { return new ItemStack(state.getBlock()); }

	@Override
	public boolean canUseUpgradeItem(@Nonnull IUpgradeable upgradeable, @Nonnull ItemStack stack, @Nullable Player player) {
		if(player != null && upgradeable instanceof TraderInterfaceBlockEntity be)
			return be.owner.isMember(player);
		return false;
	}

}
