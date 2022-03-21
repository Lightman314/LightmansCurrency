package io.github.lightman314.lightmanscurrency.trader.tradedata.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerBlacklist extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "blacklist");
	
	List<PlayerReference> bannedPlayers = new ArrayList<>();
	
	public PlayerBlacklist() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(this.isBlacklisted(event.getPlayerReference()))
			event.denyTrade(new TranslatableComponent("traderule.lightmanscurrency.blacklist.denial"));
	}

	public boolean isBlacklisted(PlayerReference player)
	{
		for(int i = 0; i < this.bannedPlayers.size(); ++i)
		{
			if(this.bannedPlayers.get(i).is(player))
				return true;
		}
		return false;
	}
	
	@Override
	public CompoundTag write(CompoundTag compound) {
		//Save player
		ListTag playerNameList = new ListTag();
		for(int i = 0; i < this.bannedPlayers.size(); i++)
		{
			playerNameList.add(this.bannedPlayers.get(i).save());
		}
		compound.put("BannedPlayers", playerNameList);
		
		return compound;
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) {
		JsonArray blacklist = new JsonArray();
		for(int i = 0; i < this.bannedPlayers.size(); ++i)
		{
			blacklist.add(this.bannedPlayers.get(i).saveAsJson());
		}
		json.add("BannedPlayers", blacklist);
		return json;
	}

	@Override
	public void readNBT(CompoundTag compound) {
		
		//Load blacklisted players
		if(compound.contains("BannedPlayers", Tag.TAG_LIST))
		{
			this.bannedPlayers.clear();
			ListTag playerList = compound.getList("BannedPlayers", Tag.TAG_COMPOUND);
			for(int i = 0; i < playerList.size(); ++i)
			{
				PlayerReference reference = PlayerReference.load(playerList.getCompound(i));
				if(reference != null)
					this.bannedPlayers.add(reference);
			}
		}
		//Load player names (old method) and convert them to player references
		if(compound.contains("BannedPlayersNames", Tag.TAG_LIST))
		{
			this.bannedPlayers.clear();
			ListTag playerNameList = compound.getList("BannedPlayersNames", Tag.TAG_COMPOUND);
			for(int i = 0; i < playerNameList.size(); i++)
			{
				CompoundTag thisCompound = playerNameList.getCompound(i);
				if(thisCompound.contains("name", Tag.TAG_STRING))
				{
					PlayerReference reference = PlayerReference.of(thisCompound.getString("name"));
					if(reference != null && !this.isBlacklisted(reference))
						this.bannedPlayers.add(reference);
				}
			}
		}
		
	}
	
	@Override
	public void handleUpdateMessage(CompoundTag updateInfo) {
		boolean add = updateInfo.getBoolean("Add");
		String name = updateInfo.getString("Name");
		PlayerReference player = PlayerReference.of(name);
		if(player == null)
			return;
		if(add && !this.isBlacklisted(player))
		{
			this.bannedPlayers.add(player);
		}
		else if(!add && this.isBlacklisted(player))
		{
			PlayerReference.removeFromList(this.bannedPlayers, name);
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

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
	{
		return new GUIHandler(screen, rule);
	}
	
	@OnlyIn(Dist.CLIENT)
	private static class GUIHandler extends TradeRule.GUIHandler
	{
		
		protected final PlayerBlacklist getBlacklistRule()
		{
			if(getRuleRaw() instanceof PlayerBlacklist)
				return (PlayerBlacklist)getRuleRaw();
			return null;
		}
		
		GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
		{
			super(screen, rule);
		}
		
		EditBox nameInput;
		
		Button buttonAddPlayer;
		Button buttonRemovePlayer;
		
		ScrollTextDisplay playerDisplay;
		
		@Override
		public void initTab() {
			
			this.nameInput = this.addCustomRenderable(new EditBox(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, screen.xSize - 20, 20, new TextComponent("")));
			
			this.buttonAddPlayer = this.addCustomRenderable(new Button(screen.guiLeft() + 10, screen.guiTop() + 30, 78, 20, new TranslatableComponent("gui.button.lightmanscurrency.blacklist.add"), this::PressBlacklistButton));
			this.buttonRemovePlayer = this.addCustomRenderable(new Button(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 30, 78, 20, new TranslatableComponent("gui.button.lightmanscurrency.blacklist.remove"), this::PressForgiveButton));
			
			this.playerDisplay = this.addCustomRenderable(new ScrollTextDisplay(screen.guiLeft() + 7, screen.guiTop() + 55, this.screen.xSize - 14, 114, this.screen.getFont(), this::getBlacklistedPlayers));
			this.playerDisplay.setColumnCount(2);
			
		}
		
		private List<Component> getBlacklistedPlayers()
		{
			List<Component> playerList = Lists.newArrayList();
			if(getBlacklistRule() == null)
				return playerList;
			for(PlayerReference player : getBlacklistRule().bannedPlayers)
				playerList.add(new TextComponent(player.lastKnownName()));
			return playerList;
		}
		
		@Override
		public void renderTab(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) { }
		
		@Override
		public void onTabClose() {
			
			this.removeCustomWidget(this.nameInput);
			this.removeCustomWidget(this.buttonAddPlayer);
			this.removeCustomWidget(this.buttonRemovePlayer);
			this.removeCustomWidget(this.playerDisplay);
			
		}
		
		void PressBlacklistButton(Button button)
		{
			String name = nameInput.getValue();
			if(name != "")
			{
				nameInput.setValue("");
				PlayerReference reference = PlayerReference.of(name);
				if(!getBlacklistRule().isBlacklisted(reference))
				{
					getBlacklistRule().bannedPlayers.add(reference);
				}
				CompoundTag updateInfo = new CompoundTag();
				updateInfo.putBoolean("Add", true);
				updateInfo.putString("Name", name);
				this.screen.updateServer(TYPE, updateInfo);
			}
		}
		
		void PressForgiveButton(Button button)
		{
			String name = nameInput.getValue();
			if(name != "")
			{
				nameInput.setValue("");
				PlayerReference reference = PlayerReference.of(name);
				if(getBlacklistRule().isBlacklisted(reference))
				{
					boolean notFound = true;
					for(int i = 0; notFound && i < getBlacklistRule().bannedPlayers.size(); ++i)
					{
						if(getBlacklistRule().bannedPlayers.get(i).is(reference))
						{
							notFound = false;
							getBlacklistRule().bannedPlayers.remove(i);
						}
					}
				}
				CompoundTag updateInfo = new CompoundTag();
				updateInfo.putBoolean("Add", false);
				updateInfo.putString("Name", name);
				this.screen.updateServer(TYPE, updateInfo);
			}
		}
		
	}

	
}
