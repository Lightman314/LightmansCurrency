package io.github.lightman314.lightmanscurrency.trader.tradedata.rules;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TradeLimit extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "tradelimit2");
	
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
			event.denyTrade(new TranslatableComponent("traderule.lightmanscurrency.tradelimit2.denial", this.count));
			event.denyTrade(new TranslatableComponent("traderule.lightmanscurrency.tradelimit.denial.limit", this.limit));
		}
	}

	@Override
	public void afterTrade(PostTradeEvent event) {
		
		this.count++;
		
		event.markDirty();
		
	}
	
	@Override
	public CompoundTag write(CompoundTag compound) {
		
		compound.putInt("Limit", this.limit);
		compound.putInt("Count", this.count);
		return compound;
	}

	@Override
	public void readNBT(CompoundTag compound) {
		
		if(compound.contains("Limit", Tag.TAG_INT))
			this.limit = compound.getInt("Limit");
		if(compound.contains("Count", Tag.TAG_INT))
			this.count = compound.getInt("Count");
		
	}
	
	@Override
	public IconData getButtonIcon() { return IconData.of(new TranslatableComponent("gui.button.lightmanscurrency.playerlimit2")); }

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
		
		EditBox limitInput;
		Button buttonSetLimit;
		Button buttonClearMemory;
		
		@Override
		public void initTab() {
			
			this.limitInput = this.addCustomRenderable(new EditBox(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 19, 30, 20, new TextComponent("")));
			this.limitInput.setMaxLength(3);
			this.limitInput.setValue(Integer.toString(this.getRule().limit));
			
			this.buttonSetLimit = this.addCustomRenderable(new Button(screen.guiLeft() + 41, screen.guiTop() + 19, 40, 20, new TranslatableComponent("gui.button.lightmanscurrency.playerlimit.setlimit"), this::PressSetLimitButton));
			this.buttonClearMemory = this.addCustomRenderable(new Button(screen.guiLeft() + 10, screen.guiTop() + 50, screen.xSize - 20, 20, new TranslatableComponent("gui.button.lightmanscurrency.playerlimit.clearmemory"), this::PressClearMemoryButton));
		}

		@Override
		public void renderTab(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
			
			screen.getFont().draw(poseStack, new TranslatableComponent("gui.button.lightmanscurrency.playerlimit.info", this.getRule().limit).getString(), screen.guiLeft() + 10, screen.guiTop() + 9, 0xFFFFFF);
			
			if(this.buttonClearMemory.isMouseOver(mouseX, mouseY))
				screen.renderTooltip(poseStack, new TranslatableComponent("gui.button.lightmanscurrency.playerlimit.clearmemory.tooltip"), mouseX, mouseY);
			
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
