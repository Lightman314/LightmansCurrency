package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.CommonData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.SingleLineTaxableNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.trade.PaygateTradeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public class PaygateNotification extends SingleLineTaxableNotification {

	public static final NotificationType<PaygateNotification> TYPE = new Type();
	
	TraderCategory traderData = TraderCategory.getEmpty();

    MoneyValue cost = MoneyValue.empty();

	long ticketID = Long.MIN_VALUE;
	boolean usedPass = false;

    private Either<MoneyValue,Pair<Long,Boolean>> getCostData()
    {
        if(this.ticketID > Long.MIN_VALUE)
            return Either.right(Pair.of(this.ticketID,this.usedPass));
        else
            return Either.left(this.cost);
    }
	
	int duration = 0;
	
	String customer = "";

	private PaygateNotification() {}
    private PaygateNotification(TraderCategory trader, Either<MoneyValue, Pair<Long,Boolean>> cost, int duration, String customer, MoneyValue taxes, CommonData data)
    {
        super(taxes,data);
        this.traderData = trader;
        cost.ifLeft(c -> this.cost = c)
                .ifRight(p -> {
                    this.ticketID = p.getFirst();
                    this.usedPass = p.getSecond();
                });
        this.duration = duration;
        this.customer = customer;
    }
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

	public static Supplier<Notification> createTicket(PaygateTradeData trade, boolean usedPass, PlayerReference customer, TraderCategory traderData) { return () -> new PaygateNotification(trade, MoneyValue.empty(), usedPass, customer, traderData, MoneyValue.empty()); }
	public static Supplier<Notification> createMoney(PaygateTradeData trade, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid) { return () -> new PaygateNotification(trade, cost, false, customer, traderData, taxesPaid); }

    @Override
	public NotificationType<PaygateNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.traderData; }

    @Override
	public Component getNormalMessage() {
		
		if(this.ticketID >= -1)
		{
			if(this.usedPass)
				return LCText.NOTIFICATION_TRADE_PAYGATE_PASS.get(this.customer, this.ticketID, PaygateTradeData.formatDurationShort(this.duration));
			else
				return LCText.NOTIFICATION_TRADE_PAYGATE_TICKET.get(this.customer, this.ticketID, PaygateTradeData.formatDurationShort(this.duration));
		}
		else
			return LCText.NOTIFICATION_TRADE_PAYGATE_MONEY.get(this.customer, this.cost.getText(), PaygateTradeData.formatDurationShort(this.duration));
		
	}

	@Override
	protected void loadNormal(CompoundTag compound, HolderLookup.Provider lookup) {
		
		this.traderData = TraderCategory.loadOldData(compound.getCompound("TraderInfo"),lookup);
		this.duration = compound.getInt("Duration");
		if(compound.contains("TicketID"))
			this.ticketID = compound.getLong("TicketID");
		else if(compound.contains("Price"))
			this.cost = MoneyValue.safeLoad(compound, "Price");
		if(compound.contains("UsedPass"))
			this.usedPass = compound.getBoolean("UsedPass");
		this.customer = compound.getString("Customer");
		
	}

	@Override
	protected boolean canMerge(Notification other) {
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

    private static class Type extends NotificationType<PaygateNotification>
    {
        private static final MapCodec<PaygateNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                TraderCategory.TYPE.codec().codec().fieldOf("trader").forGetter(n -> n.traderData),
                Codec.either(MoneyValue.CODEC,Codec.pair(Codec.LONG,Codec.BOOL)).fieldOf("cost").forGetter(PaygateNotification::getCostData),
                Codec.INT.fieldOf("duration").forGetter(n -> n.duration),
                Codec.STRING.fieldOf("customer").forGetter(n -> n.customer)
        ).and(taxableFields(builder)).apply(builder,PaygateNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,PaygateNotification> STREAM_CODEC = StreamHelper.combine(taxableStreamFields(),
                TraderCategory.TYPE.streamCodec(),n -> n.traderData,
                ByteBufCodecs.either(MoneyValue.STREAM_CODEC, StreamHelper.pair(ByteBufCodecs.VAR_LONG,ByteBufCodecs.BOOL)),PaygateNotification::getCostData,
                ByteBufCodecs.INT,n -> n.duration,
                ByteBufCodecs.STRING_UTF8,n -> n.customer,
                PaygateNotification::new);

        @Override
        protected PaygateNotification createNew() { return new PaygateNotification(); }
        @Override
        public MapCodec<PaygateNotification> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PaygateNotification> streamCodec() { return STREAM_CODEC; }
    }
	
}
