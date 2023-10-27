package io.github.lightman314.lightmanscurrency.common.traders.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Supplier;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.TradeCostEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TradeRule {
	
	public final ResourceLocation type;
	public static MutableComponent nameOfType(ResourceLocation ruleType) { return EasyText.translatable("traderule." + ruleType.getNamespace() + "." + ruleType.getPath());
	}
	public final MutableComponent getName() { return nameOfType(this.type); }

	private ITradeRuleHost host = null;

	private boolean isActive = false;
	public boolean isActive() { return this.canActivate(this.host) && this.isActive; }
	public void setActive(boolean active) { this.isActive = active; }

	protected boolean allowHost(@Nonnull ITradeRuleHost host)
	{
		if(this.onlyAllowOnTraders() && !host.isTrader())
			return false;
		if(this.onlyAllowOnTrades() && !host.isTrade())
			return false;
		return true;
	}
	public boolean canActivate() { return this.canActivate(this.host); }
	protected boolean canActivate(@Nullable ITradeRuleHost host) { return this.allowHost(host); }

	protected boolean onlyAllowOnTraders() { return false; }
	protected boolean onlyAllowOnTrades() { return false; }

	public void beforeTrade(PreTradeEvent event) {}
	public void tradeCost(TradeCostEvent event) {}
	public void afterTrade(PostTradeEvent event) {}
	
	protected TradeRule(ResourceLocation type) { this.type = type; }

	public CompoundTag save()
	{
		CompoundTag compound = new CompoundTag();
		compound.putString("Type", this.type.toString());
		compound.putBoolean("Active", this.isActive);
		this.saveAdditional(compound);
		return compound;
	}
	
	protected abstract void saveAdditional(CompoundTag compound);
	
	public final void load(CompoundTag compound)
	{
		this.isActive = compound.getBoolean("Active");
		this.loadAdditional(compound);
	}
	protected abstract void loadAdditional(CompoundTag compound);
	
	public abstract JsonObject saveToJson(JsonObject json);
	public abstract void loadFromJson(JsonObject json);
	
	public abstract CompoundTag savePersistentData();
	public abstract void loadPersistentData(CompoundTag data);
	
	public final void receiveUpdateMessage(CompoundTag updateInfo)
	{
		if(updateInfo.contains("SetActive"))
			this.isActive = updateInfo.getBoolean("SetActive");
		this.handleUpdateMessage(updateInfo);
	}
	
	protected abstract void handleUpdateMessage(CompoundTag updateInfo);
	
	public static CompoundTag saveRules(CompoundTag compound, List<TradeRule> rules, String tag)
	{
		ListTag ruleData = new ListTag();
		for (TradeRule rule : rules) ruleData.add(rule.save());
		compound.put(tag, ruleData);
		return compound;
	}
	
	public static boolean savePersistentData(CompoundTag compound, List<TradeRule> rules, String tag) {
		ListTag ruleData = new ListTag();
		for (TradeRule rule : rules) {
			CompoundTag thisRuleData = rule.savePersistentData();
			if (thisRuleData != null) {
				thisRuleData.putString("Type", rule.type.toString());
				ruleData.add(thisRuleData);
			}
		}
		if(ruleData.size() == 0)
			return false;
		compound.put(tag, ruleData);
		return true;
	}
	
	public static JsonArray saveRulesToJson(List<TradeRule> rules) {
		JsonArray ruleData = new JsonArray();
		for (TradeRule rule : rules) {
			if (rule.isActive) {
				JsonObject thisRuleData = rule.saveToJson(new JsonObject());
				if (thisRuleData != null) {
					thisRuleData.addProperty("Type", rule.type.toString());
					ruleData.add(thisRuleData);
				}
			}
		}
		return ruleData;
	}

	@Deprecated(since = "2.1.1.0")
	public static List<TradeRule> loadRules(CompoundTag compound, String tag) { return loadRules(compound, tag, null); }

	public static List<TradeRule> loadRules(CompoundTag compound, String tag, ITradeRuleHost host)
	{
		List<TradeRule> rules = new ArrayList<>();
		if(compound.contains(tag, Tag.TAG_LIST))
		{
			ListTag ruleData = compound.getList(tag, Tag.TAG_COMPOUND);
			for(int i = 0; i < ruleData.size(); i++)
			{
				CompoundTag thisRuleData = ruleData.getCompound(i);
				TradeRule thisRule = Deserialize(thisRuleData);
				if(thisRule != null)
				{
					rules.add(thisRule);
					if(host != null)
						thisRule.host = host;
				}
			}
		}
		return rules;
	}
	
	public static void loadPersistentData(CompoundTag compound, List<TradeRule> tradeRules, String tag)
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
						tradeRules.get(r).loadPersistentData(thisRuleData);
						query = false;
					}
				}
			}
		}
	}

	/**
	 * @deprecated Use host sensitive version to avoid issues.
	 */
	@Deprecated(since = "2.1.1.3")
	public static List<TradeRule> Parse(JsonArray tradeRuleData) { return Parse(tradeRuleData, null); }

	public static List<TradeRule> Parse(JsonArray tradeRuleData, ITradeRuleHost host)
	{
		List<TradeRule> rules = new ArrayList<>();
		for(int i = 0; i < tradeRuleData.size(); ++i)
		{
			try {
				JsonObject thisRuleData = tradeRuleData.get(i).getAsJsonObject();
				TradeRule thisRule = Deserialize(thisRuleData);
				thisRule.host = host;
				rules.add(thisRule);
			}
			catch(Throwable t) { LightmansCurrency.LogError("Error loading Trade Rule at index " + i + ".", t); }
		}
		return rules;
	}



	public static boolean ValidateTradeRuleList(@Nonnull List<TradeRule> rules, @Nonnull ITradeRuleHost host)
	{
		boolean changed = false;
		for(Supplier<TradeRule> ruleSource : registeredDeserializers.values())
		{
			TradeRule rule = ruleSource.get();
			if(rule != null && host.allowTradeRule(rule) && rule.allowHost(host) && !HasTradeRule(rules,rule.type))
			{
				rules.add(rule);
				rule.host = host;
				changed = true;
			}
		}
		return changed;
	}

	public static boolean ValidateTradeRuleActiveStates(@Nonnull List<TradeRule> rules)
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
	
	public static TradeRule GetTradeRule(List<TradeRule> rules, ResourceLocation type)
	{
		for(TradeRule rule : rules)
		{
			if(rule.type.equals(type))
				return rule;
		}
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public abstract TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent);
	
	/**
	 * Trade Rule Deserialization
	 */
	static final Map<String,Supplier<TradeRule>> registeredDeserializers = new HashMap<>();
	
	public static void RegisterDeserializer(ResourceLocation type, Supplier<TradeRule> deserializer)
	{
		RegisterDeserializer(type, deserializer, false);
	}
	
	public static void RegisterDeserializer(ResourceLocation type, Supplier<TradeRule> deserializer, boolean suppressDebugMessage)
	{
		RegisterDeserializer(type.toString(), deserializer, suppressDebugMessage);
		
	}
	
	private static void RegisterDeserializer(String type, Supplier<TradeRule> deserializer, boolean suppressDebugMessage)
	{
		if(registeredDeserializers.containsKey(type))
		{
			LightmansCurrency.LogWarning("A trade rule deserializer of type '" + type + "' has already been registered.");
			return;
		}
		registeredDeserializers.put(type, deserializer);
		if(!suppressDebugMessage)
			LightmansCurrency.LogInfo("Registered trade rule deserializer of type " + type);
	}
	
	public static TradeRule CreateRule(ResourceLocation ruleType)
	{
		String thisType = ruleType.toString();
		AtomicReference<TradeRule> data = new AtomicReference<TradeRule>();
		registeredDeserializers.forEach((type,deserializer) -> {
			if(thisType.equals(type))
			{
				TradeRule rule = deserializer.get();
				data.set(rule);
			}	
		});
		if(data.get() != null)
			return data.get();
		LightmansCurrency.LogError("Could not find a deserializer of type '" + thisType + "'. Unable to load the Trade Rule.");
		return null;
	}
	
	public static TradeRule Deserialize(CompoundTag compound)
	{
		String thisType = compound.contains("Type") ? compound.getString("Type") : compound.getString("type");
		if(registeredDeserializers.containsKey(thisType))
		{
			try {
				TradeRule rule = registeredDeserializers.get(thisType).get();
				rule.load(compound);
				return rule;
			} catch(Throwable t) { LightmansCurrency.LogError("Error deserializing trade rule:", t); }
		}
		LightmansCurrency.LogError("Could not find a deserializer of type '" + thisType + "'. Unable to load the Trade Rule.");
		return null;
	}
	
	public static TradeRule Deserialize(JsonObject json) throws Exception{
		String thisType = json.get("Type").getAsString();
		if(registeredDeserializers.containsKey(thisType))
		{
			TradeRule rule = registeredDeserializers.get(thisType).get();
			rule.loadFromJson(json);
			rule.setActive(true);
			return rule;
		}
		throw new Exception("Could not find a deserializer of type '" + thisType + "'.");
	}
	
	public static TradeRule getRule(ResourceLocation type, List<TradeRule> rules) {
		for(TradeRule rule : rules)
		{
			if(rule.type.equals(type))
				return rule;
		}
		return null;
	}
	
	public static CompoundTag CreateRuleMessage() { CompoundTag tag = new CompoundTag(); tag.putBoolean("Create", true); return tag; }
	public static CompoundTag RemoveRuleMessage() { CompoundTag tag = new CompoundTag(); tag.putBoolean("Remove", true); return tag; }
	
	public static boolean isCreateMessage(CompoundTag tag) { return tag.contains("Create") && tag.getBoolean("Create"); }
	public static boolean isRemoveMessage(CompoundTag tag) { return tag.contains("Remove") && tag.getBoolean("Remove"); }
	
}
