package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.PlayerBlacklistTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class PlayerBlacklist extends TradeRule {
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "blacklist");
	
	List<PlayerReference> bannedPlayers = new ArrayList<>();
	public ImmutableList<PlayerReference> getBannedPlayers() { return ImmutableList.copyOf(this.bannedPlayers); }
	
	public PlayerBlacklist() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(this.isBlacklisted(event.getPlayerReference()))
			event.addDenial(EasyText.translatable("traderule.lightmanscurrency.blacklist.denial"));
	}

	public boolean isBlacklisted(PlayerReference player)  { return PlayerReference.isInList(this.bannedPlayers, player); }
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		//Save player
		PlayerReference.saveList(compound, this.bannedPlayers, "BannedPlayers");
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) {
		json.add("BannedPlayers", PlayerReference.saveJsonList(this.bannedPlayers));
		return json;
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		
		//Load blacklisted players
		if(compound.contains("BannedPlayers", Tag.TAG_LIST))
			this.bannedPlayers = PlayerReference.loadList(compound, "BannedPlayers");
		
	}
	
	@Override
	public void handleUpdateMessage(CompoundTag updateInfo) {
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
	
	@Override
	public void loadFromJson(JsonObject json) {
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
	
	public IconData getButtonIcon() { return IconAndButtonUtil.ICON_BLACKLIST; }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new PlayerBlacklistTab(parent); }

	
}