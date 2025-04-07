package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class AuctionHouseCategory extends NotificationCategory {

	public static final NotificationCategoryType<AuctionHouseCategory> TYPE = new NotificationCategoryType<>(VersionUtil.lcResource("auction_house"),AuctionHouseCategory::loadInstance);

	
	public static final AuctionHouseCategory INSTANCE = new AuctionHouseCategory();
	
	private AuctionHouseCategory() { }

	@Nonnull
	@Override
	public IconData getIcon() { return AuctionHouseTrader.ICON; }
	
	@Nonnull
	@Override
	public MutableComponent getName() { return LCText.GUI_TRADER_AUCTION_HOUSE.get(); }

	@Nonnull
    @Override
	public NotificationCategoryType<AuctionHouseCategory> getType() { return TYPE; }
	
	@Override
	public boolean matches(NotificationCategory other) { return other == INSTANCE; }
	
	public void saveAdditional(CompoundTag compound,@Nonnull HolderLookup.Provider lookup) { }

	private static AuctionHouseCategory loadInstance(CompoundTag ignored, @Nonnull HolderLookup.Provider lookup) { return INSTANCE; }
	
	
}
