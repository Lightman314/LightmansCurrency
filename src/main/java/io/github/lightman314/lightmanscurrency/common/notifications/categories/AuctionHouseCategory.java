package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

import javax.annotation.Nonnull;

public class AuctionHouseCategory extends NotificationCategory {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID,"auction_house");
	
	public static final AuctionHouseCategory INSTANCE = new AuctionHouseCategory();
	
	private AuctionHouseCategory() { }

	@Nonnull
    @Override
	public IconData getIcon() { return AuctionHouseTrader.ICON; }
	
	@Override
	public IFormattableTextComponent getName() { return EasyText.translatable("gui.lightmanscurrency.universaltrader.auction"); }

	@Override
	public ResourceLocation getType() { return TYPE; }
	
	@Override
	public boolean matches(NotificationCategory other) { return other == INSTANCE; }
	
	public void saveAdditional(CompoundNBT compound) { }
	
	
}