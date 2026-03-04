package io.github.lightman314.lightmanscurrency.api.traders.rules;

import java.util.*;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.RuleSupportingTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public abstract class TradeRule {


    public static final Codec<TradeRule> CODEC = Codec.withAlternative(
            TradeRuleType.CODEC.dispatch(TradeRule::getType,TradeRuleType::mapCodec),
            CodecHelper.oldValueLoader(TradeRule::loadOldData,"Trade Rule"));
    public static final Codec<Map<TradeRuleType<?>,TradeRule>> SET_CODEC = Codec.dispatchedMap(TradeRuleType.CODEC,t -> TradeRule.CODEC);

	public static String translationKeyOfType(ResourceLocation ruleType) { return "traderule." + ruleType.getNamespace() + "." + ruleType.getPath(); }
	public static String translationKeyOfType(TradeRuleType<?> ruleType) { return translationKeyOfType(LCRegistries.TRADE_RULE.getKey(ruleType)); }
	public static Component nameOfType(ResourceLocation ruleType) { return Component.translatable(translationKeyOfType(ruleType)); }
	public static Component nameOfType(TradeRuleType<?> ruleType) { return nameOfType(LCRegistries.TRADE_RULE.getKey(ruleType)); }
	public final Component getName() { return nameOfType(LCRegistries.TRADE_RULE.getKey(this.getType())); }
	public abstract IconData getIcon();

	private ITradeRuleHost host = null;
	@Nullable
	protected ITradeRuleHost getHost() { return this.host; }

	private boolean isActive = false;
	public boolean isActive() { return this.canActivate(this.host) && this.isActive; }
	public void setActive(boolean active) { this.isActive = active; }

    public final void setChanged()
    {
        if(this.host != null)
            this.host.setRuleChanged(this.getType());
    }

	public boolean allowHost(@Nullable ITradeRuleHost host)
	{
        if(host == null)
            return false;
		if(this.onlyAllowOnTraders() && !host.isTrader())
			return false;
		if(this.onlyAllowOnTrades() && !host.isTrade())
			return false;
		return true;
	}
	public boolean canActivate() { return this.canActivate(this.host); }
	protected boolean canActivate(@Nullable ITradeRuleHost host) { return this.allowHost(host); }
	public boolean canPlayerActivate(Player player) { return this.canActivate(); }

	protected boolean onlyAllowOnTraders() { return false; }
	protected boolean onlyAllowOnTrades() { return false; }

	public void beforeTrade(PreTradeEvent event) {}
	public void tradeCost(TradeCostEvent event) {}
	public boolean afterTrade(PostTradeEvent event) { return false; }
	protected void tradeBaseCost(InternalPriceEvent query) {}

	protected TradeRule() { }
    protected TradeRule(boolean active) { this.isActive = active; }

    public abstract TradeRuleType<?> getType();

	public CompoundTag save(HolderLookup.Provider lookup)
	{
        return (CompoundTag)CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE,lookup),this).getOrThrow();
	}

    public static TradeRule load(CompoundTag tag, DataContext<Tag> context) { return CODEC.decode(context.ops(),tag).getOrThrow().getFirst(); }

    public final void encode(Supplier<LazyPacketData.Builder> source, LazyPacketData.Builder builder,Player player)
    {
        this.encodeInternal(source,builder,player);
        builder.setBoolean("active",this.isActive);
    }

    protected abstract void encodeInternal(Supplier<LazyPacketData.Builder> source, LazyPacketData.Builder builder, Player player);

    public final void decode(LazyPacketData data)
    {
        this.isActive = data.getBoolean("active");
        this.decodeInternal(data);
    }

    protected abstract void decodeInternal(LazyPacketData data);

    @Deprecated
    private static TradeRule loadOldData(CompoundTag tag, HolderLookup.Provider lookup)
    {
        ResourceLocation typeName = VersionUtil.parseResource(tag.getString("Type"));
        TradeRuleType<?> type = LCRegistries.TRADE_RULE.get(typeName);
        if(type != null)
            return type.loadOldData(tag,lookup);
        return null;
    }
    @Deprecated
	protected final void load(CompoundTag compound, HolderLookup.Provider lookup)
	{
		this.isActive = compound.getBoolean("Active");
		this.loadAdditional(compound, lookup);
	}
    @Deprecated
	protected abstract void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup);
	
	public final void receiveUpdateMessage(Player player, LazyPacketData data)
	{
		if(data.contains("SetActive"))
		{
			boolean active = data.getBoolean("SetActive");
			if(active)
				this.isActive = this.isActive || this.canPlayerActivate(player);
			else
				this.isActive = false;
		}
		this.handleUpdateMessage(player,data);
	}
	
	protected abstract void handleUpdateMessage(Player player, LazyPacketData updateInfo);

	public static ListTag savePersistentData(Map<TradeRuleType<?>,TradeRule> rules, DataContext<Tag> context) {
		ListTag ruleData = new ListTag();
		for (TradeRule rule : rules.values()) {
            if(rule instanceof IPersistentRule pr)
            {
                CompoundTag entry = pr.writePersistentTag(context);
                if(entry != null)
                {
                    entry.putString("type",LCRegistries.TRADE_RULE.getKey(rule.getType()).toString());
                    ruleData.add(entry);
                }
            }
		}
		return ruleData;
	}

    public static void loadPersistentData(ListTag list, Map<TradeRuleType<?>,TradeRule> tradeRules, DataContext<Tag> context)
    {
        for(int i = 0; i < list.size(); ++i)
        {
            CompoundTag entry = list.getCompound(i);
            try {
                ResourceLocation id = null;
                if(entry.contains("Type"))
                    id = ResourceLocation.parse(entry.getString("Type"));
                else if(entry.contains("type"))
                    id = ResourceLocation.parse(entry.getString("type"));
                if(id == null)
                    continue;
                TradeRuleType<?> type = LCRegistries.TRADE_RULE.get(id);
                if(type == null || !tradeRules.containsKey(type))
                    continue;
                if(tradeRules.get(type) instanceof IPersistentRule pr)
                    pr.readPersistentTag(entry,context);
            } catch (ResourceLocationException ignored) {}
        }
    }
	
	public static JsonObject savePersistentRules(Map<TradeRuleType<?>,TradeRule> rules, DataContext<JsonElement> context) {
		JsonObject json = new JsonObject();
		for (TradeRule rule : rules.values()) {
			if (rule.isActive && rule instanceof IPersistentRule pr) {
				JsonObject entry = pr.writePersistentData(context);
				if (entry != null)
                    json.add(LCRegistries.TRADE_RULE.getKey(rule.getType()).toString(),entry);
			}
		}
		return json;
	}

    public static Map<TradeRuleType<?>,TradeRule> loadPersistentRules(JsonObject parent,String key,DataContext<JsonElement> context) throws JsonSyntaxException,ResourceLocationException
    {
        if(parent.has(key))
        {
            JsonElement element = parent.get(key);
            if(element.isJsonArray())
                return loadOldPersistentRules(GsonHelper.convertToJsonArray(element,key),key,context);
            else
            {
                JsonObject json = GsonHelper.convertToJsonObject(element,key);
                Map<TradeRuleType<?>,TradeRule> result = new HashMap<>();
                for(String idString : json.keySet())
                {
                    try {
                        JsonObject entry = GsonHelper.getAsJsonObject(json,idString);
                        ResourceLocation id = ResourceLocation.parse(idString);
                        TradeRuleType<?> type = LCRegistries.TRADE_RULE.get(id);
                        if(type == null)
                            throw new JsonSyntaxException("Unknown Trade Rule Type: " + id);
                        TradeRule rule = type.create();
                        if(rule instanceof IPersistentRule pr)
                        {
                            pr.loadPersistentData(entry,context);
                            result.put(type,rule);
                        }
                        else
                            throw new JsonSyntaxException("Trade Rule of Type '" + id + "' does not support persistent trader interactions!");
                    } catch (JsonSyntaxException | ResourceLocationException e) { LightmansCurrency.LogError("Error attempting to load persistent trade rule '" + idString + "':",e);}
                }
                return result;
            }
        }
        return new HashMap<>();
    }

    private static Map<TradeRuleType<?>,TradeRule> loadOldPersistentRules(JsonArray list, String name, DataContext<JsonElement> context) throws JsonSyntaxException,ResourceLocationException
    {
        Map<TradeRuleType<?>,TradeRule> result = new HashMap<>();
        for(int i = 0; i < list.size(); ++i)
        {
            try {
                JsonObject entry = GsonHelper.convertToJsonObject(list.get(i),name + "[" + i + "]");
                ResourceLocation id = ResourceLocation.parse(GsonHelper.getAsString(entry,"Type"));
                TradeRuleType<?> type = LCRegistries.TRADE_RULE.get(id);
                if(type == null)
                    throw new JsonSyntaxException("Unknown Trade Rule Type: " + id);
                if(result.containsKey(type))
                    throw new JsonSyntaxException("Duplicate Trade Rule '" + id + "'!\nCannot have two trade rules of the same type on a trader!");
                TradeRule rule = type.create();
                if(rule instanceof IPersistentRule pr)
                {
                    pr.loadPersistentData(entry,context);
                    result.put(type,rule);
                }
                else
                    throw new JsonSyntaxException("Trade Rule of Type '" + id + "' does not support persistent trader interactions!");
            } catch (JsonSyntaxException | ResourceLocationException e) { LightmansCurrency.LogError("Error attempting to load persistent trade rule:",e);}
        }
        return result;
    }

    @Deprecated
    public static Map<TradeRuleType<?>,TradeRule> loadOldRules(CompoundTag compound, String tag, @Nullable ITradeRuleHost host, DataContext<Tag> context)
    {
        Map<TradeRuleType<?>,TradeRule> rules = new HashMap<>();
        if(compound.contains(tag, Tag.TAG_LIST))
        {
            ListTag ruleData = compound.getList(tag, Tag.TAG_COMPOUND);
            List<CompoundTag> allData = new ArrayList<>();
            for(int i = 0; i < ruleData.size(); i++)
                allData.add(ruleData.getCompound(i));
            for(CompoundTag data : allData)
            {
                TradeRule thisRule = load(data,context);
                if(thisRule != null)
                {
                    rules.put(thisRule.getType(),thisRule);
                    thisRule.host = host;
                }
            }
        }
        return rules;
    }

	private static boolean ValidateTradeRuleList(Map<TradeRuleType<?>,TradeRule> rules, ITradeRuleHost host)
	{
		boolean changed = false;
		//Add missing rules
		for(TradeRuleType<?> ruleType : LCRegistries.TRADE_RULE)
		{
			TradeRule rule = ruleType.create();
			if(rule != null && host.allowTradeRule(rule) && rule.allowHost(host))
			{
				rules.put(ruleType,rule);
				changed = true;
			}
            else if(rule != null) //Remove from the map
                rules.remove(ruleType);
		}
        //Remove the data
		return changed;
	}

	public static void ValidateTradeRuleActiveStates(ITradeRuleHost host,Map<TradeRuleType<?>,TradeRule> rules)
	{
		for(TradeRule rule : rules.values())
		{
			if(rule.isActive && !rule.canActivate())
            {
                rule.setActive(false);
                host.setRuleChanged(rule.getType());
            }
		}
	}

    public static boolean AfterRulesLoaded(Map<TradeRuleType<?>,TradeRule> rules, ITradeRuleHost host, boolean validate)
    {
        boolean change = false;
        if(validate)
            change = ValidateTradeRuleList(rules,host);
        for(TradeRule rule : rules.values())
            rule.host = host;
        return change;
    }
	
	public static TradeRule CreateRule(ResourceLocation type)
	{
		TradeRuleType<?> ruleType = LCRegistries.TRADE_RULE.get(type);
		if(ruleType != null)
			return ruleType.create();
		LightmansCurrency.LogError("Could not find a TradeRuleType of type '" + type + "'. Unable to create the Trade Rule.");
		return null;
	}
	
	public static TradeRule Deserialize(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
		String thisType = GsonHelper.getAsString(json, "Type");
		TradeRuleType<?> ruleType = LCRegistries.TRADE_RULE.get(VersionUtil.parseResource(thisType));
		if(ruleType != null)
		{
            TradeRule rule = ruleType.create();
            if(rule instanceof IPersistentRule pr)
            {
                pr.loadPersistentData(json,context);
                rule.setActive(true);
            }
            else
                throw new JsonSyntaxException(thisType + " does not support persistent data loading!");
			return rule;
		}
		throw new JsonSyntaxException("Could not find a deserializer of type '" + thisType + "'.");
	}

	@Nullable
	public static TradeRule getRule(TradeRuleType<?> type, List<TradeRule> rules) {
		for(TradeRule rule : rules)
		{
			if(rule.getType() == type)
				return rule;
		}
		return null;
	}

	public static MoneyValue getBaseCost(TradeData trade, TradeContext context)
	{
		//Don't run the query if no trader is given for context
		if(!context.hasTrader() || !trade.validCost() || !(trade instanceof RuleSupportingTradeData ruleTrade))
			return trade.getCost();

		InternalPriceEvent event = new InternalPriceEvent(trade,context);
		for(TradeRule rule : ruleTrade.getRules())
		{
			if(rule.isActive())
				rule.tradeBaseCost(event);
		}
		return event.getBaseCost();
	}

	protected static class InternalPriceEvent
	{
		public final TradeData trade;
		public final TradeContext context;
		
		private MoneyValue baseCost;
		
		public MoneyValue getBaseCost() { return this.baseCost; }
		public void setBaseCost(MoneyValue baseCost) { this.baseCost = Objects.requireNonNullElse(baseCost,MoneyValue.empty()); }
		private InternalPriceEvent(TradeData trade, TradeContext context)
		{
			this.trade = trade;
			this.context = context;
			this.baseCost = trade.getCost();
		}
	}

    public static <T extends TradeRule> App<RecordCodecBuilder.Mu<T>,Boolean> baseFields()
    {
        return Codec.BOOL.fieldOf("active").forGetter(TradeRule::isActive);
    }

    public static LazyPacketData encodeRules(Supplier<LazyPacketData.Builder> source,ITradeRuleHost host,Player player)
    {
        LazyPacketData.Builder builder = source.get();
        for(TradeRuleType<?> type : host.getRuleMap().keySet())
        {
            LazyPacketData.Builder entry = source.get();
            encodeRule(builder,source,host,type,player);
        }
        return builder.build();
    }

    public static void encodeRule(LazyPacketData.Builder builder,Supplier<LazyPacketData.Builder> source,ITradeRuleHost host,TradeRuleType<?> type,Player player)
    {
        TradeRule rule = host.getRuleOfType(type);
        if(rule != null)
        {
            LazyPacketData.Builder entry = source.get();
            rule.encode(source,entry,player);
            builder.setMap(LCRegistries.TRADE_RULE.getKey(type).toString(),entry);
        }
    }

    public static void decodeRules(LazyPacketData data,ITradeRuleHost host)
    {
        for(String key : data.keySet())
        {
            try {
                ResourceLocation id = ResourceLocation.parse(key);
                TradeRuleType<?> type = LCRegistries.TRADE_RULE.get(id);
                if(type != null)
                {
                    TradeRule rule = host.getRuleOfType(type);
                    if(rule == null)
                        rule = host.addRule(type);
                    if(rule != null)
                        rule.decode(data.getMap(key));
                }
            } catch (ResourceLocationException ignored) {}
        }
    }

}
