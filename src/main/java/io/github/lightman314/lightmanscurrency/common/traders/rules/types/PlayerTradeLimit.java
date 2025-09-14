package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.data.PlayerMemory;
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
	
	private final PlayerMemory memory = new PlayerMemory();
	public void resetMemory() { this.memory.clear(); }
	
	private PlayerTradeLimit() { super(TYPE); }

	
	@Override
	public IconData getIcon() { return IconUtil.ICON_COUNT_PLAYER; }

	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		int tradeCount = this.memory.getCount(event,this.timeLimit);
		if(tradeCount >= this.limit)
		{
			if(this.enforceTimeLimit())
            {
                event.addDenial(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL_TIMED.get(tradeCount, new TimeUtil.TimeData(this.getTimeLimit()).getString()));
                long timeRemaining = this.memory.getTimeRemaining(event,this.timeLimit);
                if(timeRemaining > 0)
                    event.addDenial(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL_TIME_REMAINING.get(new TimeUtil.TimeData(timeRemaining).getString()));
            }
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
		
		this.memory.addEntry(event);
        this.memory.clearExpiredData(this.timeLimit);
		
		event.markDirty();
		
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		
		compound.putInt("Limit", this.limit);
		this.memory.save(compound);
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
		this.memory.load(compound);
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
		this.memory.save(data);
		return data;
	}
	
	@Override
	public void loadPersistentData(CompoundTag data) {
		this.memory.load(data);
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
