package io.github.lightman314.lightmanscurrency.items;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class PortableTerminalItem extends Item{

	public PortableTerminalItem(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
	{
		LightmansCurrency.PROXY.openTerminalScreen(player);
		return ActionResult.resultSuccess(player.getHeldItem(hand));
	}
	
}
