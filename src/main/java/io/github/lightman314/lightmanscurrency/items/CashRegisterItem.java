package io.github.lightman314.lightmanscurrency.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class CashRegisterItem extends BlockItem{
		
	private static final SoundEvent soundEffect = new SoundEvent(new ResourceLocation("minecraft","entity.experience_orb.pickup"));
	
	public CashRegisterItem(Block block, Properties properties)
	{
		super(block, properties);
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		
		BlockPos lookPos = context.getPos();
		World world = context.getWorld();
		if(lookPos != null)
		{
			if(world.getBlockState(lookPos).getBlock() instanceof ITraderBlock)
			{
				ITraderBlock block = (ITraderBlock)world.getBlockState(lookPos).getBlock();
				TileEntity tileEntity = block.getTileEntity(world.getBlockState(lookPos), world, lookPos);
				if(!HasEntity(context.getItem(), tileEntity) && tileEntity instanceof TraderTileEntity)
				{
					AddEntity(context.getItem(), tileEntity);
					
					if(world.isRemote)
					{
						//CurrencyMod.LOGGER.info("Client-test");
						world.playSound(context.getPlayer(), tileEntity.getPos(), soundEffect, SoundCategory.NEUTRAL, 1f, 0f);
					}
					
					return ActionResultType.SUCCESS;
				}
				else if(tileEntity instanceof TraderTileEntity) //Return even if we have the entity to prevent any accidental placements.
				{
					if(world.isRemote)
					{
						//CurrencyMod.LOGGER.info("Client-test");
						world.playSound(context.getPlayer(), tileEntity.getPos(), soundEffect, SoundCategory.NEUTRAL, 1f, 1.35f);
					}
					return ActionResultType.SUCCESS;
				}
			}
		}
		
		return super.onItemUse(context);
		
	}
	
	private boolean HasEntity(ItemStack stack, TileEntity tileEntity)
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
				if(thisEntry.getInt("x") == tileEntity.getPos().getX() && thisEntry.getInt("y") == tileEntity.getPos().getY() && thisEntry.getInt("z") == tileEntity.getPos().getZ())
					return true;
			}
		}
		
		return false;
		
	}
	
	private void AddEntity(ItemStack stack, TileEntity tileEntity)
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
		newEntry.putInt("x", tileEntity.getPos().getX());
		newEntry.putInt("y", tileEntity.getPos().getY());
		newEntry.putInt("z", tileEntity.getPos().getZ());
		
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
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack,  worldIn,  tooltip,  flagIn);
		List<BlockPos> data = this.readNBT(stack);
		
		tooltip.add(new TranslationTextComponent("tooptip.lightmanscurrency.cash_register", data.size()));
		
		if(!Screen.hasShiftDown() || data.size() <= 0)
			tooltip.add(new TranslationTextComponent("tooptip.lightmanscurrency.cash_register.instructions"));
		
		if(Screen.hasShiftDown())
		{
			//Display details of the 
			for(int i = 0; i < data.size(); i++)
			{
				tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.cash_register.details", i + 1, data.get(i).getX(), data.get(i).getY(), data.get(i).getZ()));
			}
		}
		else if(data.size() > 0)
		{
			tooltip.add(new TranslationTextComponent("tooptip.lightmanscurrency.cash_register.holdshift"));
		}
	}
	
}
