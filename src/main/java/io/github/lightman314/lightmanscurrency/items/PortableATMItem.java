package io.github.lightman314.lightmanscurrency.items;

import io.github.lightman314.lightmanscurrency.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.menus.ATMMenu;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PortableATMItem extends TooltipItem{

	public PortableATMItem(Properties properties)
	{
		super(properties.stacksTo(1), LCTooltips.ATM);
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		player.openMenu(this.getMenuProvider());
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}
	
	public MenuProvider getMenuProvider()
	{
		return new SimpleMenuProvider((windowId, playerInventory, playerEntity) -> { return new ATMMenu(windowId, playerInventory);}, new TextComponent(""));
	}
	
}
