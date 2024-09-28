package io.github.lightman314.lightmanscurrency.common.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.ITraderBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CashRegisterItem extends BlockItem{
		
	private static final SoundEvent soundEffect = SoundEvents.EXPERIENCE_ORB_PICKUP;
	
	public CashRegisterItem(Block block, Properties properties)
	{
		super(block, properties.stacksTo(1));
	}
	
	@Override
	@Nonnull
	public InteractionResult useOn(UseOnContext context) {
		
		BlockPos lookPos = context.getClickedPos();
		Level level = context.getLevel();
		if(level.getBlockState(lookPos).getBlock() instanceof ITraderBlock block)
		{
			BlockEntity blockEntity = block.getBlockEntity(level.getBlockState(lookPos), level, lookPos);
			if(!HasEntity(context.getItemInHand(), blockEntity) && blockEntity instanceof TraderBlockEntity)
			{
				AddEntity(context.getItemInHand(), blockEntity);

				if(level.isClientSide)
				{
					level.playSound(context.getPlayer(), blockEntity.getBlockPos(), soundEffect, SoundSource.NEUTRAL, 1f, 0f);
				}

				return InteractionResult.SUCCESS;
			}
			else if(blockEntity instanceof TraderBlockEntity) //Return even if we have the entity to prevent any accidental placements.
			{
				if(level.isClientSide)
				{
					level.playSound(context.getPlayer(), blockEntity.getBlockPos(), soundEffect, SoundSource.NEUTRAL, 1f, 1.35f);
				}
				return InteractionResult.SUCCESS;
			}
		}

		return super.useOn(context);
		
	}
	
	private boolean HasEntity(ItemStack stack, BlockEntity blockEntity)
	{
		
		//Get the tag
		if(!stack.hasTag())
			return false;
		
		CompoundTag tag = stack.getTag();

		assert tag != null;
		if(!tag.contains("TraderPos"))
			return false;
		
		ListTag storageList = tag.getList("TraderPos", Tag.TAG_COMPOUND);
		
		for(int i = 0; i < storageList.size(); i++)
		{
			CompoundTag thisEntry = storageList.getCompound(i);
			if(thisEntry.contains("x") && thisEntry.contains("y") && thisEntry.contains("z"))
			{
				if(thisEntry.getInt("x") == blockEntity.getBlockPos().getX() && thisEntry.getInt("y") == blockEntity.getBlockPos().getY() && thisEntry.getInt("z") == blockEntity.getBlockPos().getZ())
					return true;
			}
		}
		
		return false;
		
	}
	
	private void AddEntity(ItemStack stack, BlockEntity blockEntity)
	{
		//Get the tag
		CompoundTag tag = stack.getOrCreateTag();
		
		//If the tag contains the TraderPos list, get it, otherwise create a new list
		ListTag storageList;
		if(tag.contains("TraderPos"))
			storageList = tag.getList("TraderPos", Tag.TAG_COMPOUND);
		else
			storageList = new ListTag();
		
		//Create the new entry to the list
		CompoundTag newEntry = new CompoundTag();
		newEntry.putInt("x", blockEntity.getBlockPos().getX());
		newEntry.putInt("y", blockEntity.getBlockPos().getY());
		newEntry.putInt("z", blockEntity.getBlockPos().getZ());
		
		//Add the new entry to the list
		storageList.add(newEntry);
		
		//Put the modified list into the tag
		tag.put("TraderPos", storageList);
		
	}
	
	private List<BlockPos> readNBT(ItemStack stack)
	{
		List<BlockPos> positions = new ArrayList<>();
		
		//Get the tag
		if(!stack.hasTag())
			return positions;
		
		CompoundTag tag = stack.getTag();
		assert tag != null;
		if(tag.contains("TraderPos"))
		{
			ListTag list = tag.getList("TraderPos", Tag.TAG_COMPOUND);
			for(int i = 0; i < list.size(); i++)
			{
				CompoundTag thisPos = list.getCompound(i);
				if(thisPos.contains("x") && thisPos.contains("y") && thisPos.contains("z"))
				{
					positions.add(new BlockPos(thisPos.getInt("x"),thisPos.getInt("y"),thisPos.getInt("z")));
				}
			}
		}
		
		return positions;
		
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		super.appendHoverText(stack,  level,  tooltip,  flagIn);
		List<BlockPos> data = this.readNBT(stack);

		tooltip.addAll(LCText.TOOLTIP_CASH_REGISTER.get());

		tooltip.add(LCText.TOOLTIP_CASH_REGISTER_INFO.get(data.size()));
		
		if(!Screen.hasShiftDown() || data.isEmpty())
			tooltip.add(LCText.TOOLTIP_CASH_REGISTER_INSTRUCTIONS.get());
		
		if(Screen.hasShiftDown())
		{
			//Display details of the registered blocks
			for(int i = 0; i < data.size(); i++)
			{
				BlockPos pos = data.get(i);
				tooltip.add(LCText.TOOLTIP_CASH_REGISTER_DETAILS.get(i + 1,pos.getX(), pos.getY(), pos.getZ()));
			}
		}
		else if(!data.isEmpty())
			tooltip.add(LCText.TOOLTIP_CASH_REGISTER_HOLD_SHIFT.get().withStyle(ChatFormatting.YELLOW));
	}
	
}
