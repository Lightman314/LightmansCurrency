package io.github.lightman314.lightmanscurrency.common.traders.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TradeRule {

	private static final List<IRuleLoadListener> LISTENERS = new ArrayList<>();
	private static final List<String> IGNORE_MISSING = new ArrayList<>();
	public static void addLoadListener(IRuleLoadListener listener)
	{
		if(LISTENERS.contains(listener))
			return;
		LISTENERS.add(listener);
	}
	public static void addIgnoreMissing(String oldType)
	{
		if(IGNORE_MISSING.contains(oldType))
			return;
		IGNORE_MISSING.add(oldType);
	}

	public final TradeRuleType<?> type;
	public static String translationKeyOfType(ResourceLocation ruleType) { return "traderule." + ruleType.getNamespace() + "." + ruleType.getPath(); }
	public static MutableComponent nameOfType(ResourceLocation ruleType) { return Component.translatable(translationKeyOfType(ruleType)); }
	public final MutableComponent getName() { return nameOfType(this.type.type); }
	public abstract IconData getIcon();

	private ITradeRuleHost host = null;
	@Nullable
	protected ITradeRuleHost getHost() { return this.host; }
	protected void setHost(@Nullable ITradeRuleHost host) { this.host = host; }

	private boolean isActive = false;
	public boolean isActive() { return this.canActivate(this.host) && this.isActive; }
	public void setActive(boolean active) { this.isActive = active; }

	protected boolean allowHost(ITradeRuleHost host)
	{
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
	public void afterTrade(PostTradeEvent event) {}
	protected void tradeBaseCost(InternalPriceEvent query) {}

	protected TradeRule(TradeRuleType<?> type) { this.type = type; }

	public CompoundTag save(HolderLookup.Provider lookup)
	{
		CompoundTag compound = new CompoundTag();
		compound.putString("Type", this.type.toString());
		compound.putBoolean("Active", this.isActive);
		this.saveAdditional(compound,lookup);
		return compound;
	}
	protected abstract void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup);
	
	public final void load(CompoundTag compound, HolderLookup.Provider lookup)
	{
		this.isActive = compound.getBoolean("Active");
		this.loadAdditional(compound, lookup);
	}
	protected abstract void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup);

	public abstract JsonObject saveToJson(JsonObject json, HolderLookup.Provider lookup);
	public abstract void loadFromJson(JsonObject json, HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException;
	
	public abstract CompoundTag savePersistentData(HolderLookup.Provider provider);
	public abstract void loadPersistentData(CompoundTag data, HolderLookup.Provider lookup);
	
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

	public static void saveRules(CompoundTag compound, List<TradeRule> rules, String tag, HolderLookup.Provider lookup)
	{
		ListTag ruleData = new ListTag();
		for (TradeRule rule : rules) ruleData.add(rule.save(lookup));
		compound.put(tag, ruleData);
	}
	
	public static boolean savePersistentData(CompoundTag compound, List<TradeRule> rules, String tag, HolderLookup.Provider lookup) {
		ListTag ruleData = new ListTag();
		for (TradeRule rule : rules) {
			CompoundTag thisRuleData = rule.savePersistentData(lookup);
			if (thisRuleData != null) {
				thisRuleData.putString("Type", rule.type.toString());
				ruleData.add(thisRuleData);
			}
		}
		if(ruleData.isEmpty())
			return false;
		compound.put(tag, ruleData);
		return true;
	}
	
	public static JsonArray saveRulesToJson(List<TradeRule> rules, HolderLookup.Provider lookup) {
		JsonArray ruleData = new JsonArray();
		for (TradeRule rule : rules) {
			if (rule.isActive) {
				JsonObject thisRuleData = rule.saveToJson(new JsonObject(), lookup);
				if (thisRuleData != null) {
					thisRuleData.addProperty("Type", rule.type.toString());
					ruleData.add(thisRuleData);
				}
			}
		}
		return ruleData;
	}

	public static List<TradeRule> loadRules(CompoundTag compound, String tag, @Nullable ITradeRuleHost host, HolderLookup.Provider lookup)
	{
		List<TradeRule> rules = new ArrayList<>();
		if(compound.contains(tag, Tag.TAG_LIST))
		{
			ListTag ruleData = compound.getList(tag, Tag.TAG_COMPOUND);
			List<CompoundTag> allData = new ArrayList<>();
			for(int i = 0; i < ruleData.size(); i++)
				allData.add(ruleData.getCompound(i));
			LISTENERS.forEach(l -> l.beforeLoading(host,allData,rules));
			for(CompoundTag data : allData)
			{
				TradeRule thisRule = Deserialize(data, lookup);
				if(thisRule != null)
				{
					rules.add(thisRule);
					thisRule.host = host;
				}
			}
			LISTENERS.forEach(l -> l.afterLoading(host,allData,rules));
		}
		return rules;
	}
	
	public static void loadPersistentData(CompoundTag compound, List<TradeRule> tradeRules, String tag, HolderLookup.Provider lookup)
	{
		if(compound.contains(tag, Tag.TAG_LIST))
		{
			ListTag ruleData = compound.getList(tag, Tag.TAG_COMPOUND);
			for(int i = 0; i < ruleData.size(); ++i)
			{
				CompoundTag thisRuleData = ruleData.getCompound(i);
				boolean query = true;
				for(int r = 0; query && r < tradeRules.size(); ++r)
				{
					if(tradeRules.get(r).type.toString().contentEquals(thisRuleData.getString("Type")))
					{
						tradeRules.get(r).loadPersistentData(thisRuleData, lookup);
						query = false;
					}
				}
			}
		}
	}

	public static List<TradeRule> Parse(JsonArray tradeRuleData, @Nullable ITradeRuleHost host, HolderLookup.Provider lookup)
	{
		List<TradeRule> rules = new ArrayList<>();
		for(int i = 0; i < tradeRuleData.size(); ++i)
		{
			try {
				JsonObject thisRuleData = tradeRuleData.get(i).getAsJsonObject();
				TradeRule thisRule = Deserialize(thisRuleData, lookup);
				thisRule.host = host;
				rules.add(thisRule);
			}
			catch(Throwable t) { LightmansCurrency.LogError("Error loading Trade Rule at index " + i + ".", t); }
		}
		return rules;
	}

	public static boolean ValidateTradeRuleList(List<TradeRule> rules, ITradeRuleHost host)
	{
		boolean changed = false;
		//Add missing rules
		for(TradeRuleType<?> ruleType : TraderAPI.API.GetAllTradeRuleTypes())
		{
			TradeRule rule = ruleType.createNew();
			if(rule != null && host.allowTradeRule(rule) && rule.allowHost(host) && !HasTradeRule(rules,rule.type.type))
			{
				rules.add(rule);
				rule.host = host;
				changed = true;
			}
		}
		//Confirm no duplicates
		for(int i = 0; i < rules.size(); ++i)
		{
			TradeRule r1 = rules.get(i);
			for(int j = i + 1; j < rules.size(); ++j)
			{
				if(rules.get(j).type == r1.type)
				{
					rules.remove(j--);
					changed = true;
				}
			}
		}
		return changed;
	}

	public static boolean ValidateTradeRuleActiveStates(List<TradeRule> rules)
	{
		boolean changed = false;
		for(TradeRule rule : rules)
		{
			if(rule.isActive && !rule.canActivate())
			{
				rule.isActive = false;
				changed = true;
			}
		}
		return changed;
	}
	
	public static boolean HasTradeRule(List<TradeRule> rules, ResourceLocation type) { return GetTradeRule(rules, type) != null; }

	@Nullable
	public static TradeRule GetTradeRule(List<TradeRule> rules, ResourceLocation type)
	{
		for(TradeRule rule : rules)
		{
			if(rule.type.type.equals(type))
				return rule;
		}
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	
	public abstract TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent);

	
	public static TradeRule CreateRule(ResourceLocation type)
	{
		TradeRuleType<?> ruleType = TraderAPI.API.GetTradeRuleType(type);
		if(ruleType != null)
			return ruleType.createNew();
		LightmansCurrency.LogError("Could not find a TradeRuleType of type '" + type + "'. Unable to create the Trade Rule.");
		return null;
	}

	
	public static TradeRule Deserialize(CompoundTag compound, HolderLookup.Provider lookup)
	{
		String thisType = compound.contains("Type") ? compound.getString("Type") : compound.getString("type");
		TradeRuleType<?> ruleType = TraderAPI.API.GetTradeRuleType(VersionUtil.parseResource(thisType));
		if(ruleType != null)
			return ruleType.load(compound, lookup);
		if(IGNORE_MISSING.contains(thisType))
			return null;
		LightmansCurrency.LogError("Could not find a TradeRuleType of type '" + thisType + "'. Unable to load the Trade Rule.");
		return null;
	}

	
	public static TradeRule Deserialize(JsonObject json, HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
		String thisType = GsonHelper.getAsString(json, "Type");
		TradeRuleType<?> ruleType = TraderAPI.API.GetTradeRuleType(VersionUtil.parseResource(thisType));
		if(ruleType != null)
		{
			TradeRule rule = ruleType.loadFromJson(json, lookup);
			rule.setActive(true);
			return rule;
		}
		throw new JsonSyntaxException("Could not find a deserializer of type '" + thisType + "'.");
	}

	@Nullable
	public static TradeRule getRule(ResourceLocation type, List<TradeRule> rules) {
		for(TradeRule rule : rules)
		{
			if(rule.type.type.equals(type))
				return rule;
		}
		return null;
	}
	
	public static CompoundTag CreateRuleMessage() { CompoundTag tag = new CompoundTag(); tag.putBoolean("Create", true); return tag; }
	public static CompoundTag RemoveRuleMessage() { CompoundTag tag = new CompoundTag(); tag.putBoolean("Remove", true); return tag; }
	
	public static boolean isCreateMessage(CompoundTag tag) { return tag.contains("Create") && tag.getBoolean("Create"); }
	public static boolean isRemoveMessage(CompoundTag tag) { return tag.contains("Remove") && tag.getBoolean("Remove"); }

	
	public static MoneyValue getBaseCost(TradeData trade, TradeContext context)
	{
		//Don't run the query if no trader is given for context
		if(!context.hasTrader() || !trade.validCost())
			return trade.getCost();

		InternalPriceEvent event = new InternalPriceEvent(trade,context);
		for(TradeRule rule : trade.getRules())
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

}
