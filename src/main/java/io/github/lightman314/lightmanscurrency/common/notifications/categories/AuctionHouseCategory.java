package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification.Category;
import io.github.lightman314.lightmanscurrency.common.universal_traders.auction.AuctionHouseTrader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class AuctionHouseCategory extends Category {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID,"auction_house");
	
	public static final AuctionHouseCategory INSTANCE = new AuctionHouseCategory();
	
	public AuctionHouseCategory() { }

	@Override
	public IconData getIcon() { return AuctionHouseTrader.ICON; }
	
	@Override
	public Component getTooltip() { return new TranslatableComponent("gui.lightmanscurrency.universaltrader.auction"); }

	@Override
	public ResourceLocation getType() { return TYPE; }
	
	@Override
	public boolean matches(Category other) { return other == INSTANCE; }
	
	public void saveAdditional(CompoundTag compound) { }
	
	
}
