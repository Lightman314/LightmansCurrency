package io.github.lightman314.lightmanscurrency.tradedata.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class PlayerWhitelist extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "whitelist");
	
	List<String> whitelistPlayerNames = new ArrayList<>();
	
	public PlayerWhitelist() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(!whitelistPlayerNames.contains(event.getPlayer().getDisplayName().getString()))
			event.setCanceled(true);
		
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		//Save player names
		ListNBT playerNameList = new ListNBT();
		for(int i = 0; i < whitelistPlayerNames.size(); i++)
		{
			CompoundNBT thisCompound = new CompoundNBT();
			thisCompound.putString("name", whitelistPlayerNames.get(i));
			playerNameList.add(thisCompound);
		}
		compound.put("WhitelistedPlayersNames", playerNameList);
		
		return compound;
	}

	@Override
	public void readNBT(CompoundNBT compound) {
		
		//Load player names
		if(compound.contains("WhitelistedPlayersNames", Constants.NBT.TAG_LIST))
		{
			this.whitelistPlayerNames.clear();
			ListNBT playerNameList = compound.getList("WhitelistedPlayersNames", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < playerNameList.size(); i++)
			{
				CompoundNBT thisCompound = playerNameList.getCompound(i);
				if(thisCompound.contains("name", Constants.NBT.TAG_STRING))
					this.whitelistPlayerNames.add(thisCompound.getString("name"));
			}
		}
		
	}
	
	@Override
	public int getGUIX() { return 16; }

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
		
		final int namesPerPage = 11;
		
		@Override
		public void initTab() {
			
			this.nameInput = new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, screen.xSize - 20, 20, new StringTextComponent(""));
			screen.addCustomListener(this.nameInput);
			
			this.buttonAddPlayer = this.screen.addCustomButton(new Button(screen.guiLeft() + 10, screen.guiTop() + 30, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.whitelist.add"), this::PressWhitelistButton));
			this.buttonRemovePlayer = this.screen.addCustomButton(new Button(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 30, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.whitelist.remove"), this::PressForgetButton));
			
		}
		
		@Override
		public void renderTab(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			
			if(getWhitelistRule() == null)
				return;
			
			this.screen.blit(matrixStack, this.screen.guiLeft(), this.screen.guiTop() + 55, 0, this.screen.ySize, this.screen.xSize, 80);
			this.screen.blit(matrixStack, this.screen.guiLeft(), this.screen.guiTop() + 55 + 80, 0, this.screen.ySize, this.screen.xSize, 34);
			
			this.nameInput.render(matrixStack, mouseX, mouseY, partialTicks);
			
			int x = 0;
			int y = 0;
			for(int i = 0; i < getWhitelistRule().whitelistPlayerNames.size() && i < this.namesPerPage * 2; i++)
			{
				screen.getFont().drawString(matrixStack, getWhitelistRule().whitelistPlayerNames.get(i), screen.guiLeft() + 10 + 78 * x, screen.guiTop() + 57 + 10 * y, 0xFFFFFF);
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
			
			screen.removeListener(this.nameInput);
			screen.removeButton(this.buttonAddPlayer);
			screen.removeButton(this.buttonRemovePlayer);
			
		}
		
		void PressWhitelistButton(Button button)
		{
			String name = nameInput.getText();
			if(name != "")
			{
				if(!getWhitelistRule().whitelistPlayerNames.contains(name))
				{
					getWhitelistRule().whitelistPlayerNames.add(name);
					screen.markRulesDirty();
				}
				nameInput.setText("");
			}
		}
		
		void PressForgetButton(Button button)
		{
			String name = nameInput.getText();
			if(name != "")
			{
				if(getWhitelistRule().whitelistPlayerNames.contains(name))
				{
					getWhitelistRule().whitelistPlayerNames.remove(name);
					screen.markRulesDirty();
				}
				nameInput.setText("");
			}
			
		}
		
	}
	
}
