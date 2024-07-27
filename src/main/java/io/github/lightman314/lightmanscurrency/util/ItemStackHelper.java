package io.github.lightman314.lightmanscurrency.util;

import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import javax.annotation.Nonnull;
import java.util.*;

public class ItemStackHelper {

	//Cache skulls for convenience
	private static final Map<String,ItemStack> skullsByName = new HashMap<>();
	private static final Map<UUID,ItemStack> skullsById = new HashMap<>();

	public static ItemStack skullForPlayer(@Nonnull String playerName)
	{
		if(!skullsByName.containsKey(playerName))
		{
			ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
			stack.set(DataComponents.PROFILE, new ResolvableProfile(Optional.of(playerName),Optional.empty(),new PropertyMap()));
			skullsByName.put(playerName,stack);
		}
		return skullsByName.get(playerName);
	}

	public static ItemStack skullForPlayer(@Nonnull UUID playerID)
	{
		if(!skullsById.containsKey(playerID))
		{
			ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
			stack.set(DataComponents.PROFILE, new ResolvableProfile(Optional.empty(),Optional.of(playerID),new PropertyMap()));
			skullsById.put(playerID,stack);
		}
		return skullsById.get(playerID);
	}

	public static CompoundTag saveAllItems(String key, CompoundTag tag, NonNullList<ItemStack> list, HolderLookup.Provider lookup)
	{
		ListTag listTag = new ListTag();
		for(int i = 0; i < list.size(); ++i)
		{
			ItemStack stack = list.get(i);
			if(!stack.isEmpty())
			{
				CompoundTag stackTag = InventoryUtil.saveItemNoLimits(stack,lookup);
				stackTag.putByte("Slot", (byte)i);
				listTag.add(stackTag);
			}
		}
		tag.put(key, listTag);
		return tag;
	}
	
	public static void loadAllItems(String key, CompoundTag tag, NonNullList<ItemStack> list, HolderLookup.Provider lookup)
	{
		ListTag listTag = tag.getList(key, Tag.TAG_COMPOUND);
		for(int i = 0; i < listTag.size(); i++)
		{
			CompoundTag slotCompound = listTag.getCompound(i);
			int index = slotCompound.getByte("Slot") & 255;
			if(index < list.size())
			{
				ItemStack stack = InventoryUtil.loadItemNoLimits(slotCompound,lookup);
				list.set(index, stack);
			}
		}
	}
	
	public static boolean TagEquals(ItemStack stack1, ItemStack stack2)
	{
		return Objects.equals(stack1.getComponents(),stack2.getComponents());
	}
	
}
