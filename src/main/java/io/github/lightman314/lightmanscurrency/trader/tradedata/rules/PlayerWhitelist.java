package io.github.lightman314.lightmanscurrency.trader.tradedata.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerWhitelist extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "whitelist");
	
	List<String> whitelistPlayerNames = new ArrayList<>();
	
	public PlayerWhitelist() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(!whitelistPlayerNames.contains(event.getPlayer().getDisplayName().getString()))
			event.denyTrade(new TranslatableComponent("traderule.lightmanscurrency.whitelist.denial"));
		
	}

	@Override
	public CompoundTag write(CompoundTag compound) {
		//Save player names
		ListTag playerNameList = new ListTag();
		for(int i = 0; i < whitelistPlayerNames.size(); i++)
		{
			CompoundTag thisCompound = new CompoundTag();
			thisCompound.putString("name", whitelistPlayerNames.get(i));
			playerNameList.add(thisCompound);
		}
		compound.put("WhitelistedPlayersNames", playerNameList);
		
		return compound;
	}

	@Override
	public void readNBT(CompoundTag compound) {
		
		//Load player names
		if(compound.contains("WhitelistedPlayersNames", Tag.TAG_LIST))
		{
			this.whitelistPlayerNames.clear();
			ListTag playerNameList = compound.getList("WhitelistedPlayersNames", Tag.TAG_COMPOUND);
			for(int i = 0; i < playerNameList.size(); i++)
			{
				CompoundTag thisCompound = playerNameList.getCompound(i);
				if(thisCompound.contains("name", Tag.TAG_STRING))
					this.whitelistPlayerNames.add(thisCompound.getString("name"));
			}
		}
		
	}
	
	public IconData getButtonIcon() { return IconData.of(ICON_TEXTURE, 16, 0); }

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
		
		EditBox nameInput;
		
		Button buttonAddPlayer;
		Button buttonRemovePlayer;
		
		final int namesPerPage = 11;
		
		@Override
		public void initTab() {
			
			this.nameInput = this.addCustomRenderable(new EditBox(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, screen.xSize - 20, 20, new TextComponent("")));
			
			this.buttonAddPlayer = this.screen.addCustomRenderable(new Button(screen.guiLeft() + 10, screen.guiTop() + 30, 78, 20, new TranslatableComponent("gui.button.lightmanscurrency.whitelist.add"), this::PressWhitelistButton));
			this.buttonRemovePlayer = this.screen.addCustomRenderable(new Button(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 30, 78, 20, new TranslatableComponent("gui.button.lightmanscurrency.whitelist.remove"), this::PressForgetButton));
			
		}
		
		@Override
		public void renderTab(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			
			if(getWhitelistRule() == null)
				return;
			
			this.screen.blit(matrixStack, this.screen.guiLeft(), this.screen.guiTop() + 55, 0, this.screen.ySize, this.screen.xSize, 80);
			this.screen.blit(matrixStack, this.screen.guiLeft(), this.screen.guiTop() + 55 + 80, 0, this.screen.ySize, this.screen.xSize, 34);
			
			int x = 0;
			int y = 0;
			for(int i = 0; i < getWhitelistRule().whitelistPlayerNames.size() && i < this.namesPerPage * 2; i++)
			{
				screen.getFont().draw(matrixStack, getWhitelistRule().whitelistPlayerNames.get(i), screen.guiLeft() + 10 + 78 * x, screen.guiTop() + 57 + 10 * y, 0xFFFFFF);
				y++;
				if(y >= this.namesPerPage)
				{
					y = 0;
					x++;
				}
			}
			
		}
		
		@Override
		public void onTabClose() {
			
			this.removeCustomWidget(this.nameInput);
			this.removeCustomWidget(this.buttonAddPlayer);
			this.removeCustomWidget(this.buttonRemovePlayer);
			
		}
		
		void PressWhitelistButton(Button button)
		{
			String name = nameInput.getValue();
			if(name != "")
			{
				if(!getWhitelistRule().whitelistPlayerNames.contains(name))
				{
					getWhitelistRule().whitelistPlayerNames.add(name);
					screen.markRulesDirty();
				}
				nameInput.setValue("");
			}
		}
		
		void PressForgetButton(Button button)
		{
			String name = nameInput.getValue();
			if(name != "")
			{
				if(getWhitelistRule().whitelistPlayerNames.contains(name))
				{
					getWhitelistRule().whitelistPlayerNames.remove(name);
					screen.markRulesDirty();
				}
				nameInput.setValue("");
			}
			
		}
		
	}
	
}
