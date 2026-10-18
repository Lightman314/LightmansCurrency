package io.github.lightman314.lightmanscurrency.api.trader.rules.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeData;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.rules.IPersistentRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.PriceModifyingTradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TimedSale extends PriceModifyingTradeRule implements IPersistentRule {

    private static final MapCodec<TimedSale> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            activeField(),
            Codec.LONG.fieldOf("startTime").forGetter(TimedSale::getStartTime),
            Codec.LONG.fieldOf("duration").forGetter(TimedSale::getDuration),
            Codec.intRange(1,100).fieldOf("discount").forGetter(TimedSale::getDiscount)
    ).apply(builder,TimedSale::new));

    public static final TradeRuleType<TimedSale> TYPE = new TradeRuleType<>(MAP_CODEC,TimedSale::new);

    public static final TextEntry INFO_SALE = TextEntry.tradeRuleMessage(TYPE,"info.sale");
    public static final TextEntry INFO_PURCHASE = TextEntry.tradeRuleMessage(TYPE,"info.purchase");

    public static final TextEntry GUI_LABEL = TextEntry.tradeRuleGui(TYPE,"label");
    public static final TextEntry GUI_INFO_ACTIVE = TextEntry.tradeRuleGui(TYPE,"info.active");
    public static final TextEntry GUI_INFO_INACTIVE = TextEntry.tradeRuleGui(TYPE,"info.inactive");

    public static final TextEntry BUTTON_START = TextEntry.tradeRuleButton(TYPE,"start");
    public static final TextEntry BUTTON_STOP = TextEntry.tradeRuleButton(TYPE,"stop");
    public static final TextEntry TOOLTIP_START = TextEntry.tradeRuleTooltip(TYPE,"start");
    public static final TextEntry TOOLTIP_STOP = TextEntry.tradeRuleTooltip(TYPE,"stop");

    private long startTime = 0;
    public long getStartTime() {
        this.checkStartTime();
        return this.startTime;
    }
    private void setStartTime(long newValue) {
        if(this.startTime != newValue) {
            this.startTime = newValue;
            this.setChanged(p -> p.setLong("startTime",this.startTime));
        }
    }
    private void checkStartTime() {
        if(this.isServer() && this.startTime != 0 && TimeHelper.timerExpired(this.startTime,this.duration))
            this.setStartTime(0);
    }

    private long duration = TimeHelper.DURATION_DAY;
    public long getDuration() { return this.duration; }
    private void setDuration(long newValue) {
        newValue = Math.clamp(newValue,TimeHelper.DURATION_MINUTE,TimeHelper.DURATION_DAY * 30);
        if(this.duration != newValue) {
            this.duration = newValue;
            this.setChanged(p -> p.setLong("duration",this.duration));
        }
    }

    public boolean timerActive() {
        this.checkStartTime();
        return this.startTime != 0 && TimeHelper.timerNotExpired(this.startTime,this.duration);
    }
    public TimeData getTimeRemaining() {
        if(!this.timerActive())
            return new TimeData(0);
        return new TimeData(this.startTime + this.duration - TimeHelper.getCurrentTime());
    }

    private int discount = 10;
    public int getDiscount() { return this.discount; }
    private void setDiscount(int newValue) {
        newValue = Math.clamp(newValue,1,100);
        if(this.discount != newValue) {
            this.discount = newValue;
            this.setChanged(p -> p.setInt("discount",this.discount));
        }
    }

    private TimedSale() { }
    private TimedSale(boolean active,long startTime,long duration,int discount) {
        super(active);
        this.startTime = startTime;
        this.duration = duration;
        this.discount = discount;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    public IconData getIcon() { return ItemIcon.of(Items.CLOCK); }

    @Override
    protected boolean isAdditionalDefaultValues() { return this.startTime == 0 && this.duration == TimeHelper.DURATION_DAY && this.discount == 10; }

    @Override
    protected void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        builder.setLong("startTime",this.startTime)
                .setLong("duration",this.duration)
                .setInt("discount",this.discount);
    }

    @Override
    protected void handlePacket(FancyPacketMap message) {
        if(message.contains("startTime"))
            this.startTime = message.getLong("startTime");
        if(message.contains("duration"))
            this.duration = message.getLong("duration");
        if(message.contains("discount"))
            this.discount = message.getInt("discount");
    }

    @Override
    protected void beforeTrade(TradeEvent.Pre event) {
        if(this.timerActive()) {
            switch (event.getTrade().getDirection()) {
                case SALE -> event.addHelpful(INFO_SALE.get(this.discount,this.getTimeRemaining().getString()));
                case PURCHASE -> event.addHelpful(INFO_PURCHASE.get(this.discount,this.getTimeRemaining().getString()));
            }
        }
    }

    @Override
    protected void tradeCost(TradeEvent.Cost event) {
        if(this.timerActive()) {
            switch (event.getTrade().getDirection()) {
                case SALE -> event.giveDiscount(this.discount);
                case PURCHASE -> event.hikePrice(this.discount);
            }
        }
    }

    @Override
    protected void handlePlayerInteraction(FancyPacketMap request) {
        if(request.contains("setActive")) {
            boolean active = request.getBoolean("setActive");
            if(active != this.timerActive()) {
                if(active)
                    this.setStartTime(TimeHelper.getCurrentTime());
                else
                    this.setStartTime(0L);
            }
        }
        if(request.contains("setDuration"))
            this.setDuration(request.getLong("setDuration"));
        if(request.contains("setDiscount"))
            this.setDiscount(request.getInt("setDiscount"));
    }

    @Override
    protected void resetAdditionalToDefault() {
        this.setStartTime(0);
        this.setDuration(TimeHelper.DURATION_DAY);
    }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.putLong("duration",this.duration);
        output.putInt("discount",this.discount);
    }

    @Override
    public void decodeSettings(ValueInput data) {
        this.setStartTime(0);
        this.setDuration(data.getLongOr("duration",TimeHelper.DURATION_DAY));
        this.setDiscount(data.getIntOr("discount",10));
    }

}