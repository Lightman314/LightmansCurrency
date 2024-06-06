package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base.InfoTab;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class InfoClientTab extends TraderInterfaceClientTab<InfoTab>{

	public InfoClientTab(TraderInterfaceScreen screen, InfoTab tab) { super(screen, tab); }

	TradeButton tradeDisplay;
	TradeButton newTradeDisplay;
	
	DropdownWidget interactionDropdown;
	
	EasyButton acceptChangesButton;

	private final ScreenArea WARNING_AREA = ScreenArea.of(45, 69, 16, 16);
	
	@Nonnull
    @Override
	public @NotNull IconData getIcon() { return IconData.of(Items.PAPER); }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_INTERFACE_INFO.get(); }

    @Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.tradeDisplay = this.addChild(new TradeButton(this.menu::getTradeContext, this.screen.getMenu().getBE()::getReferencedTrade, TradeButton.NULL_PRESS));
		this.tradeDisplay.setPosition(screenArea.pos.offset(6, 47));
		this.tradeDisplay.displayOnly = true;
		this.newTradeDisplay = this.addChild(new TradeButton(this.menu::getTradeContext, this.screen.getMenu().getBE()::getTrueTrade, TradeButton.NULL_PRESS));
		this.newTradeDisplay.setPosition(screenArea.pos.offset(6, 91));
		this.newTradeDisplay.visible = false;
		this.newTradeDisplay.displayOnly = true;
		
		this.interactionDropdown = this.addChild(IconAndButtonUtil.interactionTypeDropdown(screenArea.pos.offset(104, 25), 97, this.screen.getMenu().getBE().getInteractionType(), this::onInteractionSelect, this.menu.getBE().getBlacklistedInteractions()));
		
		this.acceptChangesButton = this.addChild(new IconButton(screenArea.pos.offset(181,90), this::AcceptTradeChanges, IconAndButtonUtil.ICON_CHECKMARK)
				.withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_INTERFACE_INFO_ACCEPT_CHANGES.get())));
		this.acceptChangesButton.visible = false;
		
	}
	
	private List<Component> getWarningMessages() {
		if(this.menu.getBE() == null)
			return new ArrayList<>();
		
		//Get last result
		List<Component> list = new ArrayList<>();
		TradeResult result = this.menu.getBE().mostRecentTradeResult();
		Component message = result.getMessage();
		if(message != null)
			list.add(message);
		
		if(this.menu.getBE().getInteractionType().trades)
		{
			TradeData referencedTrade = this.menu.getBE().getReferencedTrade();
			TradeData trueTrade = this.menu.getBE().getTrueTrade();
			if(referencedTrade == null)
				return new ArrayList<>();
			if(trueTrade == null)
			{
				list.add(LCText.GUI_TRADE_DIFFERENCE_MISSING.getWithStyle(ChatFormatting.RED));
				return list;
			}
			TradeComparisonResult differences = trueTrade.compare(referencedTrade);
			//Type check
			if(!differences.TypeMatches())
			{
				list.add(LCText.GUI_TRADE_DIFFERENCE_TYPE.getWithStyle(ChatFormatting.RED));
				return list;
			}
			//Trade-specific checks
			list.addAll(trueTrade.GetDifferenceWarnings(differences));
			return list;
		}
		else if(this.menu.getBE().getInteractionType().requiresPermissions)
		{
			TraderData trader = this.menu.getBE().getTrader();
			if(trader != null && !trader.hasPermission(this.menu.getBE().getReferencedPlayer(), Permissions.INTERACTION_LINK))
			{
				list.add(LCText.GUI_INTERFACE_INFO_MISSING_PERMISSIONS.getWithStyle(ChatFormatting.RED));
			}
		}
		return list;
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		if(this.menu.getBE() == null)
			return;

		//Block name
		gui.drawString(this.menu.getBE().getBlockState().getBlock().getName(), 8, 6, 0x404040);
		//Trader name
		TraderData trader = this.menu.getBE().getTrader();
		Component infoText;
		if(trader != null)
			infoText = trader.getTitle();
		else
		{
			if(this.menu.getBE().hasTrader())
				infoText = LCText.GUI_INTERFACE_INFO_TRADER_REMOVED.getWithStyle(ChatFormatting.RED);
			else
				infoText = LCText.GUI_INTERFACE_INFO_TRADER_NULL.get();
		}
		gui.drawString(TextRenderUtil.fitString(infoText, this.screen.getXSize() - 16), 8, 16, 0x404040);
		
		this.tradeDisplay.visible = this.menu.getBE().getInteractionType().trades;
		this.newTradeDisplay.visible = this.tradeDisplay.visible && this.changeInTrades();
		this.acceptChangesButton.visible = this.newTradeDisplay.visible;
		
		if(this.tradeDisplay.visible)
		{
			//If no defined trade, give "No Trade Selected" message.
			if(this.menu.getBE().getReferencedTrade() == null)
				gui.drawString(LCText.GUI_INTERFACE_INFO_TRADE_NOT_DEFINED.get(), 6, 40, 0x404040);
		}
		if(this.newTradeDisplay.visible)
		{
			//Render the down arrow
			gui.resetColor();
			gui.blit(TraderInterfaceScreen.GUI_TEXTURE, (this.tradeDisplay.getWidth() / 2) - 2, 67, TraderInterfaceScreen.WIDTH, 18, 16, 22);
			
			//If no found trade, give "Trade No Longer Exists" message.
			if(this.menu.getBE().getTrueTrade() == null)
				gui.drawString(LCText.GUI_INTERFACE_INFO_TRADE_MISSING.getWithStyle(ChatFormatting.RED), 6, 109 - gui.font.lineHeight, 0x404040);
			
		}

		IBankAccount account = this.menu.getBE().getBankAccount();
		if(account != null && this.menu.getBE().getInteractionType().trades)
		{
			Component accountName = TextRenderUtil.fitString(account.getName(), 160);
			gui.drawString(accountName, TraderInterfaceMenu.SLOT_OFFSET + 88 - (gui.font.width(accountName) / 2), 120, 0x404040);
			Component balanceText = account.getBalanceText();
			gui.drawString(balanceText, TraderInterfaceMenu.SLOT_OFFSET + 88 - (gui.font.width(balanceText) / 2), 130, 0x404040);
		}

		if(!this.getWarningMessages().isEmpty())
		{
			//Render warning widget
			RenderSystem.setShaderTexture(0, TraderInterfaceScreen.GUI_TEXTURE);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			gui.resetColor();
			gui.blit(TraderInterfaceScreen.GUI_TEXTURE, WARNING_AREA.x, WARNING_AREA.y, TraderInterfaceScreen.WIDTH, 40, 16, 16);
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
	public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		
		if(this.menu.getBE() == null)
			return;

		if(WARNING_AREA.atPosition(WARNING_AREA.pos.offset(this.screen)).isMouseInArea(gui.mousePos))
		{
			List<Component> warnings = this.getWarningMessages();
			if(!warnings.isEmpty())
				gui.renderComponentTooltip(warnings);
		}
		
	}
	
	private void onInteractionSelect(int newTypeIndex) {
		InteractionType newType = InteractionType.fromIndex(newTypeIndex);
		this.commonTab.changeInteractionType(newType);
	}
	
	private void AcceptTradeChanges(EasyButton button) {
		this.commonTab.acceptTradeChanges();
	}

}
