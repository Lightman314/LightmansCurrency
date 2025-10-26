package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.TradeLimitTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TradeLimit extends TradeRule implements ICopySupportingRule {

	public static final TradeRuleType<TradeLimit> TYPE = new TradeRuleType<>(VersionUtil.lcResource("trade_limit"),TradeLimit::new);
	
	private int limit = 1;
	public int getLimit() { return this.limit; }
	public void setLimit(int newLimit) { this.limit = MathUtil.clamp(newLimit,1,100); }
	
	int count = 0;
	public void resetCount() { this.count = 0; }
	
	private TradeLimit() { super(TYPE); }
	
	@Override
	public IconData getIcon() { return IconUtil.ICON_COUNT; }

	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(this.count >= this.limit)
		{
			event.addDenial(LCText.TRADE_RULE_TRADE_LIMIT_DENIAL.get(this.count));
			event.addDenial(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL_LIMIT.get(this.limit));
		}
		else
			event.addHelpful(LCText.TRADE_RULE_TRADE_LIMIT_INFO.get(this.count, this.limit));
	}

	@Override
	public void afterTrade(PostTradeEvent event) {
		
		this.count++;
		
		event.markDirty();
		
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		
		compound.putInt("Limit", this.limit);
		compound.putInt("Count", this.count);
		
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json, HolderLookup.Provider lookup) {
		json.addProperty("Limit", this.limit);
		return json;
	}

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		
		if(compound.contains("Limit", Tag.TAG_INT))
			this.limit = compound.getInt("Limit");
		if(compound.contains("Count", Tag.TAG_INT))
			this.count = compound.getInt("Count");
		
	}

	@Override
	public void writeSettings(SavedSettingData.MutableNodeAccess node) {
		node.setIntValue("limit",this.limit);
	}

	@Override
	public void loadSettings(SavedSettingData.NodeAccess node) {
		this.limit = node.getIntValue("limit");
	}

	@Override
	public void resetToDefaultState() {
		this.limit = 1;
		this.count = 0;
	}

	@Override
	public void loadFromJson(JsonObject json, HolderLookup.Provider lookup) {
		if(json.has("Limit"))
			this.limit = json.get("Limit").getAsInt();
	}
	
	@Override
	public void handleUpdateMessage(Player player, LazyPacketData updateInfo)
	{
		if(updateInfo.contains("Limit"))
			this.limit = updateInfo.getInt("Limit");
		else if(updateInfo.contains("ClearMemory"))
			this.count = 0;
	}
	
	@Override
	public CompoundTag savePersistentData(HolderLookup.Provider lookup) {
		CompoundTag data = new CompoundTag();
		data.putInt("Count", this.count);
		return data;
	}
	@Override
	public void loadPersistentData(CompoundTag data, HolderLookup.Provider lookup) {
		if(data.contains("Count", Tag.TAG_INT))
			this.count = data.getInt("Count");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new TradeLimitTab(parent); }
	
}
