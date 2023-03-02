package io.github.lightman314.lightmanscurrency.common.events;

import io.github.lightman314.lightmanscurrency.common.money.MoneyData;
import net.minecraftforge.eventbus.api.Event;

public class GetDefaultMoneyDataEvent extends Event{

	public final MoneyData.CoinDataCollector dataCollector;
	
	public GetDefaultMoneyDataEvent(MoneyData.CoinDataCollector dataCollector) { this.dataCollector = dataCollector; }
	
}
