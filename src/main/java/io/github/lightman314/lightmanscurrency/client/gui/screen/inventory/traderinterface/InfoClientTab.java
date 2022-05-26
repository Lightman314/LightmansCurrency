package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.base.InfoTab;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeComparisonResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

public class InfoClientTab extends TraderInterfaceClientTab<InfoTab>{

	public InfoClientTab(TraderInterfaceScreen screen, InfoTab tab) { super(screen, tab); }

	TradeButton tradeDisplay;
	TradeButton newTradeDisplay;
	
	ScrollTextDisplay changesDisplay;
	
	DropdownWidget interactionDropdown;
	
	Button acceptChangesButton;
	
	@Override
	public IconData getIcon() { return IconData.of(Items.PAPER); }

	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.interface.info"); }
	
	@Override
	public boolean blockInventoryClosing() { return false; }
	
	@Override
	public void onOpen() {
		
		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getTradeContext, this.screen.getMenu().getBE()::getReferencedTrade, TradeButton.NULL_PRESS));
		this.tradeDisplay.move(this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 20);
		this.tradeDisplay.displayOnly = true;
		this.newTradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getTradeContext, this.screen.getMenu().getBE()::getTrueTrade, TradeButton.NULL_PRESS));
		this.newTradeDisplay.visible = false;
		this.newTradeDisplay.displayOnly = true;
		
		this.interactionDropdown = this.screen.addRenderableTabWidget(IconAndButtonUtil.interactionTypeDropdown(this.screen.getGuiLeft() + 104, this.screen.getGuiTop() + 20, 97, this.font, this.screen.getMenu().getBE().getInteractionType(), this::onInteractionSelect, this.screen::addRenderableTabWidget, this.menu.getBE().getBlacklistedInteractions()));
		
		this.changesDisplay = this.screen.addRenderableTabWidget(new ScrollTextDisplay(this.screen.getGuiLeft() + 104, this.screen.getGuiTop() + 36, 97, 73, this.font, this::getMessages));
		//Set background color to clear.
		this.changesDisplay.backgroundColor = 0x00000000;
		
		this.acceptChangesButton = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + this.screen.getXSize(), this.screen.getGuiTop() + 20, this::AcceptTradeChanges, IconAndButtonUtil.ICON_CHECKMARK, new IconAndButtonUtil.SimpleTooltip(new TranslatableComponent("tooltip.lightmanscurrency.interface.info.acceptchanges"))));
		this.acceptChangesButton.visible = false;
		
	}
	
	private List<Component> getMessages() {
		if(this.menu.getBE() == null)
			return new ArrayList<>();
		
		//Get last result
		List<Component> list = new ArrayList<>();
		TradeResult result = this.menu.getBE().mostRecentTradeResult();
		if(result.failMessage != null)
			list.add(result.failMessage);
		
		if(this.menu.getBE().getInteractionType().trades)
		{
			TradeData referencedTrade = this.menu.getBE().getReferencedTrade();
			TradeData trueTrade = this.menu.getBE().getTrueTrade();
			if(referencedTrade == null)
				return new ArrayList<>();
			if(trueTrade == null)
			{
				list.add(new TranslatableComponent("gui.lightmanscurrency.interface.difference.missing").withStyle(ChatFormatting.RED));
				return list;
			}
			TradeComparisonResult differences = referencedTrade.compare(trueTrade);
			//Type check
			if(!differences.TypeMatches())
			{
				list.add(new TranslatableComponent("gui.lightmanscurrency.interface.difference.type").withStyle(ChatFormatting.RED));
				return list;
			}
			//Trade-specific checks
			list.addAll(referencedTrade.GetDifferenceWarnings(differences));
			return list;
		}
		else if(this.menu.getBE().getInteractionType().requiresPermissions)
		{
			UniversalTraderData trader = this.menu.getBE().getTrader();
			if(trader != null && !trader.hasPermission(this.menu.getBE().getReferencedPlayer(), Permissions.INTERACTION_LINK))
			{
				list.add(new TranslatableComponent("gui.lightmanscurrency.interface.info.trader.permissions").withStyle(ChatFormatting.RED));
			}
		}
		return list;
	}

	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.menu.getBE() == null)
			return;
		
		//Trader name
		UniversalTraderData trader = this.menu.getBE().getTrader();
		Component infoText = null;
		if(trader != null)
			infoText = trader.getTitle();
		else
		{
			if(this.menu.getBE().hasTrader())
				infoText = new TranslatableComponent("gui.lightmanscurrency.interface.info.trader.removed").withStyle(ChatFormatting.RED);
			else
				infoText = new TranslatableComponent("gui.lightmanscurrency.interface.info.trader.null");
				
		}
		this.font.draw(pose, TextRenderUtil.fitString(infoText, this.screen.getXSize() - 16), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040);
		
		this.tradeDisplay.visible = this.menu.getBE().getInteractionType().trades;
		this.newTradeDisplay.visible = this.tradeDisplay.visible && this.changeInTrades();
		this.acceptChangesButton.visible = this.newTradeDisplay.visible;
		
		if(this.tradeDisplay.visible)
		{
			//If no defined trade, give "No Trade Selected" message.
			if(this.menu.getBE().getReferencedTrade() == null)
				this.font.draw(pose, new TranslatableComponent("gui.lightmanscurrency.interface.info.trade.notdefined"), this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 20, 0x404040);
		}
		if(this.newTradeDisplay.visible)
		{
			//Reposition the new trade button, as we now know it's height.
			this.newTradeDisplay.move(this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 109 - this.newTradeDisplay.getHeight());
			//Render the down arrow
			RenderSystem.setShaderTexture(0, TraderInterfaceScreen.GUI_TEXTURE);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			this.screen.blit(pose, this.screen.getGuiLeft() - 2 + (this.tradeDisplay.getWidth() / 2), this.screen.getGuiTop() + 54, TraderInterfaceScreen.WIDTH, 18, 16, 22);
			
			//If no found trade, give "Trade No Longer Exists" message.
			if(this.menu.getBE().getTrueTrade() == null)
				this.font.draw(pose, new TranslatableComponent("gui.lightmanscurrency.interface.info.trade.missing").withStyle(ChatFormatting.RED), this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 109 - this.font.lineHeight, 0x404040);
			
		}
		
		BankAccount account = this.menu.getBE().getBankAccount();
		if(account != null && this.menu.getBE().getInteractionType().trades)
		{
			Component accountName = TextRenderUtil.fitString(account.getName(), 160);
			this.font.draw(pose, accountName, this.screen.getGuiLeft() + TraderInterfaceMenu.SLOT_OFFSET + 88 - (this.font.width(accountName) / 2), this.screen.getGuiTop() + 120, 0x404040);
			Component balanceText = new TranslatableComponent("gui.lightmanscurrency.bank.balance", account.getCoinStorage().getString("0"));
			this.font.draw(pose, balanceText, this.screen.getGuiLeft() + TraderInterfaceMenu.SLOT_OFFSET + 88 - (this.font.width(balanceText) / 2), this.screen.getGuiTop() + 130, 0x404040);
		}
		
	}
	
	public boolean changeInTrades() {
		TradeData referencedTrade = this.menu.getBE().getReferencedTrade();
		TradeData trueTrade = this.menu.getBE().getTrueTrade();
		if(referencedTrade == null)
			return false;
		if(trueTrade == null)
			return true;
		return !referencedTrade.compare(trueTrade).Identical();
	}

	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
		if(this.menu.getBE() == null)
			return;
		
		//Render the currently referenced trade's tooltips (no stock or other misc stuff, just the item tooltips & original name)
		this.tradeDisplay.renderTooltips(pose, mouseX, mouseY);
		this.newTradeDisplay.renderTooltips(pose, mouseX, mouseY);
		
		IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, List.of(this.acceptChangesButton));
		
	}

	@Override
	public void tick() { }

	@Override
	public void onClose() { }
	
	private void onInteractionSelect(int newTypeIndex) {
		InteractionType newType = InteractionType.fromIndex(newTypeIndex);
		this.commonTab.changeInteractionType(newType);
	}
	
	private void AcceptTradeChanges(Button button) {
		this.commonTab.acceptTradeChanges();
	}

}
