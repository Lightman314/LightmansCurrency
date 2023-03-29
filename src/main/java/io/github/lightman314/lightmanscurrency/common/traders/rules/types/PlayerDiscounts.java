package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.PlayerDiscountTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.PriceTweakingTradeRule;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class PlayerDiscounts extends PriceTweakingTradeRule {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "discount_list");
	
	List<PlayerReference> playerList = new ArrayList<>();
	public ImmutableList<PlayerReference> getPlayerList() { return ImmutableList.copyOf(this.playerList); }
	int discount = 10;
	public int getDiscount() { return this.discount; }
	public void setDiscount(int discount) { this.discount = MathUtil.clamp(discount, 0, 100); }
	private double getDiscountMult() { return 1d - ((double)discount/100d); }
	private double getIncreaseMult() { return 1d + ((double)discount/100d); }
	
	public PlayerDiscounts() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event)
	{
		if(this.isOnList(event.getPlayerReference()))
		{
			switch (event.getTrade().getTradeDirection()) {
				case SALE ->
						event.addHelpful(Component.translatable("traderule.lightmanscurrency.discount_list.info.sale", this.discount));
				case PURCHASE ->
						event.addHelpful(Component.translatable("traderule.lightmanscurrency.discount_list.info.purchase", this.discount));
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
				case SALE -> event.applyCostMultiplier(this.getDiscountMult());
				case PURCHASE -> event.applyCostMultiplier(this.getIncreaseMult());
				default -> {} //Nothing by default
			}
		}
	}
	
	public boolean isOnList(PlayerReference player)
	{
		for (PlayerReference playerReference : this.playerList) {
			if (playerReference.is(player))
				return true;
		}
		return false;
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		//Save player names
		ListTag playerNameList = new ListTag();
		for (PlayerReference playerReference : playerList)
			playerNameList.add(playerReference.save());
		compound.put("Players", playerNameList);
		//Save discount
		compound.putInt("discount", this.discount);
	}

	@Override
	public JsonObject saveToJson(JsonObject json) {
		JsonArray playerList = new JsonArray();
		for (PlayerReference playerReference : this.playerList)
			playerList.add(playerReference.saveAsJson());
		json.add("Players", playerList);
		json.addProperty("discounrd", this.discount);
		return json;
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound) {
		//Load player names
		if(compound.contains("Players", Tag.TAG_LIST))
		{
			this.playerList.clear();
			ListTag playerNameList = compound.getList("Players", Tag.TAG_COMPOUND);
			for(int i = 0; i < playerNameList.size(); i++)
			{
				CompoundTag thisCompound = playerNameList.getCompound(i);
				PlayerReference reference = PlayerReference.load(thisCompound);
				if(reference != null)
					this.playerList.add(reference);
				//Load old method
				else if(thisCompound.contains("name", Tag.TAG_STRING))
				{
					reference = PlayerReference.of(false, thisCompound.getString("name"));
					if(reference != null && !this.isOnList(reference))
						this.playerList.add(reference);
				}
			}
		}
		//Load discount
		if(compound.contains("discount", Tag.TAG_INT))
			this.discount = compound.getInt("discount");
		
	}
	
	@Override
	public void loadFromJson(JsonObject json) {
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
	protected void handleUpdateMessage(CompoundTag updateInfo)
	{
		if(updateInfo.contains("Discount"))
		{
			this.discount = updateInfo.getInt("Discount");
		}
		else
		{
			boolean add = updateInfo.getBoolean("Add");
			String name = updateInfo.getString("Name");
			PlayerReference player = PlayerReference.of(false, name);
			if(add && !this.isOnList(player))
			{
				this.playerList.add(player);
			}
			else if(!add && this.isOnList(player))
			{
				PlayerReference.removeFromList(this.playerList, player);
			}
		}
	}
	
	@Override
	public CompoundTag savePersistentData() { return null; }
	@Override
	public void loadPersistentData(CompoundTag data) { }
	
	public IconData getButtonIcon() { return IconAndButtonUtil.ICON_DISCOUNT_LIST; }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new PlayerDiscountTab(parent); }
	
}
