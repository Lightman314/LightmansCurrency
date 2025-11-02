package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.ItemValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;

public class PortableATMItem extends TooltipItem{

	public PortableATMItem(Properties properties)
	{
		super(properties.stacksTo(1), LCText.TOOLTIP_ATM.asTooltip());
	}
	
	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand)
	{
		if(player instanceof ServerPlayer sp)
		{
			if(QuarantineAPI.IsDimensionQuarantined(level))
				EasyText.sendMessage(player,LCText.MESSAGE_DIMENSION_QUARANTINED_BANK.getWithStyle(ChatFormatting.GOLD));
			else
				NetworkHooks.openScreen(sp, getMenuProvider(this), EasyMenu.encoder(new ItemValidator(this)));
		}

		return InteractionResultHolder.success(player.getItemInHand(hand));
	}
	
	public static MenuProvider getMenuProvider(Item item)
	{
		return new SimpleMenuProvider((windowId, playerInventory, playerEntity) -> new ATMMenu(windowId, playerInventory, new ItemValidator(item)), Component.empty());
	}
	
}
