package io.github.lightman314.lightmanscurrency.items;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PortableTerminalItem extends Item{

	public PortableTerminalItem(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		LightmansCurrency.PROXY.openTerminalScreen(player);
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}
	
}
