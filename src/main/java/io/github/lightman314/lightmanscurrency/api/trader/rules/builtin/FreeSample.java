package io.github.lightman314.lightmanscurrency.api.trader.rules.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeData;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.rules.IPersistentRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.PriceModifyingTradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleHolder;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.rules.data.InteractionMemory;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class FreeSample extends PriceModifyingTradeRule implements IPersistentRule {

    public static final int MAX_LIMIT = 100;

    private static final MapCodec<FreeSample> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            activeField(),
            Codec.intRange(1,MAX_LIMIT).fieldOf("limit").forGetter(FreeSample::getLimit),
            CodecHelper.longRange(0,TimeHelper.DURATION_YEAR).fieldOf("timer").forGetter(FreeSample::getTimer),
            InteractionMemory.CODEC.fieldOf("memory").forGetter(FreeSample::getMemory),
            Codec.INT.fieldOf("total").forGetter(FreeSample::getSampleCount)
    ).apply(builder,FreeSample::new));
    public static final TradeRuleType<FreeSample> TYPE = new TradeRuleType<>(MAP_CODEC,FreeSample::new);

    public static final IconData ICON = SpriteIcon.of(LCApi.id("icon/free_sample"));

    public static final TextEntry INFO_SINGLE = TextEntry.tradeRuleMessage(TYPE,"info.single");
    public static final TextEntry INFO_MULTI = TextEntry.tradeRuleMessage(TYPE,"info.multi");
    public static final TextEntry INFO_USED = TextEntry.tradeRuleMessage(TYPE,"info.used");
    public static final TextEntry INFO_TIMED = TextEntry.tradeRuleMessage(TYPE,"info.timed");
    public static final TextEntry INFO_TIME_REMAINING = TextEntry.tradeRuleMessage(TYPE,"info.time_remaining");
    public static final TextEntry BUTTON_CLEAR_MEMORY = TextEntry.button(LCApi.MODID,"trade_rule.free_sample.reset");
    public static final TextEntry TOOLTIP_CLEAR_MEMORY = TextEntry.tooltip(LCApi.MODID,"trade_rule.free_sample.reset");
    public static final TextEntry GUI_INFO = TextEntry.gui(LCApi.MODID,"trade_rule.free_sample.info");
    public static final TextEntry GUI_PLAYER_COUNT = TextEntry.gui(LCApi.MODID,"trade_rule.free_sample.count");
    public static final TextEntry GUI_DURATION = TextEntry.tradeRuleGui(TYPE,"duration");
    public static final TextEntry GUI_NO_DURATION = TextEntry.tradeRuleGui(TYPE,"no_duration");

    private int limit = 1;
    public int getLimit() { return this.limit; }
    public void setLimit(int newLimit) {
        newLimit = Math.clamp(newLimit,1,MAX_LIMIT);
        if(this.limit != newLimit) {
            this.limit = newLimit;
            this.setChanged(builder -> builder.setInt("limit",this.limit));
        }
    }

    private long timer = 0;
    private boolean enforceTimeLimit() { return this.timer > 0; }
    public long getTimer() { return this.timer; }
    public void setTimer(long timer) {
        timer = Math.clamp(timer,0,TimeHelper.DURATION_YEAR);
        if(this.timer != timer) {
            this.timer = timer;
            this.setChanged(builder -> builder.setLong("timer",this.timer));
        }
    }

    private final InteractionMemory memory;
    public InteractionMemory getMemory() { return this.memory; }
    private void setMemoryChanged(Consumer<FancyPacketMap.Mutable> writer, Predicate<ISyncingContext> filter) {
        this.setChanged(map -> map.modifyMap("memory",writer),filter);
    }

    private int totalCount = 0;
    public int getSampleCount() { return this.totalCount; }

    private FreeSample() {
        this.memory = new InteractionMemory().setSidedContext(this).withListener(this::setMemoryChanged);
    }
    private FreeSample(boolean active, int limit, long timer, InteractionMemory memory, int totalCount) {
        super(active);
        this.limit = limit;
        this.timer = timer;
        this.memory = memory.setSidedContext(this).withListener(this::setMemoryChanged);
        this.totalCount = totalCount;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    protected boolean isAdditionalDefaultValues() { return this.limit == 1 && this.timer == 0 && this.memory.isEmpty() && this.totalCount == 0; }

    @Override
    protected void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context) {
        builder.setInt("limit",this.limit)
                .setLong("timer",this.timer)
                .setMap("memory",this.memory.createFullSyncPacket(context))
                .setInt("total",this.totalCount);
    }

    @Override
    protected void handlePacket(FancyPacketMap message) {
        if(message.contains("limit"))
            this.limit = message.getInt("limit");
        if(message.contains("timer"))
            this.timer = message.getLong("timer");
        if(message.contains("memory"))
            this.memory.handlePacket(message.getMap("memory"));
        if(message.contains("total"))
            this.totalCount = message.getInt("total");
    }

    @Override
    protected boolean canActivate(TradeRuleHolder holder) {
        if(this.getHolder() instanceof TradeData trade && !trade.isSale())
            return false;
        return super.canActivate(holder);
    }

    @Override
    public IconData getIcon() { return ICON; }

    @Override
    protected void beforeTrade(TradeEvent.Pre event) {
        int sampleCount = this.memory.getCount(event,this.timer);
        boolean showTimeRemaining = false;
        if(this.giveDiscount(event)) {
            if(this.limit > 1) {
                event.addHelpful(INFO_MULTI.get(this.limit));
                if(sampleCount > 0)
                    event.addHelpful(INFO_USED.get(sampleCount,this.limit));
            }
            else
                event.addHelpful(INFO_SINGLE.get());
        }
        else {
            if(sampleCount > 0) {
                //Don't show them the alert if no free samples have been used
                event.addHidden(INFO_USED.get(sampleCount,this.limit));
                showTimeRemaining = true;
            }
        }
        if(this.enforceTimeLimit()) {
            //Still show the alert if free samples will come back after a given amount of time
            event.addNeutral(INFO_TIMED.get(new TimeData(this.timer).getString()));
            if(showTimeRemaining) {
                long timeRemaining = this.memory.getTimeRemaining(event,this.timer);
                if(timeRemaining > 0)
                    event.addHidden(INFO_TIME_REMAINING.get(new TimeData(timeRemaining).getString()));
            }
        }


    }

    @Override
    protected void tradeCost(TradeEvent.Cost event) {
        if(this.giveDiscount(event))
            event.makeFree();
    }

    @Override
    protected void afterTrade(TradeEvent.Post event) {
        if(this.giveDiscount(event)) {
            this.memory.addEntry(event);
            this.totalCount++;
            this.setChanged(builder -> builder.setInt("total",this.totalCount));
        }
    }

    private boolean giveDiscount(TradeEvent event) { return this.memory.getCount(event,this.timer) < this.limit && event.getTrade().isSale(); }

    @Override
    protected void handlePlayerInteraction(FancyPacketMap request) {
        if(request.contains("setLimit"))
            this.setLimit(request.getInt("setLimit"));
        if(request.contains("setTimer"))
            this.setTimer(request.getLong("setTimer"));
        if(request.contains("clearMemory"))
            this.memory.clear();
    }

    @Override
    protected void resetAdditionalToDefault() {
        this.setLimit(1);
        this.setTimer(0L);
    }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.putInt("limit",this.limit);
        output.putLong("timer",this.timer);
    }

    @Override
    public void decodeSettings(ValueInput data) {
        this.setLimit(data.getIntOr("limit",1));
        this.setTimer(data.getLongOr("timer",0L));
    }

}