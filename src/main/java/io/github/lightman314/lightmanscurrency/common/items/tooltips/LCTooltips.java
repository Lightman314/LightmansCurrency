package io.github.lightman314.lightmanscurrency.common.items.tooltips;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.NonNullSupplier;

public class LCTooltips {

	public static final NonNullSupplier<List<Component>> ATM = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.atm");
	public static final NonNullSupplier<List<Component>> TERMINAL = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.terminal");
	public static final NonNullSupplier<List<Component>> TICKET_MACHINE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.ticketmachine");
	public static final NonNullSupplier<List<Component>> CASH_REGISTER = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.cashregister");

	public static final NonNullSupplier<List<Component>> TAX_COLLECTOR = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.tax_collector");

	public static final NonNullSupplier<List<Component>> COIN_MINT = () -> {
		List<Component> result = new ArrayList<>();
		if(LCConfig.SERVER.coinMintCanMint.get())
			result.add(EasyText.translatable("tooltip.lightmanscurrency.coinmint.mintable").withStyle(TooltipItem.DEFAULT_STYLE));
		if(LCConfig.SERVER.coinMintCanMelt.get())
			result.add(EasyText.translatable("tooltip.lightmanscurrency.coinmint.meltable").withStyle(TooltipItem.DEFAULT_STYLE));
		return result;
	};
	
	public static final NonNullSupplier<List<Component>> ITEM_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.item");
	public static final NonNullSupplier<List<Component>> SLOT_MACHINE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.slot_machine");
	public static final NonNullSupplier<List<Component>> ITEM_TRADER_ARMOR = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.item.armor");
	public static final NonNullSupplier<List<Component>> ITEM_TRADER_TICKET = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.item.ticket");
	public static final NonNullSupplier<List<Component>> ITEM_TRADER_BOOK = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.item.book");
	public static final NonNullSupplier<List<Component>> ITEM_NETWORK_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.network.item");
	public static final NonNullSupplier<List<Component>> ITEM_TRADER_INTERFACE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.interface.item");
	public static final NonNullSupplier<List<Component>> PAYGATE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.paygate");
	
	
}
