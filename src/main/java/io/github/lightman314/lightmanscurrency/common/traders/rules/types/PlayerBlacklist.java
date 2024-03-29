package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.PlayerBlacklistTab;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class PlayerBlacklist extends TradeRule {
	
	public static final TradeRuleType<PlayerBlacklist> TYPE = new TradeRuleType<>(new ResourceLocation(LightmansCurrency.MODID, "blacklist"),PlayerBlacklist::new);
	
	List<PlayerReference> bannedPlayers = new ArrayList<>();
	public ImmutableList<PlayerReference> getBannedPlayers() { return ImmutableList.copyOf(this.bannedPlayers); }
	
	private PlayerBlacklist() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(this.isBlacklisted(event.getPlayerReference()))
			event.addDenial(EasyText.translatable("traderule.lightmanscurrency.blacklist.denial"));
	}

	public boolean isBlacklisted(PlayerReference player)  { return PlayerReference.isInList(this.bannedPlayers, player); }
	
	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		//Save player
		PlayerReference.saveList(compound, this.bannedPlayers, "BannedPlayers");
	}
	
	@Override
	public JsonObject saveToJson(@Nonnull JsonObject json) {
		json.add("BannedPlayers", PlayerReference.saveJsonList(this.bannedPlayers));
		return json;
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound) {
		
		//Load blacklisted players
		if(compound.contains("BannedPlayers", Tag.TAG_LIST))
			this.bannedPlayers = PlayerReference.loadList(compound, "BannedPlayers");
		
	}
	
	@Override
	public void handleUpdateMessage(@Nonnull LazyPacketData updateInfo) {
		if(updateInfo.contains("Add"))
		{
			boolean add = updateInfo.getBoolean("Add");
			String name = updateInfo.getString("Name");
			PlayerReference player = PlayerReference.of(false, name);
			if(player == null)
				return;
			if(add && !this.isBlacklisted(player))
			{
				this.bannedPlayers.add(player);
			}
			else if(!add && this.isBlacklisted(player))
			{
				PlayerReference.removeFromList(this.bannedPlayers, player);
			}
		}
	}
	
	@Override
	public void loadFromJson(@Nonnull JsonObject json) {
		if(json.has("BannedPlayers"))
		{
			this.bannedPlayers.clear();
			JsonArray blacklist = json.get("BannedPlayers").getAsJsonArray();
			for(int i = 0; i < blacklist.size(); ++i) {
				PlayerReference reference = PlayerReference.load(blacklist.get(i).getAsJsonObject());
				if(reference != null && !this.isBlacklisted(reference))
					this.bannedPlayers.add(reference);
			}
		}
	}
	
	@Override
	public CompoundTag savePersistentData() { return null; }
	@Override
	public void loadPersistentData(CompoundTag data) { }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new PlayerBlacklistTab(parent); }

	
}