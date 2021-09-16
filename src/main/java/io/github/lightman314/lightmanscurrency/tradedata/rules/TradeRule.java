package io.github.lightman314.lightmanscurrency.tradedata.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public abstract class TradeRule {
	
	public static final ResourceLocation ICON_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/traderuleicons.png");
	public static final String DEFAULT_TAG = "TradeRules";
	
	public final ResourceLocation type;
	public final ITextComponent getName() { return new TranslationTextComponent("traderule." + type.getNamespace() + "." + type.getPath()); }
	
	public void beforeTrade(PreTradeEvent event) {}
	public void afterTrade(PostTradeEvent event) {}
	
	protected TradeRule(ResourceLocation type)
	{
		this.type = type;
	}
	
	public CompoundNBT getNBT()
	{
		CompoundNBT compound = new CompoundNBT();
		compound.putString("type", this.type.toString());
		return write(compound);
	}
	
	protected abstract CompoundNBT write(CompoundNBT compound);
	
	public abstract void readNBT(CompoundNBT compound);
	
	public ITextComponent getButtonText() { return null; }
	public ResourceLocation getButtonGUI() { return ICON_TEXTURE; }
	public int getGUIX() { return 0; }
	public int getGUIY() { return 0; }
	
	public static CompoundNBT writeRules(CompoundNBT compound, List<TradeRule> rules)
	{
		return writeRules(compound, rules, DEFAULT_TAG);
	}
	
	public static CompoundNBT writeRules(CompoundNBT compound, List<TradeRule> rules, String tag)
	{
		ListNBT ruleData = new ListNBT();
		for(int i = 0; i < rules.size(); i++)
		{
			CompoundNBT thisRuleData = rules.get(i).getNBT();
			if(thisRuleData != null)
				ruleData.add(thisRuleData);
		}
		compound.put(tag, ruleData);
		return compound;
	}
	
	public static List<TradeRule> readRules(CompoundNBT compound)
	{
		return readRules(compound, DEFAULT_TAG);
	}
	
	public static List<TradeRule> readRules(CompoundNBT compound, String tag)
	{
		List<TradeRule> rules = new ArrayList<>();
		if(compound.contains(tag, Constants.NBT.TAG_LIST))
		{
			ListNBT ruleData = compound.getList(tag, Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < ruleData.size(); i++)
			{
				CompoundNBT thisRuleData = ruleData.getCompound(i);
				TradeRule thisRule = ITradeRuleDeserializer.Deserialize(thisRuleData);
				if(thisRule != null)
					rules.add(thisRule);
			}
		}
		return rules;
	}
	
	@OnlyIn(Dist.CLIENT)
	public abstract GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule);
	
	@OnlyIn(Dist.CLIENT)
	public static abstract class GUIHandler
	{
		
		protected final TradeRuleScreen screen;
		private final Supplier<TradeRule> rule;
		protected final TradeRule getRule() { return rule.get(); }
		
		protected GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
		{
			this.screen = screen;
			this.rule = rule;
		}
		
		public abstract void initTab();
		
		public abstract void renderTab(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
		
		public abstract void onTabClose();
		
		public void onScreenTick() { }
		
	}
	
	
	
}
