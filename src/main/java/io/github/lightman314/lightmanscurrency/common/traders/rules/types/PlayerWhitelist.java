package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.PlayerWhitelistTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class PlayerWhitelist extends TradeRule{

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "whitelist");

	List<PlayerReference> whitelistedPlayers = new ArrayList<>();
	public ImmutableList<PlayerReference> getWhitelistedPlayers() { return ImmutableList.copyOf(this.whitelistedPlayers); }

	public PlayerWhitelist() { super(TYPE); }

	@Override
	public void beforeTrade(PreTradeEvent event) {

		if(!this.isWhitelisted(event.getPlayerReference()))
			event.addDenial(EasyText.translatable("traderule.lightmanscurrency.whitelist.denial"));
		else
			event.addHelpful(EasyText.translatable("traderule.lightmanscurrency.whitelist.allowed"));

	}

	public boolean isWhitelisted(PlayerReference player)
	{
		for (PlayerReference whitelistedPlayer : this.whitelistedPlayers) {
			if (whitelistedPlayer.is(player))
				return true;
		}
		return false;
	}

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
	protected void saveAdditional(CompoundTag compound) {
		//Save player names
		ListTag playerNameList = new ListTag();
		for (PlayerReference whitelistedPlayer : this.whitelistedPlayers)
			playerNameList.add(whitelistedPlayer.save());
		compound.put("WhitelistedPlayers", playerNameList);
	}

	@Override
	public JsonObject saveToJson(JsonObject json) { return json; }

	@Override
	protected void loadAdditional(CompoundTag compound) {

		//Load whitelisted players
		if(compound.contains("WhitelistedPlayers", Tag.TAG_LIST))
		{
			this.whitelistedPlayers.clear();
			ListTag playerList = compound.getList("WhitelistedPlayers", Tag.TAG_COMPOUND);
			for(int i = 0; i < playerList.size(); ++i)
			{
				PlayerReference reference = PlayerReference.load(playerList.getCompound(i));
				if(reference != null)
					this.whitelistedPlayers.add(reference);
			}
		}

	}

	@Override
	public void loadFromJson(JsonObject json) {}

	@Override
	public void handleUpdateMessage(CompoundTag updateInfo)
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

	@Override
	public CompoundTag savePersistentData() {
		CompoundTag compound = new CompoundTag();
		this.saveAdditional(compound);
		return compound;
	}
	@Override
	public void loadPersistentData(CompoundTag data) { this.loadAdditional(data); }

	public IconData getButtonIcon() { return IconAndButtonUtil.ICON_WHITELIST; }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new PlayerWhitelistTab(parent); }

}