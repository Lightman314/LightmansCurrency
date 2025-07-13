package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.PlayerTradeLimitTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
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

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PlayerTradeLimit extends TradeRule implements ICopySupportingRule {

	public static final TradeRuleType<PlayerTradeLimit> TYPE = new TradeRuleType<>(VersionUtil.lcResource("player_trade_limit"),PlayerTradeLimit::new);
	
	private int limit = 1;
	public int getLimit() { return this.limit; }
	public void setLimit(int newLimit) { this.limit = MathUtil.clamp(newLimit,1,100); }
	
	private long timeLimit = 0;
	private boolean enforceTimeLimit() { return this.timeLimit > 0; }
	public long getTimeLimit() { return this.timeLimit; }
	public void setTimeLimit(long timeLimit) { this.timeLimit = timeLimit; }
	
	Map<UUID,List<Long>> memory = new HashMap<>();
	public void resetMemory() { this.memory.clear(); }
	
	private PlayerTradeLimit() { super(TYPE); }

	
	@Override
	public IconData getIcon() { return IconUtil.ICON_COUNT_PLAYER; }

	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		int tradeCount = getTradeCount(event.getPlayerReference().id);
		if(tradeCount >= this.limit)
		{
			if(this.enforceTimeLimit())
				event.addDenial(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL_TIMED.get(tradeCount, new TimeUtil.TimeData(this.getTimeLimit()).getString()));
			else
				event.addDenial(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL.get(tradeCount));
			event.addDenial(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL_LIMIT.get(this.limit));
		}
		else
		{
			if(this.enforceTimeLimit())
				event.addHelpful(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_INFO_TIMED.get(tradeCount, this.limit, new TimeUtil.TimeData(this.getTimeLimit()).getString()));
			else
				event.addHelpful(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_INFO.get(tradeCount, this.limit));
		}
	}

	@Override
	public void afterTrade(PostTradeEvent event) {
		
		this.addEvent(event.getPlayerReference().id, TimeUtil.getCurrentTime());
		
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
			if(eventTimes.isEmpty())
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
		ListTag memoryList = new ListTag();
		this.memory.forEach((id, eventTimes) ->{
			CompoundTag thisMemory = new CompoundTag();
			thisMemory.putUUID("id", id);
			thisMemory.putLongArray("times", eventTimes);
			memoryList.add(thisMemory);
		});
		compound.put("Memory", memoryList);
		compound.putLong("ForgetTime", this.timeLimit);
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
		if(compound.contains("Memory", Tag.TAG_LIST))
		{
			this.memory.clear();
			ListTag memoryList = compound.getList("Memory", Tag.TAG_COMPOUND);
			for(int i = 0; i < memoryList.size(); i++)
			{
				CompoundTag thisMemory = memoryList.getCompound(i);
				UUID id = null;
				List<Long> eventTimes = new ArrayList<>();
				if(thisMemory.contains("id"))
					id = thisMemory.getUUID("id");
				if(thisMemory.contains("count", Tag.TAG_INT))
				{
					int count = thisMemory.getInt("count");
					for(int z = 0; z < count; z++)
					{
						eventTimes.add(TimeUtil.getCurrentTime());
					}
				}
				if(thisMemory.contains("times", Tag.TAG_LONG_ARRAY))
				{
					for(long time : thisMemory.getLongArray("times"))
					{
						eventTimes.add(time);
					}
				}
				this.memory.put(id, eventTimes);
			}
		}
		if(compound.contains("ForgetTime", Tag.TAG_LONG))
			this.timeLimit = compound.getLong("ForgetTime");
	}

	@Override
	public void writeSettings(SavedSettingData.MutableNodeAccess node) {
		node.setIntValue("limit",this.limit);
		node.setLongValue("time_limit",this.timeLimit);
	}

	@Override
	public void loadSettings(SavedSettingData.NodeAccess node) {
		this.limit = Math.max(1,node.getIntValue("limit"));
		this.timeLimit = node.getLongValue("time_limit");
	}

	@Override
	public void resetToDefaultState() {
		this.limit = 1;
		this.timeLimit = 0;
		this.resetMemory();
	}

	@Override
	public void handleUpdateMessage(Player player, LazyPacketData updateInfo)
	{
		if(updateInfo.contains("Limit"))
			this.limit = updateInfo.getInt("Limit");
		else if(updateInfo.contains("TimeLimit"))
			this.timeLimit = updateInfo.getLong("TimeLimit");
		else if(updateInfo.contains("ClearMemory"))
			this.resetMemory();
	}
	
	@Override
	public CompoundTag savePersistentData() {
		CompoundTag data = new CompoundTag();
		ListTag memoryList = new ListTag();
		this.memory.forEach((id, eventTimes) ->{
			CompoundTag thisMemory = new CompoundTag();
			thisMemory.putUUID("id", id);
			thisMemory.putLongArray("times", eventTimes);
			memoryList.add(thisMemory);
		});
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
				CompoundTag thisMemory = memoryList.getCompound(i);
				UUID id = null;
				List<Long> eventTimes = new ArrayList<>();
				if(thisMemory.contains("id"))
					id = thisMemory.getUUID("id");
				if(thisMemory.contains("count", Tag.TAG_INT))
				{
					int count = thisMemory.getInt("count");
					for(int z = 0; z < count; z++)
					{
						eventTimes.add(0L);
					}
				}
				if(thisMemory.contains("times", Tag.TAG_LONG_ARRAY))
				{
					for(long time : thisMemory.getLongArray("times"))
					{
						eventTimes.add(time);
					}
				}
				this.memory.put(id, eventTimes);
			}
		}
	}
	
	@Override
	public void loadFromJson(JsonObject json) {
		if(json.has("Limit"))
			this.limit = json.get("Limit").getAsInt();
		if(json.has("ForgetTime"))
			this.timeLimit = json.get("ForgetTime").getAsLong();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new PlayerTradeLimitTab(parent); }
	
}
