package io.github.lightman314.lightmanscurrency.common.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class CashRegisterItem extends BlockItem {
		
	private static final SoundEvent soundEffect = new SoundEvent(new ResourceLocation("minecraft","entity.experience_orb.pickup"));
	
	public CashRegisterItem(Block block, Properties properties)
	{
		super(block, properties.stacksTo(1));
	}
	
	@Nonnull
	@Override
	public ActionResultType useOn(ItemUseContext context) {

		BlockPos lookPos = context.getClickedPos();
		World level = context.getLevel();
		if(lookPos != null)
		{
			if(level.getBlockState(lookPos).getBlock() instanceof ITraderBlock)
			{
				ITraderBlock block = (ITraderBlock)level.getBlockState(lookPos).getBlock();
				TileEntity blockEntity = block.getBlockEntity(level.getBlockState(lookPos), level, lookPos);
				if(!HasEntity(context.getItemInHand(), blockEntity) && blockEntity instanceof TraderBlockEntity)
				{
					AddEntity(context.getItemInHand(), blockEntity);
					
					if(level.isClientSide)
					{
						level.playSound(context.getPlayer(), blockEntity.getBlockPos(), soundEffect, SoundCategory.NEUTRAL, 1f, 0f);
					}
					
					return ActionResultType.SUCCESS;
				}
				else if(blockEntity instanceof TraderBlockEntity) //Return even if we have the entity to prevent any accidental placements.
				{
					if(level.isClientSide)
					{
						level.playSound(context.getPlayer(), blockEntity.getBlockPos(), soundEffect, SoundCategory.NEUTRAL, 1f, 1.35f);
					}
					return ActionResultType.SUCCESS;
				}
			}
		}
		
		return super.useOn(context);
		
	}
	
	private boolean HasEntity(ItemStack stack, TileEntity blockEntity)
	{
		
		//Get the tag
		if(!stack.hasTag())
			return false;
		
		CompoundNBT tag = stack.getTag();
		
		if(!tag.contains("TraderPos"))
			return false;
		
		ListNBT storageList = tag.getList("TraderPos", Constants.NBT.TAG_COMPOUND);
		
		for(int i = 0; i < storageList.size(); i++)
		{
			CompoundNBT thisEntry = storageList.getCompound(i);
			if(thisEntry.contains("x") && thisEntry.contains("y") && thisEntry.contains("z"))
			{
				if(thisEntry.getInt("x") == blockEntity.getBlockPos().getX() && thisEntry.getInt("y") == blockEntity.getBlockPos().getY() && thisEntry.getInt("z") == blockEntity.getBlockPos().getZ())
					return true;
			}
		}
		
		return false;
		
	}
	
	private void AddEntity(ItemStack stack, TileEntity blockEntity)
	{
		//Get the tag
		if(!stack.hasTag())
			stack.setTag(new CompoundNBT());
		CompoundNBT tag = stack.getTag();
		
		//If the tag contains the TraderPos list, get it. Otherwise create a new list
		ListNBT storageList;
		if(tag.contains("TraderPos"))
			storageList = tag.getList("TraderPos", Constants.NBT.TAG_COMPOUND);
		else
			storageList = new ListNBT();
		
		//Create the new entry to the list
		CompoundNBT newEntry = new CompoundNBT();
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
		
		CompoundNBT tag = stack.getTag();
		if(tag.contains("TraderPos"))
		{
			ListNBT list = tag.getList("TraderPos", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < list.size(); i++)
			{
				CompoundNBT thisPos = list.getCompound(i);
				if(thisPos.contains("x") && thisPos.contains("y") && thisPos.contains("z"))
				{
					positions.add(new BlockPos(thisPos.getInt("x"),thisPos.getInt("y"),thisPos.getInt("z")));
				}
			}
		}
		
		return positions;
		
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable World level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		super.appendHoverText(stack,  level,  tooltip,  flagIn);
		List<BlockPos> data = this.readNBT(stack);
		
		TooltipItem.addTooltipAlways(tooltip, LCTooltips.CASH_REGISTER);
		
		tooltip.add(EasyText.translatable("tooptip.lightmanscurrency.cash_register", data.size()));
		
		if(!Screen.hasShiftDown() || data.size() == 0)
		{
			tooltip.add(EasyText.translatable("tooptip.lightmanscurrency.cash_register.instructions"));
		}
		
		if(Screen.hasShiftDown())
		{
			//Display details of the 
			for(int i = 0; i < data.size(); i++)
			{
				tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.cash_register.details", i + 1, data.get(i).getX(), data.get(i).getY(), data.get(i).getZ()));
			}
		}
		else if(data.size() > 0)
		{
			tooltip.add(EasyText.translatable("tooptip.lightmanscurrency.cash_register.holdshift").withStyle(TextFormatting.YELLOW));
		}
	}
	
}
