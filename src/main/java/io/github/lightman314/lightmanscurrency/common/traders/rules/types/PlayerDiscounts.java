package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.PlayerDiscountTab;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.PriceTweakingTradeRule;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
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
public class PlayerDiscounts extends PriceTweakingTradeRule implements ICopySupportingRule {

	public static final TradeRuleType<PlayerDiscounts> TYPE = new TradeRuleType<>(VersionUtil.lcResource("discount_list"),PlayerDiscounts::new);
	
	private List<PlayerReference> playerList = new ArrayList<>();
	public ImmutableList<PlayerReference> getPlayerList() { return ImmutableList.copyOf(this.playerList); }
	int discount = 10;
	public int getDiscount() { return this.discount; }
	public void setDiscount(int discount) { this.discount = MathUtil.clamp(discount, 1, 100); }
	
	private PlayerDiscounts() { super(TYPE); }

	
	@Override
	public IconData getIcon() { return IconUtil.ICON_DISCOUNT_LIST; }

	@Override
	public void beforeTrade(PreTradeEvent event)
	{
		if(this.isOnList(event.getPlayerReference()))
		{
			switch (event.getTrade().getTradeDirection()) {
				case SALE ->
						event.addHelpful(LCText.TRADE_RULE_PLAYER_DISCOUNTS_INFO_SALE.get(this.discount));
				case PURCHASE ->
						event.addHelpful(LCText.TRADE_RULE_PLAYER_DISCOUNTS_INFO_PURCHASE.get(this.discount));
				default -> {
				} //Nothing by default
			}
		}
	}
	
	@Override
	public void tradeCost(TradeCostEvent event)
	{
		if(this.isOnList(event.getPlayerReference()))
		{
			switch (event.getTrade().getTradeDirection()) {
				case SALE -> event.giveDiscount(this.discount);
				case PURCHASE -> event.hikePrice(this.discount);
				default -> {} //Nothing by default
			}
		}
	}

	public boolean isOnList(PlayerReference player)  { return PlayerReference.isInList(this.playerList, player); }
	
	@Override
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		//Save player names
		PlayerReference.saveList(compound, this.playerList, "Players");
		//Save discount
		compound.putInt("discount", this.discount);
	}

	@Override
	public JsonObject saveToJson(JsonObject json, HolderLookup.Provider lookup) {
		json.add("Players", PlayerReference.saveJsonList(this.playerList));
		json.addProperty("discount", this.discount);
		return json;
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		//Load player names
		this.playerList = PlayerReference.loadList(compound, "Players");
		//Load discount
		if(compound.contains("discount", Tag.TAG_INT))
			this.discount = compound.getInt("discount");
	}
	
	@Override
	public void loadFromJson(JsonObject json, HolderLookup.Provider lookup) {
		if(json.has("Players"))
		{
			this.playerList.clear();
			JsonArray playerList = json.get("Players").getAsJsonArray();
			for(int i = 0; i < playerList.size(); ++i) {
				PlayerReference reference = PlayerReference.load(playerList.get(i));
				if(reference != null && !this.isOnList(reference))
					this.playerList.add(reference);
			}
		}
		if(json.has("discount"))
			this.discount = json.get("discount").getAsInt();
	}

	@Override
	public void writeSettings(SavedSettingData.MutableNodeAccess node) {
		node.setIntValue("discount",this.discount);
		for(int i = 0; i < this.playerList.size(); ++i)
			node.setCompoundValue("player_" + i,this.playerList.get(i).save());
	}

	@Override
	public void loadSettings(SavedSettingData.NodeAccess node) {
		this.discount = node.getIntValue("discount");
		List<PlayerReference> temp = new ArrayList<>();
		for(int i = 0; node.hasCompoundValue("player_" + i); ++i)
			temp.add(PlayerReference.load(node.getCompoundValue("player_" + i)));
		this.playerList = temp;
	}

	@Override
	public void resetToDefaultState() {
		this.discount = 10;
		this.playerList = new ArrayList<>();
	}

	@Override
	protected void handleUpdateMessage(Player player, LazyPacketData updateInfo)
	{
		if(updateInfo.contains("Discount"))
			this.discount = updateInfo.getInt("Discount");
		else if(updateInfo.contains("AddPlayer"))
		{
			PlayerReference added = PlayerReference.load(updateInfo.getNBT("AddPlayer"));
			if(added == null || this.isOnList(added))
				return;
			this.playerList.add(added);
		}
		else if(updateInfo.contains("RemovePlayer"))
		{
			PlayerReference removed = PlayerReference.load(updateInfo.getNBT("RemovePlayer"));
			if(removed == null || !this.isOnList(removed))
				return;
			PlayerReference.removeFromList(this.playerList,removed);
		}
	}
	
	@Override
	public CompoundTag savePersistentData(HolderLookup.Provider lookup) { return null; }
	@Override
	public void loadPersistentData(CompoundTag data, HolderLookup.Provider lookup) { }

	
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new PlayerDiscountTab(parent); }

}
