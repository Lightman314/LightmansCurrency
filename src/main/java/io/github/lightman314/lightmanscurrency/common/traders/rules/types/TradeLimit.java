package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.common.base.Supplier;
import com.google.gson.JsonObject;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
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
			event.addDenial(EasyText.translatable("traderule.lightmanscurrency.tradelimit2.denial", this.count));
			event.addDenial(EasyText.translatable("traderule.lightmanscurrency.tradelimit.denial.limit", this.limit));
		}
		else
			event.addHelpful(EasyText.translatable("traderule.lightmanscurrency.tradelimit2.info", this.count, this.limit));
	}

	@Override
	public void afterTrade(PostTradeEvent event) {
		
		this.count++;
		
		event.markDirty();
		
	}
	
	@Override
	protected void saveAdditional(CompoundNBT compound) {
		
		compound.putInt("Limit", this.limit);
		compound.putInt("Count", this.count);
		
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) {
		json.addProperty("Limit", this.limit);
		return json;
	}

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		
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
	public void handleUpdateMessage(CompoundNBT updateInfo)
	{
		if(updateInfo.contains("Limit"))
		{
			this.limit = updateInfo.getInt("Limit");
		}
		else if(updateInfo.contains("ClearMemory"))
		{
			this.count = 0;
		}
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
		
		private TradeLimit getRule()
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
			
			this.limitInput = this.addCustomRenderable(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 19, 30, 20, EasyText.empty()));
			this.limitInput.setMaxLength(3);
			this.limitInput.setValue(Integer.toString(this.getRule().limit));
			
			this.buttonSetLimit = this.addCustomRenderable(new Button(screen.guiLeft() + 41, screen.guiTop() + 19, 40, 20, EasyText.translatable("gui.button.lightmanscurrency.playerlimit.setlimit"), this::PressSetLimitButton));
			this.buttonClearMemory = this.addCustomRenderable(new Button(screen.guiLeft() + 10, screen.guiTop() + 50, screen.xSize - 20, 20, EasyText.translatable("gui.button.lightmanscurrency.playerlimit.clearmemory"), this::PressClearMemoryButton));
		}

		@Override
		public void renderTab(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks) {
			
			screen.getFont().draw(poseStack, EasyText.translatable("gui.button.lightmanscurrency.playerlimit.info", this.getRule().limit).getString(), screen.guiLeft() + 10, screen.guiTop() + 9, 0xFFFFFF);
			
			if(this.buttonClearMemory.isMouseOver(mouseX, mouseY))
				screen.renderTooltip(poseStack, EasyText.translatable("gui.button.lightmanscurrency.playerlimit.clearmemory.tooltip"), mouseX, mouseY);
			
		}

		@Override
		public void onTabClose() {
			
			this.removeCustomWidget(this.limitInput);
			this.removeCustomWidget(this.buttonSetLimit);
			this.removeCustomWidget(this.buttonClearMemory);
			
		}
		
		@Override
		public void onScreenTick() {
			
			TextInputUtil.whitelistInteger(this.limitInput, 1, 100);
			
		}
		
		void PressSetLimitButton(Button button)
		{
			int limit = MathUtil.clamp(TextInputUtil.getIntegerValue(this.limitInput), 1, 100);
			this.getRule().limit = limit;
			CompoundNBT updateInfo = new CompoundNBT();
			updateInfo.putInt("Limit", limit);
			this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
		}
		
		void PressClearMemoryButton(Button button)
		{
			this.getRule().resetCount();
			CompoundNBT updateInfo = new CompoundNBT();
			updateInfo.putBoolean("ClearMemory", true);
			this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
		}
		
	}
	
}