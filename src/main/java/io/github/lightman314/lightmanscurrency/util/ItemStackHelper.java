package io.github.lightman314.lightmanscurrency.util;

import com.mojang.authlib.properties.PropertyMap;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.*;

public class ItemStackHelper {

	//Cache skulls for convenience
	private static final Map<String,ItemStack> skullsByName = new HashMap<>();
	private static final Map<UUID,ItemStack> skullsById = new HashMap<>();

	public static ItemStack skullForPlayer(String playerName)
	{
		if(!skullsByName.containsKey(playerName))
		{
			ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
			stack.set(DataComponents.PROFILE, new ResolvableProfile(Optional.of(playerName),Optional.empty(),new PropertyMap()));
			skullsByName.put(playerName,stack);
		}
		return skullsByName.get(playerName);
	}

	public static ItemStack skullForPlayer(UUID playerID)
	{
		if(!skullsById.containsKey(playerID))
		{
			ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
			stack.set(DataComponents.PROFILE, new ResolvableProfile(Optional.empty(),Optional.of(playerID),new PropertyMap()));
			skullsById.put(playerID,stack);
		}
		return skullsById.get(playerID);
	}

    @Deprecated
	public static void loadAllItems(String key, CompoundTag tag, NonNullList<ItemStack> list, HolderLookup.Provider lookup)
	{
		ListTag listTag = tag.getList(key, Tag.TAG_COMPOUND);
        DataContext<Tag> context = DataContext.createNBT(lookup);
		for(int i = 0; i < listTag.size(); i++)
		{
			CompoundTag slotCompound = listTag.getCompound(i);
			int index = slotCompound.getByte("Slot") & 255;
			if(index < list.size())
			{
				ItemStack stack = context.read(slotCompound,CodecHelper.UNLIMITED_ITEM);
				list.set(index, stack);
			}
		}
	}
	
}
