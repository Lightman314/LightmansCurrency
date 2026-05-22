package io.github.lightman314.lightmanscurrency.api.traders.rules.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.rules.*;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class DailyTrades extends TradeRule implements ICopySupportingRule, IPersistentRule {

    public static final TradeRuleType<DailyTrades> TYPE = new Type();

    private static final Codec<Map<UUID,Data>> DATA_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC,Data.CODEC);
    private static final MapCodec<DailyTrades> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            DATA_CODEC.fieldOf("data").forGetter(r -> r.data),
            Codec.LONG.fieldOf("delay").forGetter(DailyTrades::getInteractionDelay),
            baseFields()
            ).apply(builder,DailyTrades::new));

    private final Map<UUID,Data> data;
    public int dataSize() { return this.data.size(); }
    private long interactionDelay = TimeUtil.DURATION_DAY;
    public long getInteractionDelay() { return this.interactionDelay; }

    private DailyTrades() { this.data = new HashMap<>(); }
    private DailyTrades(Map<UUID,Data> data,long interactionDelay,boolean active) {
        super(active);
        this.data = new HashMap<>(data);
        this.interactionDelay = interactionDelay;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    protected void encodeInternal(Supplier<LazyPacketData.Builder> source, LazyPacketData.Builder builder, ISyncingContext context) {
        builder.setLong("delay",this.interactionDelay);
        for(UUID entry : context.getCustomerSet())
        {
            if(this.data.containsKey(entry))
            {
                LazyPacketData.Builder b = source.get();
                Data d = this.data.get(entry);
                b.setUUID("player",entry);
                b.setInt("nextIndex",d.nextIndex);
                b.setLong("timestamp",d.lastTimeStamp);
                builder.addToList("data",b,LazyPacketData.BUILDER_FACTORY);
            }
        }
    }

    @Override
    protected void decodeInternal(LazyPacketData data) {
        this.interactionDelay = data.getLong("delay");
        this.data.clear();
        for(LazyPacketData entry : data.getList("data",LazyPacketData.class))
        {
            UUID player = entry.getUUID("player");
            if(player != null)
                this.data.put(player,new Data(entry.getInt("nextIndex"),entry.getLong("timestamp")));
        }
    }

    @Override
    protected boolean onlyAllowOnTraders() { return true; }

    @Override
    protected boolean canActivate(@Nullable ITradeRuleHost host) { return super.canActivate(host) && host instanceof TraderData trader && trader.getTradeCount() > 1; }
    
    @Override
    public IconData getIcon() { return IconUtil.ICON_DAILY_TRADE; }

    //Check if trade index is the one flagged as the next trade index for the given player
    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event) {
        if(event.getPlayerReference() == null)
            return;
        Data data = this.data.getOrDefault(event.getPlayerReference().id,new Data());
        int index = event.getTradeIndex();
        if(data.nextIndex == index)
        {
            if(TimeUtil.compareTime(this.interactionDelay,data.lastTimeStamp))
            {
                //Time has not yet passed, give info about the remaining time to wait
                long timeUntil = data.lastTimeStamp + this.interactionDelay - TimeUtil.getCurrentTime();
                event.addDenial(LCText.TRADE_RULE_DAILY_TRADES_LOCKED_WAITING.get(new TimeUtil.TimeData(timeUntil).getShortString()));
            }
            else
            {
                //Enough time has passed, allow the interaction
                event.addHelpful(LCText.TRADE_RULE_DAILY_TRADES_ALLOWED.get());
            }
        }
        else if(index < data.nextIndex)
        {
            //Already completed, so locked permanently
            event.addDenial(LCText.TRADE_RULE_DAILY_TRADES_LOCKED_COMPLETE.get());
        }
        else
        {
            //Trade is not the next one in the list, so locked until previous trade is complete
            event.addDenial(LCText.TRADE_RULE_DAILY_TRADES_LOCKED_NOT_NEXT.get());
        }
    }

    //Update data
    @Override
    public boolean afterTrade(TradeEvent.PostTradeEvent event) {
        PlayerReference player = event.getPlayerReference();
        if(player == null)
            return false;
        Data dat = this.data.getOrDefault(player.id,new Data());
        if(event.getTradeIndex() == dat.nextIndex)
        {
            dat.nextIndex++;
            dat.lastTimeStamp = TimeUtil.getCurrentTime();
            this.data.put(player.id,dat);
            return true;
        }
        else
            LightmansCurrency.LogWarning("A Daily Trade managed to go through for a trade that is not flagged as the next in the progression lineup.");
        return false;
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        this.interactionDelay = compound.getLong("Delay");
        this.loadData(compound);
    }

    //Loads data in the old format
    private void loadData(CompoundTag compound)
    {
        this.data.clear();
        ListTag dataList = compound.getList("Data", Tag.TAG_COMPOUND);
        for(int i = 0; i < dataList.size(); ++i)
        {
            CompoundTag tag = dataList.getCompound(i);
            Data dat = new Data();
            UUID id = tag.getUUID("Player");
            dat.nextIndex = tag.getInt("Index");
            dat.lastTimeStamp = tag.getLong("Time");
            this.data.put(id,dat);
        }
    }

    @Override
    public void writeSettings(SavedSettingData.MutableNodeAccess node) {
        node.setLongValue("delay",this.interactionDelay);
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess node) {
        this.interactionDelay = node.getLongValue("delay");
    }

    @Override
    public void resetToDefaultState() {
        this.interactionDelay = TimeUtil.DURATION_DAY;
        this.data.clear();
    }

    @Nullable
    @Override
    public JsonObject writePersistentData(DataContext<JsonElement> context) {
        JsonObject json = new JsonObject();
        json.addProperty("Delay",this.interactionDelay);
        return json;
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        this.interactionDelay = GsonHelper.getAsLong(json,"Delay");
    }

    @Nullable
    @Override
    public CompoundTag writePersistentTag(DataContext<Tag> context) {

        if(this.data.isEmpty())
            return null;
        CompoundTag tag = new CompoundTag();
        tag.put("data",DATA_CODEC.encodeStart(context.ops(),this.data).getOrThrow());
        return tag;
    }

    @Override
    public void readPersistentTag(CompoundTag tag, DataContext<Tag> context) {
        if(tag.contains("data"))
        {
            this.data.clear();
            this.data.putAll(DATA_CODEC.decode(context.ops(),tag.get("data")).getOrThrow().getFirst());
        }
        else if(tag.contains("Data"))
        {
            //Load old data
            this.data.clear();
            this.loadData(tag);
        }
    }

    @Override
    protected void handleUpdateMessage(Player player, LazyPacketData updateInfo) {
        if(updateInfo.contains("ClearData"))
            this.data.clear();
        if(updateInfo.contains("SetDelay"))
            this.interactionDelay = updateInfo.getLong("SetDelay");
    }

    private static class Data
    {
        private static final Codec<Data> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("nextIndex").forGetter(d -> d.nextIndex),
                Codec.LONG.fieldOf("timestamp").forGetter(d -> d.lastTimeStamp)
        ).apply(builder,Data::new));

        private Data() {}
        private Data(int nextIndex,long lastTimeStamp) { this.nextIndex = nextIndex; this.lastTimeStamp = lastTimeStamp; }

        public int nextIndex = 0;
        public long lastTimeStamp = Long.MIN_VALUE;
    }

    private static class Type extends TradeRuleType<DailyTrades>
    {
        @Override
        public DailyTrades create() { return new DailyTrades(); }
        @Override
        public MapCodec<DailyTrades> mapCodec() { return MAP_CODEC; }
    }

}
