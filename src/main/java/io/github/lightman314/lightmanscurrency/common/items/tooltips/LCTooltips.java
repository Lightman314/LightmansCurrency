package io.github.lightman314.lightmanscurrency.common.items.tooltips;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.NonNullSupplier;

public class LCTooltips {

	public static final NonNullSupplier<List<Component>> ATM = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.atm");
	public static final NonNullSupplier<List<Component>> TERMINAL = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.terminal");
	public static final NonNullSupplier<List<Component>> TICKET_MACHINE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.ticketmachine");
	public static final NonNullSupplier<List<Component>> CASH_REGISTER = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.cashregister");
	
	public static final NonNullSupplier<List<Component>> COIN_MINT = () -> {
		List<Component> result = new ArrayList<>();
		if(Config.SERVER.allowCoinMinting.get())
			result.add(Component.translatable("tooltip.lightmanscurrency.coinmint.mintable").withStyle(TooltipItem.DEFAULT_STYLE));
		if(Config.SERVER.allowCoinMelting.get())
			result.add(Component.translatable("tooltip.lightmanscurrency.coinmint.meltable").withStyle(TooltipItem.DEFAULT_STYLE));
		return result;
	};
	
	public static final NonNullSupplier<List<Component>> ITEM_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.item");
	public static final NonNullSupplier<List<Component>> ITEM_TRADER_ARMOR = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.item.armor");
	public static final NonNullSupplier<List<Component>> ITEM_TRADER_TICKET = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.item.ticket");
	public static final NonNullSupplier<List<Component>> ITEM_NETWORK_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.network.item");
	public static final NonNullSupplier<List<Component>> ITEM_TRADER_INTERFACE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.interface.item");
	public static final NonNullSupplier<List<Component>> PAYGATE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.paygate");
	
	
}
