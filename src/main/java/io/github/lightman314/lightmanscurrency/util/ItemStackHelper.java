package io.github.lightman314.lightmanscurrency.util;

import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.component.DataComponents;
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
	
}
