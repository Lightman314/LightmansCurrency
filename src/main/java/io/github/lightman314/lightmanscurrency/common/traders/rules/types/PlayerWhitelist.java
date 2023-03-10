package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
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
	
	public boolean addToWhitelist(PlayerEntity player)
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
	protected void saveAdditional(CompoundNBT compound) {
		//Save player names
		ListNBT playerNameList = new ListNBT();
		for (PlayerReference whitelistedPlayer : this.whitelistedPlayers) {
			playerNameList.add(whitelistedPlayer.save());
		}
		compound.put("WhitelistedPlayers", playerNameList);
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) { return json; }

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		
		//Load whitelisted players
		if(compound.contains("WhitelistedPlayers", Constants.NBT.TAG_LIST))
		{
			this.whitelistedPlayers.clear();
			ListNBT playerList = compound.getList("WhitelistedPlayers", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < playerList.size(); ++i)
			{
				PlayerReference reference = PlayerReference.load(playerList.getCompound(i));
				if(reference != null)
					this.whitelistedPlayers.add(reference);
			}
		}
		//Load player names (old method) and convert them to player references
		if(compound.contains("WhitelistedPlayersNames", Constants.NBT.TAG_LIST))
		{
			this.whitelistedPlayers.clear();
			ListNBT playerNameList = compound.getList("WhitelistedPlayersNames", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < playerNameList.size(); i++)
			{
				CompoundNBT thisCompound = playerNameList.getCompound(i);
				if(thisCompound.contains("name", Constants.NBT.TAG_STRING))
				{
					PlayerReference reference = PlayerReference.of(false, thisCompound.getString("name"));
					if(reference != null && !this.isWhitelisted(reference))
						this.whitelistedPlayers.add(reference);
				}
					
			}
		}
		
	}
	
	@Override
	public void loadFromJson(JsonObject json) {}
	
	@Override
	public void handleUpdateMessage(CompoundNBT updateInfo)
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
	public CompoundNBT savePersistentData() {
		CompoundNBT compound = new CompoundNBT();
		this.saveAdditional(compound);
		return compound;
	}
	@Override
	public void loadPersistentData(CompoundNBT data) { this.loadAdditional(data); }
	
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
			
			this.nameInput = this.addCustomRenderable(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, screen.xSize - 20, 20, EasyText.empty()));
			
			this.buttonAddPlayer = this.screen.addCustomRenderable(new Button(screen.guiLeft() + 10, screen.guiTop() + 30, 78, 20, EasyText.translatable("gui.button.lightmanscurrency.whitelist.add"), this::PressWhitelistButton));
			this.buttonRemovePlayer = this.screen.addCustomRenderable(new Button(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 30, 78, 20, EasyText.translatable("gui.button.lightmanscurrency.whitelist.remove"), this::PressForgetButton));
			
			//Player list display
			this.playerDisplay = this.screen.addCustomRenderable(new ScrollTextDisplay(screen.guiLeft() + 7, screen.guiTop() + 55, this.screen.xSize - 14, 114, this.screen.getFont(), this::getWhitelistedPlayers));
			this.playerDisplay.setColumnCount(2);
			
		}
		
		private List<ITextComponent> getWhitelistedPlayers()
		{
			List<ITextComponent> playerList = Lists.newArrayList();
			if(getWhitelistRule() == null)
				return playerList;
			for(PlayerReference player : getWhitelistRule().whitelistedPlayers)
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
		
		void PressWhitelistButton(Button button)
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
		
		void PressForgetButton(Button button)
		{
			String name = nameInput.getValue();
			if(!name.equals(""))
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