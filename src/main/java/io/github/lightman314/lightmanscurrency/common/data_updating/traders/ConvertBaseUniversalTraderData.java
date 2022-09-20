package io.github.lightman314.lightmanscurrency.common.data_updating.traders;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.data_updating.events.ConvertUniversalTraderEvent;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
@SuppressWarnings("deprecation")
public class ConvertBaseUniversalTraderData {

	public static final ResourceLocation ITEM_TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_trader");
	public static final ResourceLocation AUCTION_TYPE = new ResourceLocation(LightmansCurrency.MODID, "auction_house");
	
	@SubscribeEvent
	public static void convertBaseTraders(ConvertUniversalTraderEvent event) {
		if(event.type.equals(ITEM_TYPE))
		{
			//Convert trader data
			ItemTraderData newTrader = new ItemTraderData();
			newTrader.loadOldUniversalTraderData(event.compound);
			event.setTrader(newTrader);
		}
		else if(event.type.equals(AUCTION_TYPE))
		{
			//Convert trader data
			AuctionHouseTrader newTrader = new AuctionHouseTrader();
			newTrader.loadOldUniversalTraderData(event.compound);
			event.setTrader(newTrader);
		}
	}
	
}
