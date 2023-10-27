package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceClientTab;
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
	
	@Override
	public @NotNull IconData getIcon() { return IconData.of(Items.PAPER); }

	@Override
	public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.interface.info"); }
	
	@Override
	public boolean blockInventoryClosing() { return false; }

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
				.withAddons(EasyAddonHelper.tooltip(EasyText.translatable("tooltip.lightmanscurrency.interface.info.acceptchanges"))));
		this.acceptChangesButton.visible = false;
		
	}
	
	private List<Component> getWarningMessages() {
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
				list.add(EasyText.translatable("gui.lightmanscurrency.interface.difference.missing").withStyle(ChatFormatting.RED));
				return list;
			}
			TradeComparisonResult differences = referencedTrade.compare(trueTrade);
			//Type check
			if(!differences.TypeMatches())
			{
				list.add(EasyText.translatable("gui.lightmanscurrency.interface.difference.type").withStyle(ChatFormatting.RED));
				return list;
			}
			//Trade-specific checks
			list.addAll(referencedTrade.GetDifferenceWarnings(differences));
			return list;
		}
		else if(this.menu.getBE().getInteractionType().requiresPermissions)
		{
			TraderData trader = this.menu.getBE().getTrader();
			if(trader != null && !trader.hasPermission(this.menu.getBE().getReferencedPlayer(), Permissions.INTERACTION_LINK))
			{
				list.add(EasyText.translatable("gui.lightmanscurrency.interface.info.trader.permissions").withStyle(ChatFormatting.RED));
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
				infoText = EasyText.translatable("gui.lightmanscurrency.interface.info.trader.removed").withStyle(ChatFormatting.RED);
			else
				infoText = EasyText.translatable("gui.lightmanscurrency.interface.info.trader.null");
		}
		gui.drawString(TextRenderUtil.fitString(infoText, this.screen.getXSize() - 16), 8, 16, 0x404040);
		
		this.tradeDisplay.visible = this.menu.getBE().getInteractionType().trades;
		this.newTradeDisplay.visible = this.tradeDisplay.visible && this.changeInTrades();
		this.acceptChangesButton.visible = this.newTradeDisplay.visible;
		
		if(this.tradeDisplay.visible)
		{
			//If no defined trade, give "No Trade Selected" message.
			if(this.menu.getBE().getReferencedTrade() == null)
				gui.drawString(EasyText.translatable("gui.lightmanscurrency.interface.info.trade.notdefined"), 6, 40, 0x404040);
		}
		if(this.newTradeDisplay.visible)
		{
			//Render the down arrow
			gui.resetColor();
			gui.blit(TraderInterfaceScreen.GUI_TEXTURE, (this.tradeDisplay.getWidth() / 2) - 2, 67, TraderInterfaceScreen.WIDTH, 18, 16, 22);
			
			//If no found trade, give "Trade No Longer Exists" message.
			if(this.menu.getBE().getTrueTrade() == null)
				gui.drawString(EasyText.translatable("gui.lightmanscurrency.interface.info.trade.missing").withStyle(ChatFormatting.RED), 6, 109 - gui.font.lineHeight, 0x404040);
			
		}
		
		BankAccount account = this.menu.getBE().getBankAccount();
		if(account != null && this.menu.getBE().getInteractionType().trades)
		{
			Component accountName = TextRenderUtil.fitString(account.getName(), 160);
			gui.drawString(accountName, TraderInterfaceMenu.SLOT_OFFSET + 88 - (gui.font.width(accountName) / 2), 120, 0x404040);
			Component balanceText = EasyText.translatable("gui.lightmanscurrency.bank.balance", account.getCoinStorage().getComponent("0"));
			gui.drawString(balanceText, TraderInterfaceMenu.SLOT_OFFSET + 88 - (gui.font.width(balanceText) / 2), 130, 0x404040);
		}

		if(this.getWarningMessages().size() > 0)
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
			if(warnings.size() > 0)
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
