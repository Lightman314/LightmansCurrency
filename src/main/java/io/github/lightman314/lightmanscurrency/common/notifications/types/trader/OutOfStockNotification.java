package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

public class OutOfStockNotification extends Notification {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "out_of_stock");
	
	TraderCategory traderData;
	
	int tradeSlot;

	/**
	 * @deprecated Use OutOfStockNotification#create instead for easy supplier handling
	 */
	@Deprecated(since = "2.1.2.2")
	public OutOfStockNotification(TraderCategory traderData, int tradeIndex) {
		this.traderData = traderData;
		this.tradeSlot = tradeIndex + 1;
	}

	public static NonNullSupplier<Notification> create(TraderCategory trader, int tradeIndex) { return () -> new OutOfStockNotification(trader, tradeIndex); }

	public OutOfStockNotification(CompoundTag compound) { this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.traderData; }

	@Override
	public MutableComponent getMessage() { return this.tradeSlot > 0 ? EasyText.translatable("notifications.message.out_of_stock", this.traderData.getTooltip(), this.tradeSlot) : EasyText.translatable("notifications.message.out_of_stock.indexless"); }

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.put("TraderInfo", this.traderData.save());
		compound.putInt("TradeSlot", this.tradeSlot);
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
		this.tradeSlot = compound.getInt("TradeSlot");
	}

	@Override
	protected boolean canMerge(Notification other) { return false; }

}
