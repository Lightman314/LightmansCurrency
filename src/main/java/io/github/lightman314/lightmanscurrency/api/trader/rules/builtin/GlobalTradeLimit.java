package io.github.lightman314.lightmanscurrency.api.trader.rules.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.rules.IPersistentRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GlobalTradeLimit extends TradeRule implements IPersistentRule {

    public static final int MAX_LIMIT = 1000000;

    private static final MapCodec<GlobalTradeLimit> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            activeField(),
            Codec.intRange(1,MAX_LIMIT).fieldOf("limit").forGetter(GlobalTradeLimit::getLimit),
            Codec.intRange(0,Integer.MAX_VALUE).fieldOf("count").forGetter(GlobalTradeLimit::getCount)
    ).apply(builder,GlobalTradeLimit::new));

    public static final TradeRuleType<GlobalTradeLimit> TYPE = new TradeRuleType<>(MAP_CODEC,GlobalTradeLimit::new);

    public static final TextEntry DENIAL = TextEntry.tradeRuleMessage(TYPE,"denial");
    public static final TextEntry DENIAL_LIMIT = TextEntry.tradeRuleMessage(TYPE,"denial.limit");
    public static final TextEntry INFO = TextEntry.tradeRuleMessage(TYPE,"info");

    public static final TextEntry BUTTON_CLEAR_MEMORY = TextEntry.tradeRuleButton(TYPE,"clear_memory");
    public static final TextEntry TOOLTIP_CLEAR_MEMORY = TextEntry.tradeRuleTooltip(TYPE,"clear_memory");
    public static final TextEntry GUI_INFO = TextEntry.tradeRuleGui(TYPE,"info");

    private int limit = 1;
    public int getLimit() { return this.limit; }
    private void setLimit(int newValue) {
        newValue = Math.clamp(newValue,1,MAX_LIMIT);
        if(this.limit != newValue) {
            this.limit = newValue;
            this.setChanged(p -> p.setInt("limit",this.limit));
        }
    }

    private int count = 0;
    public int getCount() { return this.count; }
    private void setCount(int newValue) {
        if(this.count != newValue) {
            this.count = newValue;
            this.setChanged(p -> p.setInt("count",this.count));
        }
    }
    private void addCount() { this.setCount(this.count + 1); }
    private void resetCount() { this.setCount(0); }

    private GlobalTradeLimit() { }
    private GlobalTradeLimit(boolean active,int limit,int count) {
        super(active);
        this.limit = limit;
        this.count = count;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    public IconData getIcon() { return SpriteIcon.of(LCApi.id("icon/counting")); }

    @Override
    protected boolean isAdditionalDefaultValues() { return this.limit == 1 && this.count == 0; }

    @Override
    protected void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        builder.setInt("limit",this.limit)
                .setInt("count",this.count);
    }

    @Override
    protected void handlePacket(FancyPacketMap message) {
        if(message.contains("limit"))
            this.limit = message.getInt("limit");
        if(message.contains("count"))
            this.count = message.getInt("count");
    }

    @Override
    protected void beforeTrade(TradeEvent.Pre event) {
        if(this.count >= this.limit) {
            event.addDenial(DENIAL.get(this.count));
            event.addDenial(DENIAL_LIMIT.get(this.limit));
        }
        else
            event.addNeutral(INFO.get(this.count,this.limit));
    }

    @Override
    protected void afterTrade(TradeEvent.Post event) { this.addCount(); }

    @Override
    protected void handlePlayerInteraction(FancyPacketMap request) {
        if(request.contains("setLimit"))
            this.setLimit(request.getInt("setLimit"));
        if(request.contains("clearCount"))
            this.resetCount();
    }

    @Override
    protected void resetAdditionalToDefault() {
        this.setLimit(1);
    }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.putInt("limit",this.limit);
    }

    @Override
    public void decodeSettings(ValueInput data) {
        this.limit = data.getIntOr("limit",1);
    }

}