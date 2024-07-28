package io.github.lightman314.lightmanscurrency.util;

import com.mojang.authlib.GameProfile;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemStackHelper {

	//Cache skulls for convenience
	private static final Map<String,ItemStack> skullsByName = new HashMap<>();
	private static final Map<UUID,ItemStack> skullsById = new HashMap<>();

	public static ItemStack skullForPlayer(@Nonnull String playerName)
	{
		if(!skullsByName.containsKey(playerName))
		{
			ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
			CompoundTag tag = new CompoundTag();
			tag.putString("SkullOwner", playerName);
			stack.setTag(tag);
			skullsByName.put(playerName,stack);
		}
		return skullsByName.get(playerName);
	}

	public static ItemStack skullForPlayer(@Nonnull UUID playerID)
	{
		if(!skullsById.containsKey(playerID))
		{
			ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
			CompoundTag tag = stack.getOrCreateTag();
			//Man
			GameProfile profile = new GameProfile(playerID,"");
			SkullBlockEntity.updateGameprofile(profile, (result) -> stack.getOrCreateTag().put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), result)));
			skullsById.put(playerID,stack);
		}
		return skullsById.get(playerID);
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
