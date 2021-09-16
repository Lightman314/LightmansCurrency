package io.github.lightman314.lightmanscurrency.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

public class ItemStackHelper {
	
	public static CompoundNBT saveAllItems(String key, CompoundNBT tag, NonNullList<ItemStack> list)
	{
		ListNBT listTag = new ListNBT();
		for(int i = 0; i < list.size(); ++i)
		{
			ItemStack stack = list.get(i);
			if(!stack.isEmpty())
			{
				CompoundNBT itemCompound = new CompoundNBT();
				itemCompound.putByte("Slot", (byte)i);
				stack.write(itemCompound);
				listTag.add(itemCompound);
			}
		}
		tag.put(key, listTag);
		return tag;
	}
	
	public static void loadAllItems(String key, CompoundNBT tag, NonNullList<ItemStack> list)
	{
		ListNBT listTag = tag.getList(key, Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < listTag.size(); i++)
		{
			CompoundNBT slotCompound = listTag.getCompound(i);
			int index = slotCompound.getByte("Slot") & 255;
			if(index < list.size())
			{
				list.set(index, ItemStack.read(slotCompound));
			}
		}
	}
	
	public static boolean TagEquals(ItemStack stack1, ItemStack stack2)
	{
		return stack1.hasTag() == stack2.hasTag() && (!stack1.hasTag() && !stack2.hasTag() || stack1.getTag().equals(stack2.getTag()));
	}
	
}
