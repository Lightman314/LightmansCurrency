package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class OutOfStockNotification extends Notification {

	public static final NotificationType<OutOfStockNotification> TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID, "out_of_stock"),OutOfStockNotification::new);
	
	TraderCategory traderData;
	
	int tradeSlot;

	private OutOfStockNotification() { }

	protected OutOfStockNotification(TraderCategory traderData, int tradeIndex) {
		this.traderData = traderData;
		this.tradeSlot = tradeIndex + 1;
	}

	public static NonNullSupplier<Notification> create(TraderCategory trader, int tradeIndex) { return () -> new OutOfStockNotification(trader, tradeIndex); }
	
	@Nonnull
    @Override
	protected NotificationType<OutOfStockNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public NotificationCategory getCategory() { return this.traderData; }

	@Nonnull
	@Override
	public MutableComponent getMessage() { return this.tradeSlot > 0 ? EasyText.translatable("notifications.message.out_of_stock", this.traderData.getTooltip(), this.tradeSlot) : EasyText.translatable("notifications.message.out_of_stock.indexless"); }

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		compound.put("TraderInfo", this.traderData.save());
		compound.putInt("TradeSlot", this.tradeSlot);
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound) {
		this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
		this.tradeSlot = compound.getInt("TradeSlot");
	}

	@Override
	protected boolean canMerge(@Nonnull Notification other) { return false; }

}
