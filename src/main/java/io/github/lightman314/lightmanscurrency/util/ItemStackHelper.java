package io.github.lightman314.lightmanscurrency.util;

import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemStackHelper {

	public static ItemStack skullForPlayer(@Nonnull String playerName)
	{
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
		CompoundTag tag = new CompoundTag();
		tag.putString("SkullOwner", playerName);
		stack.setTag(tag);
		return stack;
	}

	public static CompoundTag saveAllItems(String key, CompoundTag tag, NonNullList<ItemStack> list)
	{
		ListTag listTag = new ListTag();
		for(int i = 0; i < list.size(); ++i)
		{
			ItemStack stack = list.get(i);
			if(!stack.isEmpty())
			{
				CompoundTag itemCompound = new CompoundTag();
				itemCompound.putByte("Slot", (byte)i);
				stack.save(itemCompound);
				listTag.add(itemCompound);
			}
		}
		tag.put(key, listTag);
		return tag;
	}
	
	public static void loadAllItems(String key, CompoundTag tag, NonNullList<ItemStack> list)
	{
		ListTag listTag = tag.getList(key, Tag.TAG_COMPOUND);
		for(int i = 0; i < listTag.size(); i++)
		{
			CompoundTag slotCompound = listTag.getCompound(i);
			int index = slotCompound.getByte("Slot") & 255;
			if(index < list.size())
			{
				ItemStack stack = ItemStack.of(slotCompound);
				//Manual override to force tickets to be converted on load
				if(stack.getItem() instanceof TicketItem)
					TicketItem.GetTicketID(stack);
				//Normal operations
				list.set(index, stack);
			}
		}
	}
	
	public static boolean TagEquals(ItemStack stack1, ItemStack stack2)
	{
		return compareTag(stack1.getTag(),stack2.getTag()) && stack1.areCapsCompatible(stack2);
	}

	private static boolean compareTag(@Nullable CompoundTag tag1, @Nullable CompoundTag tag2)
	{
		if(tag1 != null && tag2 != null)
			return tag1.equals(tag2);
		return (tag1 == null) == (tag2 == null);
	}
	
}
