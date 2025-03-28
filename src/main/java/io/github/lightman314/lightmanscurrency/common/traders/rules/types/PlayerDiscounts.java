package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class PlayerDiscounts extends PriceTweakingTradeRule {

	public static final TradeRuleType<PlayerDiscounts> TYPE = new TradeRuleType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "discount_list"),PlayerDiscounts::new);
	
	List<PlayerReference> playerList = new ArrayList<>();
	public ImmutableList<PlayerReference> getPlayerList() { return ImmutableList.copyOf(this.playerList); }
	int discount = 10;
	public int getDiscount() { return this.discount; }
	public void setDiscount(int discount) { this.discount = MathUtil.clamp(discount, 0, 100); }
	
	private PlayerDiscounts() { super(TYPE); }

	@Nonnull
	@Override
	public IconData getIcon() { return IconUtil.ICON_DISCOUNT_LIST; }

	@Override
	public void beforeTrade(@Nonnull PreTradeEvent event)
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
	public void tradeCost(@Nonnull TradeCostEvent event)
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
	protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		//Save player names
		PlayerReference.saveList(compound, this.playerList, "Players");
		//Save discount
		compound.putInt("discount", this.discount);
	}

	@Override
	public JsonObject saveToJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) {
		json.add("Players", PlayerReference.saveJsonList(this.playerList));
		json.addProperty("discounrd", this.discount);
		return json;
	}
	
	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		//Load player names
		this.playerList = PlayerReference.loadList(compound, "Players");
		//Load discount
		if(compound.contains("discount", Tag.TAG_INT))
			this.discount = compound.getInt("discount");
		
	}
	
	@Override
	public void loadFromJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) {
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
	protected void handleUpdateMessage(@Nonnull LazyPacketData updateInfo)
	{
		if(updateInfo.contains("Discount"))
			this.discount = updateInfo.getInt("Discount");
		else if(updateInfo.contains("AddPlayer"))
		{
			PlayerReference player = PlayerReference.load(updateInfo.getNBT("AddPlayer"));
			if(player == null || this.isOnList(player))
				return;
			this.playerList.add(player);
		}
		else if(updateInfo.contains("RemovePlayer"))
		{
			PlayerReference player = PlayerReference.load(updateInfo.getNBT("RemovePlayer"));
			if(player == null || !this.isOnList(player))
				return;
			PlayerReference.removeFromList(this.playerList,player);
		}
	}
	
	@Override
	public CompoundTag savePersistentData(@Nonnull HolderLookup.Provider lookup) { return null; }
	@Override
	public void loadPersistentData(@Nonnull CompoundTag data, @Nonnull HolderLookup.Provider lookup) { }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new PlayerDiscountTab(parent); }
	
}
