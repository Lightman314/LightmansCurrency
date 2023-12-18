package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.TaxableNotification;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.tickets.TicketSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class PaygateNotification extends TaxableNotification {

	public static final NotificationType<PaygateNotification> TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID, "paygate_trade"),PaygateNotification::new);
	
	TraderCategory traderData;
	
	long ticketID = Long.MIN_VALUE;
	boolean usedPass = false;
	MoneyValue cost = MoneyValue.empty();
	
	int duration = 0;
	
	String customer;

	private PaygateNotification() {}

	protected PaygateNotification(PaygateTradeData trade, MoneyValue cost, boolean usedPass, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid) {
		super(taxesPaid);

		this.traderData = traderData;
		this.usedPass = usedPass;
		this.ticketID = trade.getTicketID();
		
		if(trade.isTicketTrade())
			this.ticketID = trade.getTicketID();
		else
			this.cost = cost;
		
		this.duration = trade.getDuration();
		
		this.customer = customer.getName(false);
		
	}

	public static NonNullSupplier<Notification> createTicket(PaygateTradeData trade, boolean usedPass, PlayerReference customer, TraderCategory traderData) { return () -> new PaygateNotification(trade, MoneyValue.empty(), usedPass, customer, traderData, MoneyValue.empty()); }
	public static NonNullSupplier<Notification> createMoney(PaygateTradeData trade, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid) { return () -> new PaygateNotification(trade, cost, false, customer, traderData, taxesPaid); }
	
	@Nonnull
    @Override
	protected NotificationType<PaygateNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public NotificationCategory getCategory() { return this.traderData; }

	@Nonnull
    @Override
	public MutableComponent getNormalMessage() {
		
		if(this.ticketID >= -1)
		{
			if(this.usedPass)
				return EasyText.translatable("notifications.message.paygate_trade.pass", this.customer, this.ticketID, PaygateTradeData.formatDurationShort(this.duration));
			else
				return EasyText.translatable("notifications.message.paygate_trade.ticket", this.customer, this.ticketID, PaygateTradeData.formatDurationShort(this.duration));
		}
		else
			return EasyText.translatable("notifications.message.paygate_trade.coin", this.customer, this.cost.getString(), PaygateTradeData.formatDurationShort(this.duration));
		
	}

	@Override
	protected void saveNormal(CompoundTag compound) {

		compound.put("TraderInfo", this.traderData.save());
		compound.putInt("Duration", this.duration);
		if(this.ticketID >= -1)
		{
			compound.putLong("TicketID", this.ticketID);
			compound.putBoolean("UsedPass", this.usedPass);
		}
		else
			compound.put("Price", this.cost.save());
		compound.putString("Customer", this.customer);
		
	}

	@Override
	protected void loadNormal(CompoundTag compound) {
		
		this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
		this.duration = compound.getInt("Duration");
		if(compound.contains("TicketID"))
			this.ticketID = compound.getLong("TicketID");
		else if(compound.contains("Ticket"))
			this.ticketID = TicketSaveData.getConvertedID(compound.getUUID("Ticket"));
		else if(compound.contains("Price"))
			this.cost = MoneyValue.safeLoad(compound, "Price");
		if(compound.contains("UsedPass"))
			this.usedPass = compound.getBoolean("UsedPass");
		this.customer = compound.getString("Customer");
		
	}

	@Override
	protected boolean canMerge(@Nonnull Notification other) {
		if(other instanceof PaygateNotification pn)
		{
			if(!pn.traderData.matches(this.traderData))
				return false;
			if(pn.ticketID != this.ticketID)
				return false;
			if(pn.usedPass != this.usedPass)
				return false;
			if(pn.duration != this.duration)
				return false;
			if(pn.cost.equals(this.cost))
				return false;
			if(!pn.customer.equals(this.customer))
				return false;
			//Passed all checks. Allow merging.
			return this.TaxesMatch(pn);
		}
		return false;
	}
	
}
