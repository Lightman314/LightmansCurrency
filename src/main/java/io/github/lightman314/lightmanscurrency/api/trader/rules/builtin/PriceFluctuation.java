package io.github.lightman314.lightmanscurrency.api.trader.rules.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.rules.IPersistentRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.PriceModifyingTradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Random;

public class PriceFluctuation extends PriceModifyingTradeRule implements IPersistentRule {

    private static final MapCodec<PriceFluctuation> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            activeField(),
            CodecHelper.longRange(TimeHelper.DURATION_MINUTE,Long.MAX_VALUE).fieldOf("duration").forGetter(PriceFluctuation::getDuration),
            Codec.intRange(1,100).fieldOf("fluctuation").forGetter(PriceFluctuation::getFluctuation)
    ).apply(builder,PriceFluctuation::new));

    public static final TradeRuleType<PriceFluctuation> TYPE = new TradeRuleType<>(MAP_CODEC,PriceFluctuation::new);

    public static final TextEntry GUI_LABEL = TextEntry.tradeRuleGui(TYPE,"label");
    public static final TextEntry GUI_INFO = TextEntry.tradeRuleGui(TYPE,"info");

    private long duration = TimeHelper.DURATION_DAY;
    public long getDuration() { return this.duration; }
    private void setDuration(long duration) {
        duration = Math.clamp(duration,TimeHelper.DURATION_MINUTE,Long.MAX_VALUE);
        if(this.duration != duration) {
            this.duration = duration;
            this.setChanged(p -> p.setLong("duration",this.duration));
        }
    }
    private int fluctuation = 10;
    public int getFluctuation() { return this.fluctuation; }
    private void setFluctuation(int fluctuation) {
        fluctuation = Math.clamp(fluctuation,1,100);
        if(this.fluctuation != fluctuation) {
            this.fluctuation = fluctuation;
            this.setChanged(p -> p.setInt("fluctuation",this.fluctuation));
        }
    }

    private PriceFluctuation() {}
    private PriceFluctuation(boolean active,long duration,int fluctuation) {
        super(active);
        this.duration = duration;
        this.fluctuation = fluctuation;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    public IconData getIcon() { return SpriteIcon.of(LCApi.id("icon/price_fluctuation")); }

    @Override
    protected boolean isAdditionalDefaultValues() { return this.duration == TimeHelper.DURATION_DAY && this.fluctuation == 10; }

    @Override
    protected void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        builder.setLong("duration",this.duration)
                .setInt("fluctuation",this.fluctuation);
    }

    @Override
    protected void handlePacket(FancyPacketMap message) {
        if(message.contains("duration"))
            this.duration = message.getLong("duration");
        if(message.contains("fluctuation"))
            this.fluctuation = message.getInt("fluctuation");
    }

    @Override
    protected void handlePlayerInteraction(FancyPacketMap request) {
        if(request.contains("setDuration"))
            this.setDuration(request.getLong("setDuration"));
        if(request.contains("setFluctuation"))
            this.fluctuation = request.getInt("setFluctuation");
    }

    @Override
    protected void resetAdditionalToDefault() {
        this.setDuration(TimeHelper.DURATION_DAY);
        this.setFluctuation(10);
    }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.putLong("duration",this.duration);
        output.putInt("fluctuation",this.fluctuation);
    }

    @Override
    public void decodeSettings(ValueInput data) {
        this.setDuration(data.getLongOr("duration",TimeHelper.DURATION_DAY));
        this.setFluctuation(data.getIntOr("fluctuation",10));
    }

    private long getTraderSeedFactor(TradeEvent.Cost event) {
        long traderID = event.getTrader().getID();
        int tradeIndex = event.getTradeIndex();
        return ((traderID + 1) << 32) + tradeIndex;
    }

    private int randomizePriceMultiplier(long traderSeedFactor) {
        //Have the seed be constant during the given duration
        long seed = TimeHelper.getCurrentTime() / this.duration;
        return new Random(seed * traderSeedFactor).nextInt(-this.fluctuation,this.fluctuation + 1);
    }

    @Override
    protected void tradeCost(TradeEvent.Cost event) {
        event.giveDiscount(this.randomizePriceMultiplier(this.getTraderSeedFactor(event)));
    }

}