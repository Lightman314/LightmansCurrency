package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class PortableATMItem extends TooltipItem{

	public PortableATMItem(Properties properties)
	{
		super(properties.stacksTo(1), LCText.TOOLTIP_ATM.asTooltip());
	}
	
	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(@Nonnull Level world, @Nonnull Player player, @Nonnull InteractionHand hand)
	{
		if(!player.level().isClientSide)
			player.openMenu(getMenuProvider(), EasyMenu.nullEncoder());
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}
	
	public static MenuProvider getMenuProvider()
	{
		return new SimpleMenuProvider((windowId, playerInventory, playerEntity) -> new ATMMenu(windowId, playerInventory, SimpleValidator.NULL), Component.empty());
	}
	
}
