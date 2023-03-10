package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.AuctionHouseCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.ItemTradeNotification.ItemData;
import net.minecraft.util.text.ITextComponent;

public abstract class AuctionHouseNotification extends Notification {

	@Override
	public final NotificationCategory getCategory() { return AuctionHouseCategory.INSTANCE; }
	
	@Override
	protected final boolean canMerge(Notification other) { return false; }
	
	protected final ITextComponent getItemNames(List<ItemData> items) {
		ITextComponent result = null;
		for (ItemData item : items) {
			if (result != null)
				result = item.formatWith(result);
			else
				result = item.format();
		}
		return result == null ? EasyText.literal("ERROR") : result;
	}
	
}