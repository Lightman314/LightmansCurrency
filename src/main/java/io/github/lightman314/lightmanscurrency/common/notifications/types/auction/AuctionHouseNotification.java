package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.AuctionHouseCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.ItemTradeNotification.ItemData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public abstract class AuctionHouseNotification extends Notification {

	@Override
	public final NotificationCategory getCategory() { return AuctionHouseCategory.INSTANCE; }
	
	@Override
	protected final boolean canMerge(Notification other) { return false; }
	
	protected final Component getItemNames(List<ItemData> items) {
		Component result = null;
		for(int i = 0; i < items.size(); ++i)
		{
			if(result != null)
				result = items.get(i).formatWith(result);
			else
				result = items.get(i).format();
		}
		return result == null ? new TextComponent("ERROR") : result;
	}
	
}