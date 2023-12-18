package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.List;

import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.AuctionHouseCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.data.ItemWriteData;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public abstract class AuctionHouseNotification extends Notification {

	@Nonnull
    @Override
	public final NotificationCategory getCategory() { return AuctionHouseCategory.INSTANCE; }
	
	@Override
	protected final boolean canMerge(@Nonnull Notification other) { return false; }
	
	protected final Component getItemNames(List<ItemWriteData> items) {
		Component result = null;
		for (ItemWriteData item : items) {
			if (result != null)
				result = item.formatWith(result);
			else
				result = item.format();
		}
		return result == null ? Component.literal("ERROR") : result;
	}
	
}
