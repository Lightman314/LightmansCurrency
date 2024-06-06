package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.AuctionHouseCategory;

import javax.annotation.Nonnull;

public abstract class AuctionHouseNotification extends Notification {

	@Nonnull
    @Override
	public final NotificationCategory getCategory() { return AuctionHouseCategory.INSTANCE; }
	
	@Override
	protected final boolean canMerge(@Nonnull Notification other) { return false; }
	
}
