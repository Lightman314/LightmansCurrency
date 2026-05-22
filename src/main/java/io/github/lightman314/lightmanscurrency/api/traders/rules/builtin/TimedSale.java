package io.github.lightman314.lightmanscurrency.api.traders.rules.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.PriceTweakingTradeRule;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public class TimedSale extends PriceTweakingTradeRule implements ICopySupportingRule {

	public static final TradeRuleType<TimedSale> TYPE = new Type();

    private static final MapCodec<TimedSale> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.LONG.fieldOf("start").forGetter(TimedSale::getStartTime),
            Codec.LONG.fieldOf("duration").forGetter(TimedSale::getDuration),
            Codec.INT.fieldOf("discount").forGetter(TimedSale::getDiscount),
            baseFields()
    ).apply(builder,TimedSale::new));

	long startTime = 0;
	public void setStartTime(long time) { this.startTime = time; }
	public long getStartTime() { return this.startTime; }
	public boolean timerActive() { return this.startTime != 0 && TimeUtil.compareTime(this.duration, this.startTime); }
	long duration = 0;
	public long getDuration() { return this.duration; }
	public void setDuration(long duration) { this.duration = MathUtil.clamp(duration, 1000, Long.MAX_VALUE); }
	int discount = 10;
	public int getDiscount() { return this.discount; }
	public void setDiscount(int discount) { this.discount = MathUtil.clamp(discount, 1, 100); }
	
	private TimedSale() { }
    private TimedSale(long startTime,long duration,int discount,boolean active) {
        super(active);
        this.startTime = startTime;
        this.duration = duration;
        this.discount = discount;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    protected void encodeInternal(Supplier<LazyPacketData.Builder> source,LazyPacketData.Builder builder,ISyncingContext context) {
        builder.setLong("startTime",this.startTime)
                .setLong("duration",this.duration)
                .setInt("discount",this.discount);
    }

    @Override
    protected void decodeInternal(LazyPacketData data) {
        this.startTime = data.getLong("startTime");
        this.duration = data.getLong("duration");
        this.discount = data.getInt("discount");
    }

    @Override
	public IconData getIcon() { return IconUtil.ICON_TIMED_SALE; }

	@Override
	public void beforeTrade(PreTradeEvent event)
	{
		if(this.timerActive())
		{
			switch (event.getTrade().getTradeDirection()) {
				case SALE ->
						event.addHelpful(LCText.TRADE_RULE_TIMED_SALE_INFO_SALE.get(this.discount, this.getTimeRemaining().getString()));
				case PURCHASE ->
						event.addHelpful(LCText.TRADE_RULE_TIMED_SALE_INFO_PURCHASE.get(this.discount, this.getTimeRemaining().getString()));
				default -> { } //Nothing if direction is NONE
			}
		}
	}
	
	@Override
	public void tradeCost(TradeCostEvent event)
	{
		if(timerActive() && TimeUtil.compareTime(this.duration, this.startTime))
		{
			switch (event.getTrade().getTradeDirection()) {
				case SALE -> event.giveDiscount(this.discount);
				case PURCHASE -> event.hikePrice(this.discount);
				default -> {} //Nothing if direction is NONE
			}
		}
	}
	
	@Override
	public boolean afterTrade(PostTradeEvent event)
	{
        return confirmStillActive();
	}
	
	private boolean confirmStillActive()
	{
		if(!timerActive())
			return false;
		else if(!TimeUtil.compareTime(this.duration, this.startTime))
		{
			this.startTime = 0;
			return true;
		}
		return false;
	}

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		
		//Load start time
		if(compound.contains("startTime", Tag.TAG_LONG))
			this.startTime = compound.getLong("startTime");
		//Load duration
		if(compound.contains("duration", Tag.TAG_LONG))
			this.duration = compound.getLong("duration");
		//Load discount
		if(compound.contains("discount", Tag.TAG_INT))
			this.discount = compound.getInt("discount");
		
	}

	@Override
	public void writeSettings(SavedSettingData.MutableNodeAccess node) {
		node.setIntValue("discount",this.discount);
		node.setLongValue("duration",this.duration);
	}

	@Override
	public void loadSettings(SavedSettingData.NodeAccess node) {
		this.discount = Math.max(1,node.getIntValue("discount"));
		this.duration = node.getLongValue("duration");
	}

	@Override
	public void resetToDefaultState() {
		this.discount = 10;
		this.duration = 0;
		this.startTime = 0;
	}
	
	@Override
	public void handleUpdateMessage(Player player, LazyPacketData updateInfo) {
		if(updateInfo.contains("Discount"))
		{
			this.discount = updateInfo.getInt("Discount");
		}
		else if(updateInfo.contains("Duration"))
		{
			this.duration = updateInfo.getLong("Duration");
		}
		else if(updateInfo.contains("StartSale"))
		{
			if(this.startTime != 0)
				return;
			this.startTime = TimeUtil.getCurrentTime();
		}
		else if(updateInfo.contains("StopSale"))
		{
			if(this.startTime == 0)
				return;
			this.startTime = 0;
		}
	}
	
	public TimeData getTimeRemaining()
	{
		if(!timerActive())
			return new TimeData(0);
		else
		{
			return new TimeData(this.startTime + this.duration - TimeUtil.getCurrentTime());
		}
	}

    private static class Type extends TradeRuleType<TimedSale>
    {
        @Override
        public TimedSale create() { return new TimedSale(); }
        @Override
        public MapCodec<TimedSale> mapCodec() { return MAP_CODEC; }
    }
	
}
