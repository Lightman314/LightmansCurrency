package io.github.lightman314.lightmanscurrency.api.trader.rules.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeData;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.rules.IPersistentRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleHolder;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DailyTrades extends TradeRule implements IPersistentRule {

    private static final MapCodec<DailyTrades> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            activeField(),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC,Data.CODEC).fieldOf("data").forGetter(r -> r.data),
            Codec.LONG.fieldOf("delay").forGetter(DailyTrades::getDelay)
    ).apply(builder,DailyTrades::new));
    public static final TradeRuleType<DailyTrades> TYPE = new TradeRuleType<>(MAP_CODEC,DailyTrades::new);

    public static final TextEntry ALLOWED = TextEntry.tradeRuleMessage(TYPE,"allowed");
    public static final TextEntry LOCKED_COMPLETE = TextEntry.tradeRuleMessage(TYPE,"locked.complete");
    public static final TextEntry LOCKED_WAITING = TextEntry.tradeRuleMessage(TYPE,"locked.waiting");
    public static final TextEntry LOCKED_NOT_NEXT = TextEntry.tradeRuleMessage(TYPE,"locked.not_next");

    public static final TextEntry BUTTON_RESET = TextEntry.tradeRuleButton(TYPE,"reset");
    public static final TextEntry TOOLTIP_RESET = TextEntry.tradeRuleTooltip(TYPE,"reset");
    public static final TextEntry GUI_INFO = TextEntry.tradeRuleGui(TYPE,"info");

    private final Map<UUID,Data> data;
    public int dataSize() { return this.data.size(); }
    private void clearData() {
        if(this.data.isEmpty())
            return;
        this.data.clear();
        this.setChanged(p -> p.setFlag("clearData").remove("data"));
    }
    private void setDataChanged(UUID data) {
        this.setChanged(p -> {
            Data d = this.data.get(data);
            if(d != null) {
                p.addToList("data",LCFancyPacketTypes.MAP,FancyPacketMap.map()
                        .setUUID("player",data)
                        .setInt("nextIndex",d.nextIndex)
                        .setLong("timestamp",d.lastTimestamp));
            }
        });
    }

    private long delay = TimeHelper.DURATION_DAY;
    public long getDelay() { return this.delay; }
    private void setDelay(long newValue) {
        newValue = Math.clamp(newValue,TimeHelper.DURATION_MINUTE,TimeHelper.DURATION_DAY * 30);
        if(this.delay != newValue) {
            this.delay = newValue;
            this.setChanged(p -> p.setLong("delay",this.delay));
        }
    }

    private DailyTrades() { this.data = new HashMap<>(); }
    private DailyTrades(boolean active,Map<UUID,Data> data,long interactionDelay) {
        super(active);
        this.data = new HashMap<>(data);
        this.delay = interactionDelay;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    public IconData getIcon() { return SpriteIcon.of(LCApi.id("icon/daily_trades")); }

    @Override
    protected boolean isAdditionalDefaultValues() { return this.data.isEmpty() && this.delay == TimeHelper.DURATION_DAY; }

    @Override
    protected void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context) {
        builder.setLong("delay",this.delay);
        for(UUID entry : context.getCustomerSet()) {
            if(this.data.containsKey(entry)) {
                FancyPacketMap.Mutable e = FancyPacketMap.map();
                Data d = this.data.get(entry);
                e.setUUID("player",entry)
                        .setInt("nextIndex",d.nextIndex)
                        .setLong("timestamp",d.lastTimestamp);
                builder.addToList("data", LCFancyPacketTypes.MAP,e);
            }
        }
    }

    @Override
    protected void handlePacket(FancyPacketMap message) {
        this.delay = message.getLong("delay",this.delay);
        if(message.contains("clearData"))
            this.data.clear();
        for(FancyPacketMap entry : message.getList("data",LCFancyPacketTypes.MAP)) {
            UUID player = entry.getUUID("player");
            this.data.put(player,new Data(entry.getInt("nextIndex"),entry.getLong("timestamp")));
        }
    }

    @Override
    protected boolean allowHolder(TradeRuleHolder holder) { return holder.isMultiTrade(); }

    @Override
    protected boolean canActivate(TradeRuleHolder holder) { return holder.getValidTradeCount() > 1; }

    @Override
    protected void handlePlayerInteraction(FancyPacketMap request) {
        if(request.contains("clearData"))
            this.clearData();
        if(request.contains("setDelay"))
            this.setDelay(request.getLong("setDelay"));
    }

    @Override
    protected void resetAdditionalToDefault() {
        this.setDelay(TimeHelper.DURATION_DAY);
        this.clearData();
    }

    @Override
    protected void beforeTrade(TradeEvent.Pre event) {
        UUID customer = event.getCustomer().getID();
        Data data = this.data.getOrDefault(customer,new Data());
        int index = event.getTradeIndex();
        if(data.nextIndex == index) {
            if(!TimeHelper.timerExpired(data.lastTimestamp,this.delay)) {
                long timeUntil = data.lastTimestamp + this.delay - TimeHelper.getCurrentTime();
                event.addDenial(LOCKED_WAITING.get(new TimeData(timeUntil).getShortString()));
            }
            else
                event.addHelpful(ALLOWED.get());
        }
        else if(index < data.nextIndex)
            event.addDenial(LOCKED_COMPLETE.get());
        else
            event.addDenial(LOCKED_NOT_NEXT.get());
    }

    @Override
    protected void afterTrade(TradeEvent.Post event) {
        UUID customer = event.getCustomer().getID();
        Data data = this.data.getOrDefault(customer,new Data());
        if(event.getTradeIndex() == data.nextIndex) {
            data.nextIndex++;
            data.lastTimestamp = TimeHelper.getCurrentTime();
            this.data.put(customer,data);
            this.setDataChanged(customer);
        }
    }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.putLong("delay",this.delay);
    }

    @Override
    public void decodeSettings(ValueInput data) {
        this.delay = data.getLongOr("delay",TimeHelper.DURATION_DAY);
    }

    private static class Data {
        private static final Codec<Data> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.intRange(0,Integer.MAX_VALUE).fieldOf("nextIndex").forGetter(d -> d.nextIndex),
                Codec.LONG.fieldOf("timestamp").forGetter(d -> d.lastTimestamp)
        ).apply(builder,Data::new));

        public int nextIndex = 0;
        public long lastTimestamp = 0;

        private Data() {}
        private Data(int nextIndex,long lastTimestamp) { this.nextIndex = nextIndex; this.lastTimestamp = lastTimestamp;}
    }

}
