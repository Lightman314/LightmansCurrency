package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.tickets.TicketSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

public class PaygateNotification extends Notification{

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "paygate_trade");
	
	TraderCategory traderData;
	
	long ticketID = Long.MIN_VALUE;
	CoinValue cost = new CoinValue();
	
	int duration = 0;
	
	String customer;
	
	public PaygateNotification(PaygateTradeData trade, CoinValue cost, PlayerReference customer, TraderCategory traderData) {
		
		this.traderData = traderData;
		this.ticketID = trade.getTicketID();
		
		if(trade.isTicketTrade())
			this.ticketID = trade.getTicketID();
		else
			this.cost = cost;
		
		this.duration = trade.getDuration();
		
		this.customer = customer.getName(false);
		
	}
	
	public PaygateNotification(CompoundNBT compound) { this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.traderData; }

	@Override
	public IFormattableTextComponent getMessage() {
		
		if(this.ticketID >= -1)
			return EasyText.translatable("notifications.message.paygate_trade.ticket", this.customer, this.ticketID, PaygateTradeData.formatDurationShort(this.duration));
		else
			return EasyText.translatable("notifications.message.paygate_trade.coin", this.customer, this.cost.getString(), PaygateTradeData.formatDurationShort(this.duration));
		
	}

	@Override
	protected void saveAdditional(CompoundNBT compound) {
		
		compound.put("TraderInfo", this.traderData.save());
		compound.putInt("Duration", this.duration);
		if(this.ticketID >= -1)
			compound.putLong("TicketID", this.ticketID);
		else
			this.cost.save(compound, "Price");
		compound.putString("Customer", this.customer);
		
	}

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		
		this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
		this.duration = compound.getInt("Duration");
		if(compound.contains("TicketID"))
			this.ticketID = compound.getLong("TicketID");
		else if(compound.contains("Ticket"))
			this.ticketID = TicketSaveData.getConvertedID(compound.getUUID("Ticket"));
		else if(compound.contains("Price"))
			this.cost.load(compound, "Price");
		this.customer = compound.getString("Customer");
		
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof PaygateNotification)
		{
			PaygateNotification pn = (PaygateNotification)other;
			if(!pn.traderData.matches(this.traderData))
				return false;
			if(pn.ticketID != this.ticketID)
				return false;
			if(pn.duration != this.duration)
				return false;
			if(pn.cost.getRawValue() != this.cost.getRawValue())
				return false;
			if(!pn.customer.equals(this.customer))
				return false;
			//Passed all checks. Allow merging.
			return true;
		}
		return false;
	}
	
}