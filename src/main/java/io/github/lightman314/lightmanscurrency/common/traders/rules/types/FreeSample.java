package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.FreeSampleTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.common.traders.rules.PriceTweakingTradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.TradeCostEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class FreeSample extends PriceTweakingTradeRule {
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "free_sample");
	
	List<UUID> memory = new ArrayList<>();
	
	public FreeSample() { super(TYPE); }

	@Override
	protected boolean canActivate(@Nullable ITradeRuleHost host) {
		if(host instanceof TradeData trade && trade.getTradeDirection() != TradeDirection.SALE)
			return false;
		return super.canActivate(host);
	}

	@Override
	public void beforeTrade(PreTradeEvent event)
	{
		if(this.giveDiscount(event))
			event.addHelpful(Component.translatable("traderule.lightmanscurrency.free_sample.alert"));
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
		return this.giveDiscount(event.getPlayerReference().id) && event.getTrade().getTradeDirection() == TradeDirection.PURCHASE;
	}
	
	private void addToMemory(UUID playerID) {
		if(!this.memory.contains(playerID))
			this.memory.add(playerID);
	}
	
	public boolean giveDiscount(UUID playerID) { return !this.givenFreeSample(playerID); }
	
	private boolean givenFreeSample(UUID playerID) { return this.memory.contains(playerID); }
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		
		ListTag memoryList = new ListTag();
		for(UUID entry : this.memory)
		{
			CompoundTag tag = new CompoundTag();
			tag.putUUID("ID", entry);
			memoryList.add(tag);
		}
		compound.put("Memory", memoryList);
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) { return json; }

	@Override
	protected void loadAdditional(CompoundTag compound) {
		
		if(compound.contains("Memory", Tag.TAG_LIST))
		{
			this.memory.clear();
			ListTag memoryList = compound.getList("Memory", Tag.TAG_COMPOUND);
			for(int i = 0; i < memoryList.size(); i++)
			{
				CompoundTag tag = memoryList.getCompound(i);
				if(tag.contains("ID"))
					this.memory.add(tag.getUUID("ID"));
				else if(tag.contains("id"))
					this.memory.add(tag.getUUID("id"));
			}
		}
	}
	
	@Override
	public CompoundTag savePersistentData() {
		CompoundTag data = new CompoundTag();
		ListTag memoryList = new ListTag();
		for(UUID entry : this.memory)
		{
			CompoundTag tag = new CompoundTag();
			tag.putUUID("ID", entry);
			memoryList.add(tag);
		}
		data.put("Memory", memoryList);
		return data;
	}
	
	@Override
	public void loadPersistentData(CompoundTag data) {
		if(data.contains("Memory", Tag.TAG_LIST))
		{
			this.memory.clear();
			ListTag memoryList = data.getList("Memory", Tag.TAG_COMPOUND);
			for(int i = 0; i < memoryList.size(); i++)
			{
				CompoundTag tag = memoryList.getCompound(i);
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
	protected void handleUpdateMessage(CompoundTag updateInfo) {
		if(updateInfo.contains("ClearData"))
			this.memory.clear();
	}
	
	@Override
	public IconData getButtonIcon() { return IconAndButtonUtil.ICON_FREE_SAMPLE; }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new FreeSampleTab(parent); }
	
}
