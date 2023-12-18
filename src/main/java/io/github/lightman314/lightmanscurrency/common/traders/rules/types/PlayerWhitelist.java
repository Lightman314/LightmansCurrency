package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.PlayerWhitelistTab;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class PlayerWhitelist extends TradeRule {
	
	public static final TradeRuleType<PlayerWhitelist> TYPE = new TradeRuleType<>(new ResourceLocation(LightmansCurrency.MODID, "whitelist"),PlayerWhitelist::new);
	
	List<PlayerReference> whitelistedPlayers = new ArrayList<>();
	public ImmutableList<PlayerReference> getWhitelistedPlayers() { return ImmutableList.copyOf(this.whitelistedPlayers); }
	
	private PlayerWhitelist() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(!this.isWhitelisted(event.getPlayerReference()))
			event.addDenial(EasyText.translatable("traderule.lightmanscurrency.whitelist.denial"));
		else
			event.addHelpful(EasyText.translatable("traderule.lightmanscurrency.whitelist.allowed"));
		
	}
	
	public boolean isWhitelisted(PlayerReference player) { return PlayerReference.isInList(this.whitelistedPlayers, player); }
	
	public boolean addToWhitelist(Player player)
	{
		PlayerReference pr = PlayerReference.of(player);
		if(!this.isWhitelisted(pr))
		{
			this.whitelistedPlayers.add(pr);
			return true;
		}
		return false;
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		//Save player names
		PlayerReference.saveList(compound, this.whitelistedPlayers, "WhitelistedPlayers");
	}
	
	@Override
	public JsonObject saveToJson(@Nonnull JsonObject json) { return json; }

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound) {
		
		//Load whitelisted players
		if(compound.contains("WhitelistedPlayers", Tag.TAG_LIST))
			this.whitelistedPlayers = PlayerReference.loadList(compound, "WhitelistedPlayers");
		
	}
	
	@Override
	public void loadFromJson(@Nonnull JsonObject json) {}
	
	@Override
	public void handleUpdateMessage(@Nonnull LazyPacketData updateInfo)
	{
		if(updateInfo.contains("Add"))
		{
			boolean add = updateInfo.getBoolean("Add");
			String name = updateInfo.getString("Name");
			PlayerReference player = PlayerReference.of(false, name);
			if(player == null)
				return;
			if(add && !this.isWhitelisted(player))
			{
				this.whitelistedPlayers.add(player);
			}
			else if(!add && this.isWhitelisted(player))
			{
				PlayerReference.removeFromList(this.whitelistedPlayers, player);
			}
		}
	}
	
	@Override
	public CompoundTag savePersistentData() {
		CompoundTag compound = new CompoundTag();
		this.saveAdditional(compound);
		return compound;
	}
	@Override
	public void loadPersistentData(CompoundTag data) { this.loadAdditional(data); }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new PlayerWhitelistTab(parent); }
	
}
