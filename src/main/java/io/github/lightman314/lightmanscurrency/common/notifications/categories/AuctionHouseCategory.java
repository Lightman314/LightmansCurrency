package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import net.minecraft.network.chat.Component;

public class AuctionHouseCategory extends NotificationCategory {

    public static final AuctionHouseCategory INSTANCE = new AuctionHouseCategory();

	public static final NotificationCategoryType<AuctionHouseCategory> TYPE = new NotificationCategoryType.Instance<>(INSTANCE);
	
	private AuctionHouseCategory() { }

	@Override
	public IconData getIcon() { return AuctionHouseTrader.ICON; }

	@Override
	public Component getName() { return LCText.GUI_TRADER_AUCTION_HOUSE.get(); }

    @Override
	public NotificationCategoryType<AuctionHouseCategory> getType() { return TYPE; }
	
	@Override
	public boolean matches(NotificationCategory other) { return other == INSTANCE; }

}
