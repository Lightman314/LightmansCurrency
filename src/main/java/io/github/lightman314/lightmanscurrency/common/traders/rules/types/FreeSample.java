package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Supplier;
import com.google.gson.JsonObject;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.TradeCostEvent;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class FreeSample extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "free_sample");
	
	List<UUID> memory = new ArrayList<>();
	public void resetMemory() { this.memory.clear(); }
	
	public FreeSample() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event)
	{
		if(this.giveDiscount(event))
			event.addHelpful(EasyText.translatable("traderule.lightmanscurrency.free_sample.alert"));
	}
	
	@Override
	public void tradeCost(TradeCostEvent event) {
		if(this.giveDiscount(event))
			event.applyCostMultiplier(0d);
	}

	@Override
	public void afterTrade(PostTradeEvent event) {
		
		if(this.giveDiscount(event))
		{
			this.addToMemory(event.getPlayerReference().id);
			event.markDirty();
		}
	}
	
	private boolean giveDiscount(TradeEvent event) {
		return this.giveDiscount(event.getPlayerReference().id) && event.getTrade().getTradeDirection() != TradeDirection.PURCHASE;
	}
	
	private void addToMemory(UUID playerID) {
		if(!this.memory.contains(playerID))
			this.memory.add(playerID);
	}
	
	public boolean giveDiscount(UUID playerID) {
		return !this.givenFreeSample(playerID);
	}
	
	private boolean givenFreeSample(UUID playerID) {
		return this.memory.contains(playerID);
	}
	
	@Override
	protected void saveAdditional(CompoundNBT compound) {

		ListNBT memoryList = new ListNBT();
		for(UUID entry : this.memory)
		{
			CompoundNBT tag = new CompoundNBT();
			tag.putUUID("ID", entry);
			memoryList.add(tag);
		}
		compound.put("Memory", memoryList);
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) { return json; }

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		
		if(compound.contains("Memory", Constants.NBT.TAG_LIST))
		{
			this.memory.clear();
			ListNBT memoryList = compound.getList("Memory", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < memoryList.size(); i++)
			{
				CompoundNBT tag = memoryList.getCompound(i);
				if(tag.contains("ID"))
					this.memory.add(tag.getUUID("ID"));
				else if(tag.contains("id"))
					this.memory.add(tag.getUUID("id"));
				
			}
		}
	}
	
	@Override
	public CompoundNBT savePersistentData() {
		CompoundNBT data = new CompoundNBT();
		ListNBT memoryList = new ListNBT();
		for(UUID entry : this.memory)
		{
			CompoundNBT tag = new CompoundNBT();
			tag.putUUID("ID", entry);
			memoryList.add(tag);
		}
		data.put("Memory", memoryList);
		return data;
	}
	
	@Override
	public void loadPersistentData(CompoundNBT data) {
		if(data.contains("Memory", Constants.NBT.TAG_LIST))
		{
			this.memory.clear();
			ListNBT memoryList = data.getList("Memory", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < memoryList.size(); i++)
			{
				CompoundNBT tag = memoryList.getCompound(i);
				if(tag.contains("ID"))
					this.memory.add(tag.getUUID("ID"));
				else if(tag.contains("id"))
					this.memory.add(tag.getUUID("id"));
			}
		}
	}
	
	@Override
	public void loadFromJson(JsonObject json) { }
	
	@Override
	protected void handleUpdateMessage(CompoundNBT updateInfo) {
		if(updateInfo.contains("ClearData"))
			this.memory.clear();
	}
	
	@Override
	public IconData getButtonIcon() { return IconAndButtonUtil.ICON_FREE_SAMPLE; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
	{
		return new GUIHandler(screen, rule);
	}
	
	@OnlyIn(Dist.CLIENT)
	private static class GUIHandler extends TradeRule.GUIHandler
	{
		
		private FreeSample getRule()
		{
			if(getRuleRaw() instanceof FreeSample)
				return (FreeSample)getRuleRaw();
			return null;
		}
		
		GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
		{
			super(screen, rule);
		}
		
		Button buttonClearMemory;
		
		@Override
		public void initTab() {
			
			this.buttonClearMemory = this.addCustomRenderable(new Button(screen.guiLeft() + 10, screen.guiTop() + 50, screen.xSize - 20, 20, EasyText.translatable("gui.button.lightmanscurrency.free_sample.reset"), this::PressClearMemoryButton));
			
		}

		@Override
		public void renderTab(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks) {
			
			if(this.buttonClearMemory.isMouseOver(mouseX, mouseY))
				screen.renderTooltip(poseStack, EasyText.translatable("gui.button.lightmanscurrency.free_sample.reset.tooltip"), mouseX, mouseY);
			
		}

		@Override
		public void onTabClose() {
			this.removeCustomWidget(this.buttonClearMemory);
		}
		
		@Override
		public void onScreenTick() { }
		
		void PressClearMemoryButton(Button button)
		{
			this.getRule().memory.clear();
			CompoundNBT updateInfo = new CompoundNBT();
			updateInfo.putBoolean("ClearData", true);
			this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
		}
		
	}
	
}