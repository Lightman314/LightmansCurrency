package io.github.lightman314.lightmanscurrency.tradedata.rules;

import java.util.List;

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

public abstract class TradeRule {
	
	public static final ResourceLocation ICON_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/traderules.png");
	
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
	
	public ResourceLocation getButtonGUI() { return ICON_TEXTURE; }
	public int getGUIX() { return 0; }
	public int getGUIY() { return 0; }
	
	public static CompoundNBT writeRules(CompoundNBT compound, List<TradeRule> rules)
	{
		return writeRules(compound, rules, "TradeRules");
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
	
	@OnlyIn(Dist.CLIENT)
	public abstract void initTab(TradeRuleScreen screen);
	
	@OnlyIn(Dist.CLIENT)
	public abstract void renderTab(TradeRuleScreen screen, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
	
	@OnlyIn(Dist.CLIENT)
	public abstract void onTabClose(TradeRuleScreen screen);
	
	@OnlyIn(Dist.CLIENT)
	public void onScreenTick(TradeRuleScreen screen) { }
	
}
