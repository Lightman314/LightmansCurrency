package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

public class OutOfStockNotification extends Notification {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "out_of_stock");
	
	TraderCategory traderData;
	
	int tradeSlot;
	
	public OutOfStockNotification(TraderCategory traderData, int tradeIndex) {
		this.traderData = traderData;
		this.tradeSlot = tradeIndex + 1;
	}
	
	public OutOfStockNotification(CompoundNBT compound) { this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.traderData; }

	@Override
	public IFormattableTextComponent getMessage() { return EasyText.translatable("notifications.message.out_of_stock", this.traderData.getTooltip(), this.tradeSlot); }

	@Override
	protected void saveAdditional(CompoundNBT compound) {
		compound.put("TraderInfo", this.traderData.save());
		compound.putInt("TradeSlot", this.tradeSlot);
	}

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
		this.tradeSlot = compound.getInt("TradeSlot");
	}

	@Override
	protected boolean canMerge(Notification other) { return false; }

}