package io.github.lightman314.lightmanscurrency.api.traders.rules.builtin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.IPersistentRule;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.data.PlayerMemory;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.traders.rules.PriceTweakingTradeRule;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class DiscountCodes extends PriceTweakingTradeRule implements ICopySupportingRule, IPersistentRule {

    public static TradeRuleType<DiscountCodes> TYPE = new Type();

    private static final Codec<Map<String,DiscountRules>> DATA_CODEC = Codec.unboundedMap(Codec.STRING,DiscountRules.CODEC);
    private static final Codec<Map<String,DiscountRules>> JSON_DATA_CODEC = Codec.unboundedMap(Codec.STRING,DiscountRules.PARTIAL_CODEC);
    private static final Codec<Map<String,PlayerMemory>> MEMORY_CODEC = Codec.unboundedMap(Codec.STRING,PlayerMemory.CODEC);

    private static final MapCodec<DiscountCodes> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            DATA_CODEC.fieldOf("rules").forGetter(DiscountCodes::getRules),
            baseFields()
    ).apply(builder,DiscountCodes::new));

    private final Map<String,DiscountRules> rules = new HashMap<>();
    public Map<String,DiscountRules> getRules() { return this.rules; }

    private DiscountCodes() { }
    private DiscountCodes(Map<String,DiscountRules> rules,boolean active) {
        super(active);
        this.rules.putAll(rules);
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    public void encodeInternal(Supplier<LazyPacketData.Builder> source, LazyPacketData.Builder builder, Player player) {
        this.rules.forEach((code,rule) -> {
            LazyPacketData.Builder entry = source.get();
            builder.setMap(code,rule.encode(source,source.get(),player));
        });
    }

    @Override
    public void decodeInternal(LazyPacketData data) {
        this.rules.clear();
        for(String key : data.keySet())
            this.rules.put(key,DiscountRules.decode(data.getMap(key)));
    }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(ModItems.COUPON); }

    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event) {
        TradeContext context = event.getContext();
        this.rules.forEach((code,rule) -> {
            if(context.hasDiscountCode(code))
            {
                //Limit Info
                if(rule.limit > 0)
                {
                    event.addNeutral(LCText.TRADE_RULE_DISCOUNT_CODES_INFO_LIMIT.get(rule.memory.getCount(event,rule.timeLimit),rule.limit));
                    if(rule.timeLimit > 0)
                        event.addNeutral(LCText.TRADE_RULE_DISCOUNT_CODES_INFO_TIMED.get(new TimeUtil.TimeData(rule.timeLimit).getString()));
                }
                if(rule.validLimit(event))
                {
                    //Give discount
                    switch (event.getTrade().getTradeDirection()) {
                        case SALE -> event.addHelpful(LCText.TRADE_RULE_DISCOUNT_CODES_INFO_SALE.get(rule.discount));
                        case PURCHASE -> event.addHelpful(LCText.TRADE_RULE_DISCOUNT_CODES_INFO_PURCHASE.get(rule.discount));
                        default -> {} //Nothing by default
                    }
                }
            }
        });
    }

    @Override
    public void tradeCost(TradeEvent.TradeCostEvent event) {
        TradeContext context = event.getContext();
        this.rules.forEach((code,rule) -> {
            if(context.hasDiscountCode(code))
            {
                if(rule.validLimit(event))
                {
                    //Give discount
                    switch (event.getTrade().getTradeDirection()) {
                        case SALE -> event.giveDiscount(rule.discount);
                        case PURCHASE -> event.hikePrice(rule.discount);
                        default -> {} //Nothing by default
                    }
                }
            }
        });
    }

    @Override
    public boolean afterTrade(TradeEvent.PostTradeEvent event) {
        TradeContext context = event.getContext();
        AtomicBoolean dirty = new AtomicBoolean(false);
        this.rules.forEach((code,rule) -> {
            if(context.hasDiscountCode(code))
            {
                context.consumeDiscountCode(code);
                if(rule.limit > 0)
                {
                    rule.memory.addEntry(context.getPlayerReference().id);
                    dirty.set(true);
                }
            }
            if(rule.memory.clearExpiredData(rule.timeLimit))
                dirty.set(true);
        });
        return dirty.get();
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        ListTag list = compound.getList("Rules",Tag.TAG_COMPOUND);
        this.rules.clear();
        for(int i = 0; i < list.size(); ++i)
        {
            Pair<String,DiscountRules> entry = DiscountRules.load(list.getCompound(i));
            this.rules.put(entry.getFirst(),entry.getSecond());
        }
    }

    @Nullable
    @Override
    public JsonObject writePersistentData(DataContext<JsonElement> context) {
        if(this.rules.isEmpty())
            return null;
        JsonObject json = new JsonObject();
        json.add("rules",JSON_DATA_CODEC.encodeStart(context.ops(),this.rules).getOrThrow());
        return json;
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        if(json.has("rules"))
        {
            this.rules.clear();
            this.rules.putAll(JSON_DATA_CODEC.decode(context.ops(),json.get("rules")).getOrThrow(JsonSyntaxException::new).getFirst());
            if(this.rules.isEmpty())
                throw new JsonSyntaxException("Rules cannot be empty!");
            return;
        }
        //Old load method for backwards compatibility
        JsonArray list = GsonHelper.getAsJsonArray(json,"Rules");
        if(list.isEmpty())
            throw new JsonSyntaxException("Rules cannot be empty!");
        this.rules.clear();
        for(int i = 0; i < list.size(); ++i)
        {
            Pair<String,DiscountRules> entry = DiscountRules.read(GsonHelper.convertToJsonObject(list.get(i),"Rules[" + i + "]"));
            this.rules.put(entry.getFirst(),entry.getSecond());
        }
    }

    @Nullable
    @Override
    public CompoundTag writePersistentTag(DataContext<Tag> context) {
        CompoundTag tag = new CompoundTag();
        Map<String,PlayerMemory> data = new HashMap<>();
        this.rules.forEach((key,rule) -> data.put(key,rule.memory));
        tag.put("memory",MEMORY_CODEC.encodeStart(context.ops(),data).getOrThrow());
        return tag;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void readPersistentTag(CompoundTag tag, DataContext<Tag> context) {
        if(tag.contains("memory"))
        {
            Map<String,PlayerMemory> data = MEMORY_CODEC.decode(context.ops(),tag.get("memory")).getOrThrow().getFirst();
            data.forEach((key,mem) -> {
                DiscountRules rule = this.rules.get(key);
                if(rule != null)
                    rule.memory.copyFrom(mem);
            });
            return;
        }
        ListTag list = tag.getList("Memory",Tag.TAG_COMPOUND);
        for(int i = 0; i < list.size(); ++i)
        {
            CompoundTag entry = list.getCompound(i);
            String code = entry.getString("Code");
            if(this.rules.containsKey(code))
                this.rules.get(code).memory.loadOldData(entry);
        }
    }

    @Override
    public void resetToDefaultState() { this.rules.clear(); }

    @Override
    public void writeSettings(SavedSettingData.MutableNodeAccess node) {
        AtomicInteger index = new AtomicInteger(0);
        this.rules.forEach((code,rules) -> {
            SavedSettingData.MutableNodeAccess entryNode = node.forSubNode("rule_" + index.getAndIncrement());
            rules.saveSettings(entryNode,code);
        });
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess node) {
        this.rules.clear();
        for(int i = 0; !node.forSubNode("rule_" + i).isEmpty(); ++i)
        {
            SavedSettingData.NodeAccess entryNode = node.forSubNode("rule_" + i);
            Pair<String,DiscountRules> entry = DiscountRules.loadSettings(entryNode);
            this.rules.put(entry.getFirst(),entry.getSecond());
        }
    }

    @Override
    protected void handleUpdateMessage(Player player, LazyPacketData updateInfo) {

        if(updateInfo.contains("Edit"))
        {
            String code = updateInfo.getString("Edit");
            DiscountRules entry = this.rules.get(code);
            if(entry == null)
            {
                LightmansCurrency.LogWarning("Could not find '" +code + "' entry on the server.");
                return;
            }
            if(updateInfo.contains("Discount"))
                entry.discount = MathUtil.clamp(updateInfo.getInt("Discount"),1,100);
            if(updateInfo.contains("Limit"))
                entry.limit = Math.max(0,updateInfo.getInt("Limit"));
            if(updateInfo.contains("Timer"))
                entry.timeLimit = Math.max(0,updateInfo.getLong("Timer"));
            if(updateInfo.contains("Rename"))
            {
                this.rules.remove(code);
                this.rules.put(updateInfo.getString("Rename"),entry);
            }
        }
        if(updateInfo.contains("Create"))
        {
            String newCode = updateInfo.getString("Create");
            if(!this.rules.containsKey(newCode))
                this.rules.put(newCode,new DiscountRules());
        }
        if(updateInfo.contains("Remove"))
            this.rules.remove(updateInfo.getString("Remove"));
    }

    public static class DiscountRules
    {

        public static final Codec<DiscountRules> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("discount").forGetter(n -> n.discount),
                Codec.INT.fieldOf("limit").forGetter(n -> n.limit),
                Codec.LONG.fieldOf("timeLimit").forGetter(n -> n.timeLimit),
                PlayerMemory.CODEC.fieldOf("memory").forGetter(n -> n.memory)
        ).apply(builder,DiscountRules::new));
        public static final Codec<DiscountRules> PARTIAL_CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("discount").forGetter(n -> n.discount),
                Codec.INT.fieldOf("limit").forGetter(n -> n.limit),
                Codec.LONG.fieldOf("timeLimit").forGetter(n -> n.timeLimit)
        ).apply(builder,DiscountRules::new));


        public int discount;
        public int limit;
        public long timeLimit;
        public final PlayerMemory memory;

        public DiscountRules() { this(10,0,0,new PlayerMemory()); }
        private DiscountRules(int discount,int limit,long timeLimit) { this(discount,limit,timeLimit,new PlayerMemory()); }
        private DiscountRules(int discount,int limit,long timeLimit,PlayerMemory memory) {
            this.discount = discount;
            this.limit = limit;
            this.timeLimit = timeLimit;
            this.memory = memory;
        }

        public boolean validLimit(TradeEvent event) { return this.limit <= 0 || this.memory.getCount(event,this.timeLimit) < this.limit; }

        public LazyPacketData encode(Supplier<LazyPacketData.Builder> source, LazyPacketData.Builder builder,Player player)
        {
            builder.setInt("disount",this.discount);
            builder.setInt("limit",this.limit);
            builder.setLong("timeLimit",this.timeLimit);
            builder.setMap("memory",this.memory.encode(source.get(),player));
            return builder.build();
        }

        public static DiscountRules decode(LazyPacketData data)
        {
            return new DiscountRules(data.getInt("discount"),
                    data.getInt("limit"),
                    data.getLong("timeLimit"),
                    PlayerMemory.decode(data.getMap("memory")));
        }

        @SuppressWarnings("deprecation")
        public static Pair<String,DiscountRules> load(CompoundTag tag)
        {
            String code = tag.getString("Code");
            DiscountRules rules = new DiscountRules();
            rules.discount = MathUtil.clamp(tag.getInt("Discount"),1,100);
            rules.limit = Math.max(0,tag.getInt("Limit"));
            rules.timeLimit = Math.max(0,tag.getLong("Timer"));
            rules.memory.loadOldData(tag);
            return Pair.of(code,rules);
        }

        @Deprecated
        public static Pair<String,DiscountRules> read(JsonObject json) throws JsonSyntaxException
        {
            String code = GsonHelper.getAsString(json,"Code");
            DiscountRules rules = new DiscountRules();
            rules.discount = GsonHelper.getAsInt(json,"Discount");
            rules.limit = GsonHelper.getAsInt(json,"Limit");
            rules.timeLimit = GsonHelper.getAsInt(json,"Timer");
            return Pair.of(code,rules);
        }

        public void saveSettings(SavedSettingData.MutableNodeAccess node,String code)
        {
            node.setStringValue("code",code);
            node.setIntValue("discount",this.discount);
            node.setIntValue("limit",this.limit);
            node.setLongValue("timer",this.timeLimit);
        }

        public static Pair<String,DiscountRules> loadSettings(SavedSettingData.NodeAccess node)
        {
            String code = node.getStringValue("code");
            DiscountRules rules = new DiscountRules();
            rules.discount = node.getIntValue("discount");
            rules.limit = node.getIntValue("limit");
            rules.timeLimit = node.getLongValue("timer");
            return Pair.of(code,rules);
        }


    }

    private static class Type extends TradeRuleType<DiscountCodes>
    {
        @Override
        public DiscountCodes create() { return new DiscountCodes(); }
        @Override
        public MapCodec<DiscountCodes> mapCodec() { return MAP_CODEC; }
    }

}
