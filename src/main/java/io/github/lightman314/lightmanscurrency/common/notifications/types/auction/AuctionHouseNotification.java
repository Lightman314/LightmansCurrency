package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.AuctionHouseCategory;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class AuctionHouseNotification extends SingleLineNotification {

    @Override
	public final NotificationCategory getCategory() { return AuctionHouseCategory.INSTANCE; }
	
	@Override
	protected final boolean canMerge(Notification other) { return false; }
	
}
