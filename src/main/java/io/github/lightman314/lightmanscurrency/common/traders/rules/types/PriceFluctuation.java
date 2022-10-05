package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.base.Supplier;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeUnit;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PriceFluctuation extends TradeRule {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "price_fluctuation");
	
	long duration = TimeUtil.DURATION_DAY;
	int fluctuation = 10;
	public int getFluctuation() { return this.fluctuation; }
	public void setFluctuation(int percent) { this.fluctuation = MathUtil.clamp(percent, 0, 100); }
	
	public PriceFluctuation() { super(TYPE); }
	
	private static List<Long> debuggedSeeds = new ArrayList<>();
	private static List<Long> debuggedTraderFactors = new ArrayList<>();
	
	private static void debugTraderFactor(long factor, long traderID, int tradeIndex)
	{
		if(debuggedTraderFactors.contains(factor))
			return;
		LightmansCurrency.LogDebug("Trader Seed Factor for trader with id '" + traderID + "' and trade index '" + tradeIndex + "' is " + factor);
		debuggedTraderFactors.add(factor);
	}
	
	private static void debugFlux(long seed, int maxFlux, int flux)
	{
		if(debuggedSeeds.contains(seed))
			return;
		LightmansCurrency.LogDebug("Price Fluctuation for trade with seed '" + (seed) + "' and max fluctuation of " + maxFlux + "% is " + flux + "%");
		debuggedSeeds.add(seed);
	}
	
	private long getTraderSeedFactor(TradeCostEvent event) {
		long traderID = event.getTrader().getID();
		int tradeIndex = event.getTradeIndex();
		long factor = ((traderID + 1) << 32) + tradeIndex;
		debugTraderFactor(factor, traderID, tradeIndex);
		return factor;
	}
	
	private double randomizePriceMultiplier(long traderSeedFactor)
	{
		//Have the seed be constant during the given duration
		long seed = TimeUtil.getCurrentTime() / this.duration;
		int fluct = new Random(seed * traderSeedFactor).nextInt(-this.fluctuation, this.fluctuation + 1);
		debugFlux(seed * traderSeedFactor, this.fluctuation, fluct);
		
		return 1d + ((double)fluct/100d);
	}
	
	@Override
	public void tradeCost(TradeCostEvent event) {
		event.applyCostMultiplier(this.randomizePriceMultiplier(this.getTraderSeedFactor(event)));
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		
		compound.putLong("Duration", this.duration);
		compound.putInt("Fluctuation", this.fluctuation);
		
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound) {
		
		this.duration = compound.getLong("Duration");
		if(this.duration <= 0)
			this.duration = TimeUtil.DURATION_DAY;
		
		this.fluctuation = compound.getInt("Fluctuation");
		
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) {
		
		json.addProperty("Duration", this.duration);
		json.addProperty("Fluctuation", this.fluctuation);
		
		return json;
	}
	
	@Override
	public void loadFromJson(JsonObject json) {
		if(json.has("Duration"))
			this.duration = json.get("Duration").getAsLong();
		if(json.has("Fluctuation"))
			this.fluctuation = json.get("Fluctuation").getAsInt();
	}
	
	@Override
	public CompoundTag savePersistentData() { return null; }
	@Override
	public void loadPersistentData(CompoundTag data) {}
	
	@Override
	public IconData getButtonIcon() { return IconAndButtonUtil.ICON_PRICE_FLUCTUATION; }
	
	@Override
	protected void handleUpdateMessage(CompoundTag updateInfo) {
		if(updateInfo.contains("Duration"))
			this.duration = updateInfo.getLong("Duration");
		if(updateInfo.contains("Fluctuation"))
			this.fluctuation = updateInfo.getInt("Fluctuation");
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule) { return new GuiHandler(screen, rule); }
	
	@OnlyIn(Dist.CLIENT)
	private static class GuiHandler extends TradeRule.GUIHandler
	{
		
		protected final PriceFluctuation getRule()
		{
			if(getRuleRaw() instanceof PriceFluctuation)
				return (PriceFluctuation)getRuleRaw();
			return null;
		}

		GuiHandler(TradeRuleScreen screen, Supplier<TradeRule> rule) { super(screen, rule); }
		
		EditBox fluctuationInput;
		Button buttonSetFluctuation;
		
		TimeInputWidget durationInput;
		
		@Override
		public void initTab() {
			this.fluctuationInput = this.addCustomRenderable(new EditBox(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, 20, 20, new TextComponent("")));
			this.fluctuationInput.setMaxLength(2);
			this.fluctuationInput.setValue(Integer.toString(this.getRule().fluctuation));
			
			this.buttonSetFluctuation = this.addCustomRenderable(new Button(screen.guiLeft() + 110, screen.guiTop() + 10, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.discount.set"), this::PressSetFluctuationButton));
			
			this.durationInput = this.addCustomRenderable(new TimeInputWidget(screen.guiLeft() + 48, screen.guiTop() + 75, 10, TimeUnit.DAY, TimeUnit.MINUTE, this::addCustomRenderable, this::onTimeSet));
			this.durationInput.setTime(this.getRule().duration);
			
		}

		@Override
		public void renderTab(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
			if(getRule() == null)
				return;
			
			this.screen.getFont().draw(poseStack, new TranslatableComponent("gui.lightmanscurrency.fluctuation.tooltip"), this.fluctuationInput.x + this.fluctuationInput.getWidth() + 4, this.fluctuationInput.y + 3, 0xFFFFFF);
			
			TextRenderUtil.drawCenteredMultilineText(poseStack, new TranslatableComponent("gui.button.lightmanscurrency.price_fluctuation.info", this.getRule().fluctuation, new TimeData(this.getRule().duration).getShortString()), this.screen.guiLeft() + 10, this.screen.xSize - 20, this.screen.guiTop() + 35, 0xFFFFFF);
			
		}
		
		@Override
		public void onScreenTick() {
			TextInputUtil.whitelistInteger(this.fluctuationInput, 0, 99);
		}

		@Override
		public void onTabClose() {
			
			this.removeCustomWidget(this.fluctuationInput);
			this.removeCustomWidget(this.buttonSetFluctuation);
			this.durationInput.removeChildren(this::removeCustomWidget);
			this.removeCustomWidget(this.durationInput);
			
		}
		
		void PressSetFluctuationButton(Button button)
		{
			int fluctuation = TextInputUtil.getIntegerValue(this.fluctuationInput, 1);
			this.getRule().fluctuation = fluctuation;
			CompoundTag updateInfo = new CompoundTag();
			updateInfo.putInt("Fluctuation", fluctuation);
			this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
		}
		
		public void onTimeSet(TimeData newTime)
		{
			this.getRule().duration = newTime.miliseconds;
			CompoundTag updateInfo = new CompoundTag();
			updateInfo.putLong("Duration", newTime.miliseconds);
			this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
		}
		
	}

}