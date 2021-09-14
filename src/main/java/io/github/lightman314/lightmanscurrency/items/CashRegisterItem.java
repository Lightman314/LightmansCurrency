package io.github.lightman314.lightmanscurrency.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.ITraderBlock;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraftforge.common.util.Constants;

public class CashRegisterItem extends BlockItem{
		
	private static final SoundEvent soundEffect = SoundEvents.EXPERIENCE_ORB_PICKUP;
	
	public CashRegisterItem(Block block, Properties properties)
	{
		super(block, properties);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		
		BlockPos lookPos = context.getClickedPos();
		Level level = context.getLevel();
		if(lookPos != null)
		{
			if(level.getBlockState(lookPos).getBlock() instanceof ITraderBlock)
			{
				ITraderBlock block = (ITraderBlock)level.getBlockState(lookPos).getBlock();
				BlockEntity tileEntity = block.getTileEntity(level.getBlockState(lookPos), level, lookPos);
				if(!HasEntity(context.getItemInHand(), tileEntity) && tileEntity instanceof TraderBlockEntity)
				{
					AddEntity(context.getItemInHand(), tileEntity);
					
					if(level.isClientSide)
					{
						//CurrencyMod.LOGGER.info("Client-test");
						level.playSound(context.getPlayer(), tileEntity.getBlockPos(), soundEffect, SoundSource.NEUTRAL, 1f, 0f);
					}
					
					return InteractionResult.SUCCESS;
				}
				else if(tileEntity instanceof TraderBlockEntity) //Return even if we have the entity to prevent any accidental placements.
				{
					if(level.isClientSide)
					{
						//CurrencyMod.LOGGER.info("Client-test");
						level.playSound(context.getPlayer(), tileEntity.getBlockPos(), soundEffect, SoundSource.NEUTRAL, 1f, 1.35f);
					}
					return InteractionResult.SUCCESS;
				}
			}
		}
		
		return super.useOn(context);
		
	}
	
	private boolean HasEntity(ItemStack stack, BlockEntity blockEntity)
	{
		
		//Get the tag
		CompoundTag tag = stack.getOrCreateTag();
		
		if(!tag.contains("TraderPos"))
			return false;
		
		ListTag storageList = tag.getList("TraderPos", Constants.NBT.TAG_COMPOUND);
		
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
		
		//If the tag contains the TraderPos list, get it. Otherwise create a new list
		ListTag storageList;
		if(tag.contains("TraderPos"))
			storageList = tag.getList("TraderPos", Constants.NBT.TAG_COMPOUND);
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
		List<BlockPos> positions = new ArrayList<BlockPos>();
		
		//Get the tag
		if(!stack.hasTag())
			return positions;
		
		CompoundTag tag = stack.getTag();
		if(tag.contains("TraderPos"))
		{
			ListTag list = tag.getList("TraderPos", Constants.NBT.TAG_COMPOUND);
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
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack,  worldIn,  tooltip,  flagIn);
		List<BlockPos> data = this.readNBT(stack);
		
		tooltip.add(new TranslatableComponent("tooptip.lightmanscurrency.cash_register", data.size()));
		
		if(!Screen.hasShiftDown() || data.size() <= 0)
			tooltip.add(new TranslatableComponent("tooptip.lightmanscurrency.cash_register.instructions"));
		
		if(Screen.hasShiftDown())
		{
			//Display details of the 
			for(int i = 0; i < data.size(); i++)
			{
				tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.cash_register.details", i + 1, data.get(i).getX(), data.get(i).getY(), data.get(i).getZ()));
			}
		}
		else if(data.size() > 0)
		{
			tooltip.add(new TranslatableComponent("tooptip.lightmanscurrency.cash_register.holdshift"));
		}
	}
	
}
