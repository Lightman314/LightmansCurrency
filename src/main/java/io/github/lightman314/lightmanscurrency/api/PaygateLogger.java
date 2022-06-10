package io.github.lightman314.lightmanscurrency.api;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.PaygateTradeData;
import net.minecraft.network.chat.Component;

public class PaygateLogger extends TextLogger {

	public PaygateLogger() { super("PaygateHistory"); }

	public void AddLog(PlayerReference player, PaygateTradeData trade, @Nonnull CoinValue pricePaid, boolean isCreative) {
		
		Component creativeText = getCreativeText(isCreative);
		Component playerName = getPlayerText(player);
		
		Component durationText = PaygateTradeData.formatDurationShort(trade.getDuration());
		
		if(trade.isTicketTrade())
			this.AddLog(Component.translatable("log.shoplog.paygate.format.ticket", creativeText, playerName, trade.getTicketID().toString(), durationText));
		else
			this.AddLog(Component.translatable("log.shoplog.paygate.format.coin", creativeText, playerName, getCostText(pricePaid), durationText));
		
	}
	
}
