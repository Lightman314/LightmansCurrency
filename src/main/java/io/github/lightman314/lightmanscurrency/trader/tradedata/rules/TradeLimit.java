package io.github.lightman314.lightmanscurrency.trader.tradedata.rules;

import com.google.common.base.Supplier;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class TradeLimit extends TradeRule{
	
	public static final ResourceLocation OLD_TYPE = new ResourceLocation(LightmansCurrency.MODID, "tradelimit2");
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "trade_limit");
	
	private int limit = 1;
	public int getLimit() { return this.limit; }
	public void setLimit(int newLimit) { this.limit = newLimit; }
	
	int count = 0;
	public void resetCount() { this.count = 0; }
	
	public TradeLimit() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(this.count >= this.limit)
		{
			event.denyTrade(new TranslationTextComponent("traderule.lightmanscurrency.tradelimit2.denial", this.count));
			event.denyTrade(new TranslationTextComponent("traderule.lightmanscurrency.tradelimit.denial.limit", this.limit));
		}
	}

	@Override
	public void afterTrade(PostTradeEvent event) {
		
		this.count++;
		
		event.markDirty();
		
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		
		compound.putInt("Limit", this.limit);
		compound.putInt("Count", this.count);
		return compound;
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) {
		json.addProperty("Limit", this.limit);
		return json;
	}

	@Override
	public void readNBT(CompoundNBT compound) {
		
		if(compound.contains("Limit", Constants.NBT.TAG_INT))
			this.limit = compound.getInt("Limit");
		if(compound.contains("Count", Constants.NBT.TAG_INT))
			this.count = compound.getInt("Count");
		
	}
	
	@Override
	public void loadFromJson(JsonObject json) {
		if(json.has("Limit"))
			this.limit = json.get("Limit").getAsInt();
	}
	
	@Override
	public CompoundNBT savePersistentData() {
		CompoundNBT data = new CompoundNBT();
		data.putInt("Count", this.count);
		return data;
	}
	@Override
	public void loadPersistentData(CompoundNBT data) {
		if(data.contains("Count", Constants.NBT.TAG_INT))
			this.count = data.getInt("Count");
	}
	
	@Override
	public IconData getButtonIcon() { return IconAndButtonUtil.ICON_COUNT; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
	{
		return new GUIHandler(screen, rule);
	}
	
	@OnlyIn(Dist.CLIENT)
	private static class GUIHandler extends TradeRule.GUIHandler
	{
		
		private final TradeLimit getRule()
		{
			if(getRuleRaw() instanceof TradeLimit)
				return (TradeLimit)getRuleRaw();
			return null;
		}
		
		GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
		{
			super(screen, rule);
		}
		
		TextFieldWidget limitInput;
		Button buttonSetLimit;
		Button buttonClearMemory;
		
		@Override
		public void initTab() {
			
			this.limitInput = this.addListener(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 19, 30, 20, new StringTextComponent("")));
			this.limitInput.setMaxStringLength(3);
			this.limitInput.setText(Integer.toString(this.getRule().limit));
			
			this.buttonSetLimit = this.addButton(new Button(screen.guiLeft() + 41, screen.guiTop() + 19, 40, 20, new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit.setlimit"), this::PressSetLimitButton));
			this.buttonClearMemory = this.addButton(new Button(screen.guiLeft() + 10, screen.guiTop() + 50, screen.xSize - 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit.clearmemory"), this::PressClearMemoryButton));
		}

		@Override
		public void renderTab(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			
			this.limitInput.render(matrixStack, mouseX, mouseY, partialTicks);
			
			screen.getFont().drawString(matrixStack, new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit.info", this.getRule().limit).getString(), screen.guiLeft() + 10, screen.guiTop() + 9, 0xFFFFFF);
			
			if(this.buttonClearMemory.isMouseOver(mouseX, mouseY))
				screen.renderTooltip(matrixStack, new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit.clearmemory.tooltip"), mouseX, mouseY);
			
		}

		@Override
		public void onTabClose() {
			
			this.screen.removeListener(this.limitInput);
			this.screen.removeButton(this.buttonSetLimit);
			this.screen.removeButton(this.buttonClearMemory);
			
		}
		
		@Override
		public void onScreenTick() {
			
			this.limitInput.tick();
			
			TextInputUtil.whitelistInteger(this.limitInput, 1, 100);
			
		}
		
		void PressSetLimitButton(Button button)
		{
			this.getRule().limit = MathUtil.clamp(TextInputUtil.getIntegerValue(this.limitInput), 1, 100);
			this.screen.markRulesDirty();
		}
		
		void PressClearMemoryButton(Button button)
		{
			this.getRule().resetCount();
			this.screen.markRulesDirty();
		}
		
	}
	
}
