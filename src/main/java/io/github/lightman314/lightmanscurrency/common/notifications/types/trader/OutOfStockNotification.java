package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OutOfStockNotification extends SingleLineNotification {

	public static final NotificationType<OutOfStockNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("out_of_stock"),OutOfStockNotification::new);
	
	TraderCategory traderData;
	
	int tradeSlot;

	private OutOfStockNotification() { }

	protected OutOfStockNotification(TraderCategory traderData, int tradeIndex) {
		this.traderData = traderData;
		this.tradeSlot = tradeIndex + 1;
	}

	public static Supplier<Notification> create(TraderCategory trader, int tradeIndex) { return () -> new OutOfStockNotification(trader, tradeIndex); }

    @Override
	protected NotificationType<OutOfStockNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.traderData; }

	@Override
	public Component getMessage() { return this.tradeSlot > 0 ? LCText.NOTIFICATION_TRADER_OUT_OF_STOCK.get(this.traderData.getTooltip(), this.tradeSlot) : LCText.NOTIFICATION_TRADER_OUT_OF_STOCK_INDEXLESS.get(); }

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
