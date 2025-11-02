package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.*;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.FreeSampleTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.common.traders.rules.PriceTweakingTradeRule;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FreeSample extends PriceTweakingTradeRule implements ICopySupportingRule {

	public static final TradeRuleType<FreeSample> TYPE = new TradeRuleType<>(VersionUtil.lcResource("free_sample"),FreeSample::new);

	private int limit = 1;
	public int getLimit() { return this.limit; }
	public void setLimit(int newLimit) { this.limit = MathUtil.clamp(newLimit,1,100); }

	private long timeLimit = 0;
	private boolean enforceTimeLimit() { return this.timeLimit > 0; }
	public long getTimeLimit() { return this.timeLimit; }
	public void setTimeLimit(long timeLimit) { this.timeLimit = timeLimit; }

	Map<UUID,List<Long>> memory = new HashMap<>();
	public void resetMemory() { this.memory.clear(); }

	private int totalCount = 0;
	public int getSampleCount() { return this.totalCount; }

	private FreeSample() { super(TYPE); }

	@Override
	protected boolean canActivate(@Nullable ITradeRuleHost host) {
		if(host instanceof TradeData trade && trade.getTradeDirection() != TradeDirection.SALE)
			return false;
		return super.canActivate(host);
	}

	
	@Override
	public IconData getIcon() { return IconUtil.ICON_FREE_SAMPLE; }

	@Override
	public void beforeTrade(PreTradeEvent event)
	{
		if(this.giveDiscount(event))
		{
			if(this.limit > 1)
			{
				event.addHelpful(LCText.TRADE_RULE_FREE_SAMPLE_INFO_MULTI.get(this.limit));
				int count = this.getFreeSampleCount(event);
				if(count > 0)
					event.addHelpful(LCText.TRADE_RULE_FREE_SAMPLE_INFO_USED.get(count,this.limit));
			}
			else
				event.addHelpful(LCText.TRADE_RULE_FREE_SAMPLE_INFO_SINGLE.get());
		}
		else
		{
			int count = this.getFreeSampleCount(event);
			if(count > 0)
				event.addNeutral(LCText.TRADE_RULE_FREE_SAMPLE_INFO_USED.get(count,this.limit));
		}
		if(this.enforceTimeLimit())
			event.addNeutral(LCText.TRADE_RULE_FREE_SAMPLE_INFO_TIMED.get(new TimeUtil.TimeData(this.getTimeLimit()).getString()));
	}

	@Override
	public void tradeCost(TradeCostEvent event) {
		if(this.giveDiscount(event))
			event.makeFree();
	}

	@Override
	public void afterTrade(PostTradeEvent event) {
		if(this.giveDiscount(event))
		{
			this.addToMemory(event.getPlayerReference().id, TimeUtil.getCurrentTime());
			this.clearExpiredData();
			event.markDirty();
		}
	}

	private boolean giveDiscount(TradeEvent event) {
		return event.hasPlayerReference() && this.giveDiscount(event.getPlayerReference().id) && event.getTrade().getTradeDirection() == TradeDirection.SALE;
	}

	private void addToMemory(UUID playerID, Long time) {
		List<Long> entry = this.memory.getOrDefault(playerID,new ArrayList<>());
		entry.add(time);
		this.memory.put(playerID,entry);
		this.totalCount++;
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
			if(eventTimes.isEmpty())
				emptyEntries.add(id);
		});
		emptyEntries.forEach(id -> this.memory.remove(id));
	}

	public boolean giveDiscount(UUID playerID) { return this.getFreeSampleCount(playerID) < this.limit; }

	private int getFreeSampleCount(TradeEvent event) { return getFreeSampleCount(event.getPlayerReference().id); }
	private int getFreeSampleCount(UUID playerID)
	{
		int count = 0;
		if(this.memory.containsKey(playerID))
		{
			List<Long> eventTimes = this.memory.get(playerID);
			if(!this.enforceTimeLimit())
				return eventTimes.size();
			for (Long eventTime : eventTimes) {
				if (TimeUtil.compareTime(this.timeLimit, eventTime))
					count++;
			}
		}
		return count;
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {

		compound.putInt("Limit", this.limit);
		compound.putLong("ForgetTime", this.timeLimit);
		this.saveMemory(compound);

	}

	private void saveMemory(CompoundTag compound)
	{
		compound.putInt("Total", this.totalCount);
		final ListTag memoryList = new ListTag();
		this.memory.forEach((id,entries) -> {
			CompoundTag tag = new CompoundTag();
			tag.putUUID("ID", id);
			tag.putLongArray("Times", entries);
			memoryList.add(tag);
		});
		compound.put("Memory", memoryList);
	}

	@Override
	public JsonObject saveToJson(JsonObject json) {
		json.addProperty("Limit", this.limit);
		if(this.enforceTimeLimit())
			json.addProperty("ForgetTime", this.timeLimit);
		return json;
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {

		if(compound.contains("Limit", Tag.TAG_INT))
			this.limit = compound.getInt("Limit");
		if(compound.contains("ForgetTime"))
			this.timeLimit = compound.getLong("ForgetTime");
		this.loadMemory(compound);
	}

	private void loadMemory(CompoundTag compound)
	{
		if(compound.contains("Total"))
			this.totalCount = compound.getInt("Total");
		if(compound.contains("Memory", Tag.TAG_LIST))
		{
			this.memory.clear();
			ListTag memoryList = compound.getList("Memory", Tag.TAG_COMPOUND);
			for(int i = 0; i < memoryList.size(); i++)
			{
				CompoundTag tag = memoryList.getCompound(i);
				if(tag.contains("ID"))
				{
					List<Long> eventTimes = new ArrayList<>();
					if(tag.contains("Times"))
					{
						for(long time : tag.getLongArray("Times"))
							eventTimes.add(time);
					}
					else
					{
						eventTimes.add(TimeUtil.getCurrentTime());
						this.totalCount++;
					}
					this.memory.put(tag.getUUID("ID"),eventTimes);
				}
				else if(tag.contains("id"))
				{
					List<Long> eventTimes = new ArrayList<>();
					eventTimes.add(TimeUtil.getCurrentTime());
					this.memory.put(tag.getUUID("id"),eventTimes);
				}
			}
		}
	}

	@Override
	public void writeSettings(SavedSettingData.MutableNodeAccess node) {
		node.setIntValue("limit",this.limit);
		node.setLongValue("time_limit",this.timeLimit);
	}

	@Override
	public void loadSettings(SavedSettingData.NodeAccess node) {
		this.limit = node.getIntValue("limit");
		this.timeLimit = node.getLongValue("time_limit");
	}

	@Override
	public void resetToDefaultState() {
		this.limit = 1;
		this.timeLimit = 0;
		this.resetMemory();
	}

	@Override
	public CompoundTag savePersistentData() {
		CompoundTag data = new CompoundTag();
		this.saveMemory(data);
		return data;
	}

	@Override
	public void loadPersistentData(CompoundTag data) {
		this.loadMemory(data);
	}

	@Override
	public void loadFromJson(JsonObject json) {
		if(json.has("Limit"))
			this.limit = json.get("Limit").getAsInt();
		if(json.has("ForgetTime"))
			this.timeLimit = json.get("ForgetTime").getAsLong();
	}

	@Override
	protected void handleUpdateMessage(Player player, LazyPacketData updateInfo) {
		if(updateInfo.contains("Limit"))
			this.limit = updateInfo.getInt("Limit");
		else if(updateInfo.contains("TimeLimit"))
			this.timeLimit = updateInfo.getLong("TimeLimit");
		else if(updateInfo.contains("ClearMemory"))
			this.memory.clear();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new FreeSampleTab(parent); }

}