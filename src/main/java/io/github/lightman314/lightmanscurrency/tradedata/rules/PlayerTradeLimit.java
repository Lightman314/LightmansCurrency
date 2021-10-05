package io.github.lightman314.lightmanscurrency.tradedata.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeWidget;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class PlayerTradeLimit extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "tradelimit");
	
	private int limit = 1;
	public int getLimit() { return this.limit; }
	public void setLimit(int newLimit) { this.limit = newLimit; }
	
	private long timeLimit = 0;
	private boolean enforceTimeLimit() { return this.timeLimit > 0; }
	public long getTimeLimit() { return this.timeLimit; }
	public void setTimeLimit(int newValue) { this.timeLimit = newValue; }
	
	Map<UUID,List<Long>> memory = new HashMap<>();
	public void resetMemory() { this.memory.clear(); }
	
	public PlayerTradeLimit() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(getTradeCount(event.getPlayer().getUniqueID()) >= this.limit)
			event.setCanceled(true);
	}

	@Override
	public void afterTrade(PostTradeEvent event) {
		
		this.addEvent(event.getPlayer().getUniqueID(), TimeUtil.getCurrentTime());
		
		this.clearExpiredData();
		
		event.markDirty();
		
	}
	
	private void addEvent(UUID player, Long time)
	{
		List<Long> eventTimes = new ArrayList<>();
		if(this.memory.containsKey(player))
			eventTimes = this.memory.get(player);
		eventTimes.add(time);
		this.memory.put(player, eventTimes);
	}
	
	private void clearExpiredData()
	{
		if(!this.enforceTimeLimit())
			return;
		List<UUID> emptyEntries = new ArrayList<>();
		this.memory.forEach((id, eventTimes) ->{
			for(int i = 0; i < eventTimes.size(); i++)
			{
				if(!TimeUtil.compareTime(this.timeLimit, eventTimes.get(i)))
				{
					eventTimes.remove(i);
					i--;
				}
			}
			if(eventTimes.size() <= 0)
				emptyEntries.add(id);
		});
		emptyEntries.forEach(id -> this.memory.remove(id));
	}
	
	private int getTradeCount(UUID playerID)
	{
		int count = 0;
		if(this.memory.containsKey(playerID))
		{
			List<Long> eventTimes = this.memory.get(playerID);
			if(!this.enforceTimeLimit())
				return eventTimes.size();
			for(int i = 0; i < eventTimes.size(); i++)
			{
				if(TimeUtil.compareTime(this.timeLimit, eventTimes.get(i)))
					count++;
			}
		}
		return count;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		
		compound.putInt("Limit", this.limit);
		ListNBT memoryList = new ListNBT();
		this.memory.forEach((id, eventTimes) ->{
			CompoundNBT thisMemory = new CompoundNBT();
			thisMemory.putUniqueId("id", id);
			thisMemory.putLongArray("times", eventTimes);
			memoryList.add(thisMemory);
		});
		compound.put("Memory", memoryList);
		compound.putLong("ForgetTime", this.timeLimit);
		return compound;
	}

	@Override
	public void readNBT(CompoundNBT compound) {
		
		if(compound.contains("Limit", Constants.NBT.TAG_INT))
			this.limit = compound.getInt("Limit");
		if(compound.contains("Memory", Constants.NBT.TAG_LIST))
		{
			this.memory.clear();
			ListNBT memoryList = compound.getList("Memory", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < memoryList.size(); i++)
			{
				CompoundNBT thisMemory = memoryList.getCompound(i);
				UUID id = null;
				List<Long> eventTimes = new ArrayList<>();
				if(thisMemory.contains("id"))
					id = thisMemory.getUniqueId("id");
				if(thisMemory.contains("count", Constants.NBT.TAG_INT))
				{
					int count = thisMemory.getInt("count");
					for(int z = 0; z < count; z++)
					{
						eventTimes.add(0l);
					}
				}
				if(thisMemory.contains("times", Constants.NBT.TAG_LONG_ARRAY))
				{
					for(long time : thisMemory.getLongArray("times"))
					{
						eventTimes.add(time);
					}
				}
				this.memory.put(id, eventTimes);
			}
		}
		if(compound.contains("ForgetTime", Constants.NBT.TAG_LONG))
			this.timeLimit = compound.getLong("ForgetTime");
	}
	
	@Override
	public ITextComponent getButtonText() { return new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit"); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
	{
		return new GUIHandler(screen, rule);
	}
	
	@OnlyIn(Dist.CLIENT)
	private static class GUIHandler extends TradeRule.GUIHandler implements TimeWidget.ITimeInput
	{
		
		private final PlayerTradeLimit getRule()
		{
			if(getRuleRaw() instanceof PlayerTradeLimit)
				return (PlayerTradeLimit)getRuleRaw();
			return null;
		}
		
		GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
		{
			super(screen, rule);
		}
		
		TextFieldWidget limitInput;
		Button buttonSetLimit;
		Button buttonClearMemory;
		TimeWidget timeInput;
		
		@Override
		public void initTab() {
			
			this.limitInput = new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 19, 30, 20, new StringTextComponent(""));
			this.limitInput.setMaxStringLength(3);
			this.limitInput.setText(Integer.toString(this.getRule().limit));
			this.addListener(this.limitInput);
			
			this.buttonSetLimit = this.addButton(new Button(screen.guiLeft() + 41, screen.guiTop() + 19, 40, 20, new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit.setlimit"), this::PressSetLimitButton));
			this.buttonClearMemory = this.addButton(new Button(screen.guiLeft() + 10, screen.guiTop() + 50, screen.xSize - 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit.clearmemory"), this::PressClearMemoryButton));
			
			this.timeInput = this.addListener(new TimeWidget(screen.guiLeft(), screen.guiTop() + 80, this.screen.getFont(), this.getRule().timeLimit, this, this, new TranslationTextComponent("gui.widget.lightmanscurrency.playerlimit.noduration")));
			
		}

		@Override
		public void renderTab(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			
			this.limitInput.render(matrixStack, mouseX, mouseY, partialTicks);
			this.timeInput.render(matrixStack, mouseX, mouseY, partialTicks);
			
			screen.getFont().drawString(matrixStack, new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit.info", this.getRule().limit).getString(), screen.guiLeft() + 10, screen.guiTop() + 9, 0xFFFFFF);
			
			if(this.buttonClearMemory.isMouseOver(mouseX, mouseY))
				screen.renderTooltip(matrixStack, new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit.clearmemory.tooltip"), mouseX, mouseY);
			
		}

		@Override
		public void onTabClose() {
			
			screen.removeListener(this.limitInput);
			screen.removeButton(this.buttonSetLimit);
			screen.removeButton(this.buttonClearMemory);
			
			this.timeInput.getButtons().forEach(button -> screen.removeButton(button));
			this.timeInput.getListeners().forEach(listener -> screen.removeListener(listener));
			screen.removeListener(this.timeInput);
			
		}
		
		@Override
		public void onScreenTick() {
			
			this.limitInput.tick();
			this.timeInput.tick();
			TextInputUtil.whitelistInteger(this.limitInput, 1, 100);
			
		}
		
		void PressSetLimitButton(Button button)
		{
			this.getRule().limit = MathUtil.clamp(TextInputUtil.getIntegerValue(this.limitInput), 1, 100);
			this.screen.markRulesDirty();
		}
		
		void PressClearMemoryButton(Button button)
		{
			this.getRule().memory.clear();
			this.screen.markRulesDirty();
		}

		@Override
		public void onTimeSet(long newTime) {
			this.getRule().timeLimit = MathUtil.clamp(newTime, 0, Long.MAX_VALUE);
			this.screen.markRulesDirty();
		}
		
	}
	
}
