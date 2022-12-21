package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class AuctionHouseCategory extends NotificationCategory {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID,"auction_house");
	
	public static final AuctionHouseCategory INSTANCE = new AuctionHouseCategory();
	
	private AuctionHouseCategory() { }

	@Override
	public @NotNull IconData getIcon() { return AuctionHouseTrader.ICON; }
	
	@Override
	public MutableComponent getName() { return Component.translatable("gui.lightmanscurrency.universaltrader.auction"); }

	@Override
	public ResourceLocation getType() { return TYPE; }
	
	@Override
	public boolean matches(NotificationCategory other) { return other == INSTANCE; }
	
	public void saveAdditional(CompoundTag compound) { }
	
	
}
