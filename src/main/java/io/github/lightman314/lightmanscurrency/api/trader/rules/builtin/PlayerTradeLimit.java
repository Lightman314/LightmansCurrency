package io.github.lightman314.lightmanscurrency.api.trader.rules.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeData;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.rules.IPersistentRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.rules.data.InteractionMemory;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class PlayerTradeLimit extends TradeRule implements IPersistentRule {

    public static final int MAX_LIMIT = 1000000;

    public static final IconData ICON = SpriteIcon.ofMulti(LCApi.id("icon/counting"),LCApi.id("icon/player_head"));

    private static final MapCodec<PlayerTradeLimit> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            activeField(),
            Codec.intRange(1,MAX_LIMIT).fieldOf("limit").forGetter(PlayerTradeLimit::getLimit),
            CodecHelper.longRange(0,TimeHelper.DURATION_YEAR).fieldOf("timer").forGetter(PlayerTradeLimit::getTimer),
            InteractionMemory.CODEC.fieldOf("memory").forGetter(r -> r.memory)
    ).apply(builder,PlayerTradeLimit::new));

    public static final TradeRuleType<PlayerTradeLimit> TYPE = new TradeRuleType<>(MAP_CODEC,PlayerTradeLimit::new);

    public static final TextEntry DENIAL = TextEntry.tradeRuleMessage(TYPE,"denial");
    public static final TextEntry DENIAL_TIMED = TextEntry.tradeRuleMessage(TYPE,"denial.timed");
    public static final TextEntry DENIAL_TIME_REMAINING = TextEntry.tradeRuleMessage(TYPE,"denial.time_remaining");
    public static final TextEntry DENIAL_LIMIT = TextEntry.tradeRuleMessage(TYPE,"denial.limit");
    public static final TextEntry INFO = TextEntry.tradeRuleMessage(TYPE,"info");
    public static final TextEntry INFO_TIMED = TextEntry.tradeRuleMessage(TYPE,"info.timed");
    public static final TextEntry BUTTON_CLEAR_MEMORY = TextEntry.tradeRuleButton(TYPE,"clear_memory");
    public static final TextEntry TOOLTIP_CLEAR_MEMORY = TextEntry.tradeRuleTooltip(TYPE,"clear_memory");
    public static final TextEntry GUI_INFO = TextEntry.tradeRuleGui(TYPE,"info");
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

    private PlayerTradeLimit() { this.memory = new InteractionMemory().setSidedContext(this).withListener(this::setMemoryChanged); }
    private PlayerTradeLimit(boolean active, int limit, long timer, InteractionMemory memory) {
        super(active);
        this.limit = limit;
        this.timer = timer;
        this.memory = memory.setSidedContext(this).withListener(this::setMemoryChanged);
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    public IconData getIcon() { return ICON; }

    @Override
    protected boolean isAdditionalDefaultValues() { return this.limit == 1 && this.timer == 0 && this.memory.isEmpty(); }

    @Override
    protected void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        builder.setInt("limit",this.limit)
                .setLong("timer",this.timer)
                .setMap("memory",this.memory.createFullSyncPacket(context));
    }

    @Override
    protected void handlePacket(FancyPacketMap message) {
        if(message.contains("limit"))
            this.limit = message.getInt("limit");
        if(message.contains("timer"))
            this.timer = message.getLong("timer");
        if(message.contains("memory"))
            this.memory.handlePacket(message.getMap("memory"));
    }

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
    protected void beforeTrade(TradeEvent.Pre event) {
        int tradeCount = this.memory.getCount(event,this.timer);
        if(tradeCount >= this.limit) {
            if(this.enforceTimeLimit()) {
                event.addDenial(DENIAL_TIMED.get(tradeCount,new TimeData(this.timer).getString()));
                long timeRemaining = this.memory.getTimeRemaining(event,this.timer);
                if(timeRemaining > 0)
                    event.addDenial(DENIAL_TIME_REMAINING.get(new TimeData(timeRemaining).getString()));
            }
            else
                event.addDenial(DENIAL.get(this.limit));
        }
        else
        {
            if(this.enforceTimeLimit())
                event.addNeutral(INFO_TIMED.get(tradeCount,this.limit,new TimeData(this.timer).getString()));
            else
                event.addNeutral(INFO.get(tradeCount,this.limit));
        }
    }

    @Override
    protected void afterTrade(TradeEvent.Post event) {
        this.memory.addEntry(event);
        this.memory.clearExpiredData(this.timer);
    }



}