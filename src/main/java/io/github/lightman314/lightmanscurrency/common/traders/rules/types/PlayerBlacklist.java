package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class PlayerBlacklist extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "blacklist");
	
	List<PlayerReference> bannedPlayers = new ArrayList<>();
	
	public PlayerBlacklist() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(this.isBlacklisted(event.getPlayerReference()))
			event.addDenial(EasyText.translatable("traderule.lightmanscurrency.blacklist.denial"));
	}

	public boolean isBlacklisted(PlayerReference player)
	{
		for (PlayerReference bannedPlayer : this.bannedPlayers) {
			if (bannedPlayer.is(player))
				return true;
		}
		return false;
	}
	
	@Override
	protected void saveAdditional(CompoundNBT compound) {
		//Save player
		ListNBT playerNameList = new ListNBT();
		for (PlayerReference bannedPlayer : this.bannedPlayers)
			playerNameList.add(bannedPlayer.save());
		compound.put("BannedPlayers", playerNameList);
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) {
		JsonArray blacklist = new JsonArray();
		for (PlayerReference bannedPlayer : this.bannedPlayers) {
			blacklist.add(bannedPlayer.saveAsJson());
		}
		json.add("BannedPlayers", blacklist);
		return json;
	}

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		
		//Load blacklisted players
		if(compound.contains("BannedPlayers", Constants.NBT.TAG_LIST))
		{
			this.bannedPlayers.clear();
			ListNBT playerList = compound.getList("BannedPlayers", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < playerList.size(); ++i)
			{
				PlayerReference reference = PlayerReference.load(playerList.getCompound(i));
				if(reference != null)
					this.bannedPlayers.add(reference);
			}
		}
		
	}
	
	@Override
	public void handleUpdateMessage(CompoundNBT updateInfo) {
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
	public CompoundNBT savePersistentData() { return null; }
	@Override
	public void loadPersistentData(CompoundNBT data) { }
	
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
		
		TextFieldWidget nameInput;
		
		Button buttonAddPlayer;
		Button buttonRemovePlayer;
		
		ScrollTextDisplay playerDisplay;
		
		@Override
		public void initTab() {
			
			this.nameInput = this.addCustomRenderable(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, screen.xSize - 20, 20, EasyText.empty()));
			
			this.buttonAddPlayer = this.addCustomRenderable(new Button(screen.guiLeft() + 10, screen.guiTop() + 30, 78, 20, EasyText.translatable("gui.button.lightmanscurrency.blacklist.add"), this::PressBlacklistButton));
			this.buttonRemovePlayer = this.addCustomRenderable(new Button(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 30, 78, 20, EasyText.translatable("gui.button.lightmanscurrency.blacklist.remove"), this::PressForgiveButton));
			
			this.playerDisplay = this.addCustomRenderable(new ScrollTextDisplay(screen.guiLeft() + 7, screen.guiTop() + 55, this.screen.xSize - 14, 114, this.screen.getFont(), this::getBlacklistedPlayers));
			this.playerDisplay.setColumnCount(2);
			
		}
		
		private List<ITextComponent> getBlacklistedPlayers()
		{
			List<ITextComponent> playerList = Lists.newArrayList();
			if(getBlacklistRule() == null)
				return playerList;
			for(PlayerReference player : getBlacklistRule().bannedPlayers)
				playerList.add(player.getNameComponent(true));
			return playerList;
		}
		
		@Override
		public void renderTab(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) { }
		
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
			if(!name.isEmpty())
			{
				nameInput.setValue("");
				CompoundNBT updateInfo = new CompoundNBT();
				updateInfo.putBoolean("Add", true);
				updateInfo.putString("Name", name);
				this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
			}
		}
		
		void PressForgiveButton(Button button)
		{
			String name = nameInput.getValue();
			if(!name.isEmpty())
			{
				nameInput.setValue("");
				CompoundNBT updateInfo = new CompoundNBT();
				updateInfo.putBoolean("Add", false);
				updateInfo.putString("Name", name);
				this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
			}
		}
		
	}
	
}