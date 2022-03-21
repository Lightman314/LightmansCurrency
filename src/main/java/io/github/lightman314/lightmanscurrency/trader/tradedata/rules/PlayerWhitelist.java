package io.github.lightman314.lightmanscurrency.trader.tradedata.rules;

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
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class PlayerWhitelist extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "whitelist");
	
	List<PlayerReference> whitelistedPlayers = new ArrayList<>();
	
	public PlayerWhitelist() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(!this.isWhitelisted(event.getPlayerReference()))
			event.denyTrade(new TranslationTextComponent("traderule.lightmanscurrency.whitelist.denial"));
		
	}

	public boolean isWhitelisted(PlayerReference player)
	{
		for(int i = 0; i < this.whitelistedPlayers.size(); ++i)
		{
			if(this.whitelistedPlayers.get(i).is(player))
				return true;
		}
		return false;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		//Save player names
		ListNBT playerNameList = new ListNBT();
		for(int i = 0; i < this.whitelistedPlayers.size(); i++)
		{
			playerNameList.add(this.whitelistedPlayers.get(i).save());
		}
		compound.put("WhitelistedPlayers", playerNameList);
		
		return compound;
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) {
		JsonArray whitelist = new JsonArray();
		for(int i = 0; i < this.whitelistedPlayers.size(); ++i)
		{
			whitelist.add(this.whitelistedPlayers.get(i).saveAsJson());
		}
		json.add("WhitelistedPlayers", whitelist);
		return json;
	}

	@Override
	public void readNBT(CompoundNBT compound) {
		
		//Load whitelisted players
		if(compound.contains("WhitelistedPlayers", Constants.NBT.TAG_LIST))
		{
			this.whitelistedPlayers.clear();
			ListNBT playerList = compound.getList("WhitelistedPlayers", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < playerList.size(); i++)
			{
				PlayerReference reference = PlayerReference.load(playerList.getCompound(i));
				if(reference != null)
					this.whitelistedPlayers.add(reference);
			}
		}
		//Load player names
		if(compound.contains("WhitelistedPlayersNames", Constants.NBT.TAG_LIST))
		{
			this.whitelistedPlayers.clear();
			ListNBT playerNameList = compound.getList("WhitelistedPlayersNames", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < playerNameList.size(); i++)
			{
				CompoundNBT thisCompound = playerNameList.getCompound(i);
				if(thisCompound.contains("name", Constants.NBT.TAG_STRING))
				{
					PlayerReference reference = PlayerReference.of(thisCompound.getString("name"));
					if(reference != null && !this.isWhitelisted(reference))
						this.whitelistedPlayers.add(reference);
				}
			}
		}
		
	}
	
	@Override
	public void loadFromJson(JsonObject json) {
		if(json.has("WhitelistedPlayers"))
		{
			this.whitelistedPlayers.clear();
			JsonArray whitelist = json.get("WhitelistedPlayers").getAsJsonArray();
			for(int i = 0; i < whitelist.size(); ++i) {
				PlayerReference reference = PlayerReference.load(whitelist.get(i).getAsJsonObject());
				if(reference != null && !this.isWhitelisted(reference))
					this.whitelistedPlayers.add(reference);
			}
		}
	}
	
	@Override
	public void handleUpdateMessage(CompoundNBT updateInfo)
	{
		boolean add = updateInfo.getBoolean("Add");
		String name = updateInfo.getString("Name");
		PlayerReference player = PlayerReference.of(name);
		if(player == null)
			return;
		if(add && !this.isWhitelisted(player))
		{
			this.whitelistedPlayers.add(player);
		}
		else if(!add && this.isWhitelisted(player))
		{
			PlayerReference.removeFromList(this.whitelistedPlayers, name);
		}
	}
	
	@Override
	public CompoundNBT savePersistentData() { return null; }
	@Override
	public void loadPersistentData(CompoundNBT data) { }
	
	@Override
	public IconData getButtonIcon() { return IconAndButtonUtil.ICON_WHITELIST; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
	{
		return new GUIHandler(screen, rule);
	}
	
	@OnlyIn(Dist.CLIENT)
	private static class GUIHandler extends TradeRule.GUIHandler
	{
		
		protected final PlayerWhitelist getWhitelistRule()
		{
			if(getRuleRaw() instanceof PlayerWhitelist)
				return (PlayerWhitelist)getRuleRaw();
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
			
			this.nameInput = new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, screen.xSize - 20, 20, new StringTextComponent(""));
			screen.addCustomListener(this.nameInput);
			
			this.buttonAddPlayer = this.screen.addCustomButton(new Button(screen.guiLeft() + 10, screen.guiTop() + 30, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.whitelist.add"), this::PressWhitelistButton));
			this.buttonRemovePlayer = this.screen.addCustomButton(new Button(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 30, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.whitelist.remove"), this::PressForgetButton));
			
			this.playerDisplay = this.addListener(new ScrollTextDisplay(screen.guiLeft() + 7, screen.guiTop() + 55, this.screen.xSize - 14, 114, this.screen.getFont(), this::getWhitelistedPlayers));
			this.playerDisplay.setColumnCount(2);
			
		}
		
		private List<ITextComponent> getWhitelistedPlayers()
		{
			List<ITextComponent> playerList = Lists.newArrayList();
			if(getWhitelistRule() == null)
				return playerList;
			for(PlayerReference player : getWhitelistRule().whitelistedPlayers)
				playerList.add(new StringTextComponent(player.lastKnownName()));
			return playerList;
		}
		
		@Override
		public void renderTab(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			
			if(getWhitelistRule() == null)
				return;
			
			this.nameInput.render(matrixStack, mouseX, mouseY, partialTicks);
			this.playerDisplay.render(matrixStack, mouseX, mouseY, partialTicks);
			
		}
		
		@Override
		public void onTabClose() {
			
			screen.removeListener(this.nameInput);
			screen.removeButton(this.buttonAddPlayer);
			screen.removeButton(this.buttonRemovePlayer);
			screen.removeListener(this.playerDisplay);
			
		}
		
		void PressWhitelistButton(Button button)
		{
			String name = nameInput.getText();
			if(name != "")
			{
				nameInput.setText("");
				PlayerReference reference = PlayerReference.of(name);
				if(reference != null)
				{
					if(!getWhitelistRule().isWhitelisted(reference))
					{
						getWhitelistRule().whitelistedPlayers.add(reference);
					}
				}
				CompoundNBT updateInfo = new CompoundNBT();
				updateInfo.putBoolean("Add", true);
				updateInfo.putString("Name", name);
				this.screen.updateServer(TYPE, updateInfo);
			}
		}
		
		void PressForgetButton(Button button)
		{
			String name = nameInput.getText();
			if(name != "")
			{
				nameInput.setText("");
				PlayerReference reference = PlayerReference.of(name);
				if(reference != null)
				{
					if(getWhitelistRule().isWhitelisted(reference))
					{
						boolean notFound = true;
						for(int i = 0; notFound && i < getWhitelistRule().whitelistedPlayers.size(); ++i)
						{
							if(getWhitelistRule().whitelistedPlayers.get(i).is(reference))
							{
								notFound = false;
								getWhitelistRule().whitelistedPlayers.remove(i);
							}
						}
					}
				}
				CompoundNBT updateInfo = new CompoundNBT();
				updateInfo.putBoolean("Add", false);
				updateInfo.putString("Name", name);
				this.screen.updateServer(TYPE, updateInfo);
			}
			
		}
		
	}
	
}
