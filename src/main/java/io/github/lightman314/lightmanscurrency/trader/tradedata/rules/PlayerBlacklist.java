package io.github.lightman314.lightmanscurrency.trader.tradedata.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
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

public class PlayerBlacklist extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "blacklist");
	
	List<PlayerReference> bannedPlayers = new ArrayList<>();
	
	public PlayerBlacklist() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(this.isBlacklisted(event.getPlayerReference()))
			event.denyTrade(new TranslationTextComponent("traderule.lightmanscurrency.blacklist.denial"));
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
	public CompoundNBT write(CompoundNBT compound) {
		//Save player
		ListNBT playerNameList = new ListNBT();
		for(int i = 0; i < this.bannedPlayers.size(); i++)
		{
			playerNameList.add(this.bannedPlayers.get(i).save());
		}
		compound.put("BannedPlayers", playerNameList);
		
		return compound;
	}

	@Override
	public void readNBT(CompoundNBT compound) {
		
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
		//Load player names (old method) and convert them to player references
		if(compound.contains("BannedPlayersNames", Constants.NBT.TAG_LIST))
		{
			this.bannedPlayers.clear();
			ListNBT playerNameList = compound.getList("BannedPlayersNames", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < playerNameList.size(); i++)
			{
				CompoundNBT thisCompound = playerNameList.getCompound(i);
				if(thisCompound.contains("name", Constants.NBT.TAG_STRING))
				{
					PlayerReference reference = PlayerReference.of(thisCompound.getString("name"));
					if(reference != null && !this.isBlacklisted(reference))
						this.bannedPlayers.add(reference);
				}
			}
		}
		
	}
	
	@Override
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
			
			this.nameInput = new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, screen.xSize - 20, 20, new StringTextComponent(""));
			screen.addCustomListener(this.nameInput);
			
			this.buttonAddPlayer = this.screen.addCustomButton(new Button(screen.guiLeft() + 10, screen.guiTop() + 30, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.blacklist.add"), this::PressBlacklistButton));
			this.buttonRemovePlayer = this.screen.addCustomButton(new Button(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 30, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.blacklist.remove"), this::PressForgiveButton));
			
			this.playerDisplay = this.addListener(new ScrollTextDisplay(screen.guiLeft() + 7, screen.guiTop() + 55, this.screen.xSize - 14, 114, this.screen.getFont(), this::getBlacklistedPlayers));
			this.playerDisplay.setColumnCount(2);
			
		}
		
		private List<ITextComponent> getBlacklistedPlayers()
		{
			List<ITextComponent> playerList = Lists.newArrayList();
			if(getBlacklistRule() == null)
				return playerList;
			for(PlayerReference player : getBlacklistRule().bannedPlayers)
				playerList.add(new StringTextComponent(player.lastKnownName()));
			return playerList;
		}
		
		@Override
		public void renderTab(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			this.playerDisplay.render(matrixStack, mouseX, mouseY, partialTicks);
		}
		
		@Override
		public void onTabClose() {
			
			screen.removeListener(this.nameInput);
			screen.removeButton(this.buttonAddPlayer);
			screen.removeButton(this.buttonRemovePlayer);
			screen.removeListener(this.playerDisplay);
			
		}
		
		void PressBlacklistButton(Button button)
		{
			String name = nameInput.getText();
			if(name != "")
			{
				nameInput.setText("");
				PlayerReference reference = PlayerReference.of(name);
				if(reference != null)
				{
					if(!getBlacklistRule().isBlacklisted(reference))
					{
						getBlacklistRule().bannedPlayers.add(reference);
						screen.markRulesDirty();
					}
				}
			}
		}
		
		void PressForgiveButton(Button button)
		{
			String name = nameInput.getText();
			if(name != "")
			{
				nameInput.setText("");
				PlayerReference reference = PlayerReference.of(name);
				if(reference != null)
				{
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
						screen.markRulesDirty();
					}
				}
			}
			
		}
		
	}

	
}
