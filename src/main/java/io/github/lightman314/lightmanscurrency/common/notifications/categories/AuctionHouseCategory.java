package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class AuctionHouseCategory extends NotificationCategory {

	public static final NotificationCategoryType<AuctionHouseCategory> TYPE = new NotificationCategoryType<>(new ResourceLocation(LightmansCurrency.MODID,"auction_house"),AuctionHouseCategory::loadInstance);

	
	public static final AuctionHouseCategory INSTANCE = new AuctionHouseCategory();
	
	private AuctionHouseCategory() { }

	@Nonnull
	@Override
	public @NotNull IconData getIcon() { return AuctionHouseTrader.ICON; }
	
	@Nonnull
	@Override
	public MutableComponent getName() { return EasyText.translatable("gui.lightmanscurrency.universaltrader.auction"); }

	@Nonnull
    @Override
	public NotificationCategoryType<AuctionHouseCategory> getType() { return TYPE; }
	
	@Override
	public boolean matches(NotificationCategory other) { return other == INSTANCE; }
	
	public void saveAdditional(CompoundTag compound) { }

	private static AuctionHouseCategory loadInstance(CompoundTag ignored) { return INSTANCE; }
	
	
}
