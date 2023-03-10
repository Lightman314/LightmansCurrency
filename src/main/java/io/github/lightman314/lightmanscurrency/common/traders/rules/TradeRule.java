package io.github.lightman314.lightmanscurrency.common.traders.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.google.common.base.Supplier;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.TradeCostEvent;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public abstract class TradeRule {
	
	public final ResourceLocation type;
	public final IFormattableTextComponent getName() { return EasyText.translatable("traderule." + type.getNamespace() + "." + type.getPath()); }
	
	private boolean isActive = false;
	public boolean isActive() { return this.isActive; }
	public void setActive(boolean active) { this.isActive = active; }
	
	public void beforeTrade(PreTradeEvent event) {}
	public void tradeCost(TradeCostEvent event) {}
	public void afterTrade(PostTradeEvent event) {}
	
	protected TradeRule(ResourceLocation type)
	{
		this.type = type;
	}
	
	public CompoundNBT save()
	{
		CompoundNBT compound = new CompoundNBT();
		compound.putString("Type", this.type.toString());
		compound.putBoolean("Active", this.isActive);
		this.saveAdditional(compound);
		return compound;
	}
	
	protected abstract void saveAdditional(CompoundNBT compound);
	
	public final void load(CompoundNBT compound)
	{
		this.isActive = compound.getBoolean("Active");
		this.loadAdditional(compound);
	}
	protected abstract void loadAdditional(CompoundNBT compound);
	
	public abstract JsonObject saveToJson(JsonObject json);
	public abstract void loadFromJson(JsonObject json);
	
	public abstract CompoundNBT savePersistentData();
	public abstract void loadPersistentData(CompoundNBT data);
	
	public abstract IconData getButtonIcon();
	
	public final void receiveUpdateMessage(CompoundNBT updateInfo)
	{
		if(updateInfo.contains("SetActive"))
			this.isActive = updateInfo.getBoolean("SetActive");
		this.handleUpdateMessage(updateInfo);
	}
	
	protected abstract void handleUpdateMessage(CompoundNBT updateInfo);
	
	public static CompoundNBT saveRules(CompoundNBT compound, List<TradeRule> rules, String tag)
	{
		ListNBT ruleData = new ListNBT();
		for (TradeRule rule : rules) ruleData.add(rule.save());
		compound.put(tag, ruleData);
		return compound;
	}
	
	public static boolean savePersistentData(CompoundNBT compound, List<TradeRule> rules, String tag) {
		ListNBT ruleData = new ListNBT();
		for (TradeRule rule : rules) {
			CompoundNBT thisRuleData = rule.savePersistentData();
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
	
	public static List<TradeRule> loadRules(CompoundNBT compound, String tag)
	{
		List<TradeRule> rules = new ArrayList<>();
		if(compound.contains(tag, Constants.NBT.TAG_LIST))
		{
			ListNBT ruleData = compound.getList(tag, Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < ruleData.size(); i++)
			{
				CompoundNBT thisRuleData = ruleData.getCompound(i);
				TradeRule thisRule = Deserialize(thisRuleData);
				if(thisRule != null)
					rules.add(thisRule);
			}
		}
		return rules;
	}
	
	public static void loadPersistentData(CompoundNBT compound, List<TradeRule> tradeRules, String tag)
	{
		if(compound.contains(tag, Constants.NBT.TAG_LIST))
		{
			ListNBT ruleData = compound.getList(tag, Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < ruleData.size(); ++i)
			{
				CompoundNBT thisRuleData = ruleData.getCompound(i);
				boolean query = true;
				for(int r = 0; query && r < tradeRules.size(); ++r)
				{
					if(tradeRules.get(r).type.toString().contentEquals(thisRuleData.getString("Type")))
					{
						tradeRules.get(r).loadPersistentData(thisRuleData);
					}
				}
			}
		}
	}
	
	public static List<TradeRule> Parse(JsonArray tradeRuleData)
	{
		List<TradeRule> rules = new ArrayList<>();
		for(int i = 0; i < tradeRuleData.size(); ++i)
		{
			try {
				JsonObject thisRuleData = tradeRuleData.get(i).getAsJsonObject();
				TradeRule thisRule = Deserialize(thisRuleData);
				if(thisRule != null)
					rules.add(thisRule);
			}
			catch(Throwable t) { LightmansCurrency.LogError("Error loading Trade Rule at index " + i + ".", t); }
		}
		return rules;
	}
	
	public static boolean ValidateTradeRuleList(List<TradeRule> rules, Function<TradeRule,Boolean> allowed)
	{
		boolean changed = false;
		for(Supplier<TradeRule> ruleSource : registeredDeserializers.values())
		{
			TradeRule rule = ruleSource.get();
			if(rule != null && allowed.apply(rule) && !HasTradeRule(rules,rule.type))
			{
				rules.add(rule);
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
	public abstract GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule);
	
	@OnlyIn(Dist.CLIENT)
	public static abstract class GUIHandler
	{
		
		protected final TradeRuleScreen screen;
		private final Supplier<TradeRule> rule;
		protected final TradeRule getRuleRaw() { return rule.get(); }
		
		protected GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
		{
			this.screen = screen;
			this.rule = rule;
		}
		
		public abstract void initTab();
		
		public abstract void renderTab(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks);
		
		public abstract void onTabClose();
		
		public void onScreenTick() { }
		
		public <T extends Widget> T addCustomRenderable(T widget)
		{
			return screen.addCustomRenderable(widget);
		}
		
		public <T extends IGuiEventListener> T addCustomWidget(T widget)
		{
			return screen.addCustomWidget(widget);
		}
		
		public <T extends IGuiEventListener> void removeCustomWidget(T widget)
		{
			screen.removeCustomWidget(widget);
		}
		
	}
	
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
	
	public static TradeRule Deserialize(CompoundNBT compound)
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
	
	public static CompoundNBT CreateRuleMessage() { CompoundNBT tag = new CompoundNBT(); tag.putBoolean("Create", true); return tag; }
	public static CompoundNBT RemoveRuleMessage() { CompoundNBT tag = new CompoundNBT(); tag.putBoolean("Remove", true); return tag; }
	
	public static boolean isCreateMessage(CompoundNBT tag) { return tag.contains("Create") && tag.getBoolean("Create"); }
	public static boolean isRemoveMessage(CompoundNBT tag) { return tag.contains("Remove") && tag.getBoolean("Remove"); }
	
}