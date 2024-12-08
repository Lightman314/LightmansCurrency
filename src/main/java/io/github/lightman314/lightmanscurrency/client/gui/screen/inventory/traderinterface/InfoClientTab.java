package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.trader_interface.data.TradeReference;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
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
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InfoClientTab extends TraderInterfaceClientTab<InfoTab> implements IScrollable {

	public InfoClientTab(Object screen, InfoTab tab) { super(screen, tab); }

	TradeButton tradeDisplay;
	TradeButton newTradeDisplay;
	
	DropdownWidget interactionDropdown;
	
	EasyButton acceptChangesButton;

	private final ScreenArea WARNING_AREA = ScreenArea.of(45, 69, 16, 16);

	private int scroll = 0;

	@Nonnull
    @Override
	public IconData getIcon() { return IconData.of(Items.PAPER); }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_INTERFACE_INFO.get(); }

    @Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.tradeDisplay = this.addChild(TradeButton.builder()
				.position(screenArea.pos.offset(6,47))
				.context(this::getTradeContext)
				.trade(this::getReferencedTrade)
				.displayOnly()
				.addon(EasyAddonHelper.visibleCheck(() -> this.menu.getBE().getInteractionType().trades()))
				.build());
		this.newTradeDisplay = this.addChild(TradeButton.builder()
				.position(screenArea.pos.offset(6,91))
				.context(this::getTradeContext)
				.trade(this::getTrueTrade)
				.displayOnly()
				.addon(EasyAddonHelper.visibleCheck(() -> this.menu.getBE().getInteractionType().trades() && this.changeInTrades()))
				.build());
		this.newTradeDisplay.visible = false;
		
		this.interactionDropdown = this.addChild(IconAndButtonUtil.interactionTypeDropdown(screenArea.pos.offset(104, 25), 97, this.screen.getMenu().getBE().getInteractionType(), this::onInteractionSelect, this.menu.getBE().getBlacklistedInteractions()));
		
		this.acceptChangesButton = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(181,90))
				.pressAction(this::AcceptTradeChanges)
				.icon(IconUtil.ICON_CHECKMARK)
				.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_INTERFACE_INFO_ACCEPT_CHANGES))
				.addon(EasyAddonHelper.visibleCheck(() -> this.menu.getBE().getInteractionType().trades() && this.changeInTrades()))
				.build());
		this.acceptChangesButton.visible = false;

		this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(-20,98))
				.pressAction(() -> this.changeScroll(-1))
				.icon(IconUtil.ICON_LEFT)
				.addon(EasyAddonHelper.activeCheck(() -> this.scroll > 0))
				.addon(EasyAddonHelper.visibleCheck(this::isScrollRelevant))
				.build());

		this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(screenArea.width,98))
				.pressAction(() -> this.changeScroll(1))
				.icon(IconUtil.ICON_RIGHT)
				.addon(EasyAddonHelper.activeCheck(() -> this.scroll < this.getMaxScroll() && this.getMaxScroll() > 0))
				.addon(EasyAddonHelper.visibleCheck(this::isScrollRelevant))
				.build());
		
	}
	
	private List<Component> getWarningMessages() {
		if(this.menu.getBE() == null)
			return new ArrayList<>();
		
		//Get last result
		TradeReference tradeReference = this.getTradeReference();
		if(tradeReference == null)
			return new ArrayList<>();
		List<Component> list = new ArrayList<>();
		TradeResult result = tradeReference.getLastResult();
		Component message = result.getMessage();
		if(message != null)
			list.add(message);
		
		if(this.menu.getBE().getInteractionType().trades())
		{
			TradeData referencedTrade = tradeReference.getLocalTrade();
			TradeData trueTrade = tradeReference.getTrueTrade();
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
		return list;
	}

	private TradeContext getTradeContext() {
		TraderInterfaceBlockEntity be = this.menu.getBE();
		TraderData trader =	be.targets.getTrader();
		if(trader == null)
			return null;
		return be.getTradeContext(trader);
	}

	@Nullable
	private TradeReference getTradeReference()
	{
		TraderInterfaceBlockEntity be = this.menu.getBE();
		if(be.getInteractionType().trades())
		{
			List<TradeReference> trades = be.targets.getTradeReferences();
			this.validateScroll();
			if(this.scroll < 0 || this.scroll >= trades.size())
				return null;
			return trades.get(this.scroll);
		}
		else
			return null;
	}

	@Nullable
	private TradeData getReferencedTrade()
	{
		TradeReference tradeReference = this.getTradeReference();
		return tradeReference == null ? null : tradeReference.getLocalTrade();
	}

	@Nullable
	private TradeData getTrueTrade()
	{
		TradeReference tradeReference = this.getTradeReference();
		return tradeReference == null ? null : tradeReference.getTrueTrade();
	}

	public boolean isScrollRelevant() { return this.menu.getBE().getInteractionType().trades() && this.menu.getBE().getSelectableCount() > 1; }

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		if(this.menu.getBE() == null)
			return;

		//Block name
		gui.drawString(this.menu.getBE().getBlockState().getBlock().getName(), 8, 6, 0x404040);
		//Trader name
		TraderInterfaceBlockEntity be = this.menu.getBE();
		if(be.getInteractionType().targetsTraders())
		{
			//Render list of trader names
			List<TraderData> traders = be.targets.getTraders();
			MutableComponent traderNames = EasyText.empty();
			if(traders.isEmpty())
				traderNames = LCText.GUI_INTERFACE_INFO_TRADER_NULL.get();
			else
			{
				for(TraderData trader : traders)
				{
					if(traderNames.getString().isEmpty())
						traderNames.append(trader.getTitle());
					else
						traderNames.append(LCText.GUI_SEPERATOR.get()).append(trader.getTitle());
				}
			}
			List<FormattedCharSequence> lines = gui.font.split(traderNames,this.screen.getXSize() - 40);
			for(int y = 0; y < lines.size() && y < 8; ++y)
				gui.drawString(lines.get(y),8,38 + 10 * y,0x404040);
		}
		else
		{
			//Trader Title
			TraderData trader = be.targets.getTrader();
			Component traderName;
			if(trader != null)
				traderName = trader.getTitle();
			else
				traderName = LCText.GUI_INTERFACE_INFO_TRADER_NULL.get();
			gui.drawString(TextRenderUtil.fitString(traderName, this.screen.getXSize() - 16), 8, 16, 0x404040);

			//Bank Account
			IBankAccount account = this.menu.getBE().getBankAccount();
			if(account != null)
			{
				Component accountName = TextRenderUtil.fitString(account.getName(), 160);
				gui.drawString(accountName, TraderInterfaceMenu.SLOT_OFFSET + 88 - (gui.font.width(accountName) / 2), 120, 0x404040);
				Component balanceText = account.getBalanceText();
				gui.drawString(balanceText, TraderInterfaceMenu.SLOT_OFFSET + 88 - (gui.font.width(balanceText) / 2), 130, 0x404040);
			}

			//If no trader is selected,no warnings or trade comparisons to draw
			if(trader == null)
				return;

			//Trade Comparisons
			if(this.changeInTrades())
			{
				//Render the down arrow
				gui.resetColor();
				gui.blit(TraderInterfaceScreen.GUI_TEXTURE, (this.tradeDisplay.getWidth() / 2) - 2, 67, TraderInterfaceScreen.WIDTH, 18, 16, 22);

				//If no found trade, give "Trade No Longer Exists" message.
				if(this.getTrueTrade() == null)
					gui.drawString(LCText.GUI_INTERFACE_INFO_TRADE_MISSING.getWithStyle(ChatFormatting.RED), 6, 109 - gui.font.lineHeight, 0x404040);
			}

			//Warnings
			if(!this.getWarningMessages().isEmpty())
			{
				//Render warning widget
				RenderSystem.setShaderTexture(0, TraderInterfaceScreen.GUI_TEXTURE);
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				gui.resetColor();
				gui.blit(TraderInterfaceScreen.GUI_TEXTURE, WARNING_AREA.x, WARNING_AREA.y, TraderInterfaceScreen.WIDTH, 40, 16, 16);
			}

		}
		
	}
	
	public boolean changeInTrades() {
		TradeReference tradeReference = this.getTradeReference();
		if(tradeReference == null)
			return false;
		TradeData referencedTrade = tradeReference.getLocalTrade();
		TradeData trueTrade = tradeReference.getTrueTrade();
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
	
	private void AcceptTradeChanges() { this.commonTab.acceptTradeChanges(this.scroll); }

	@Override
	public int currentScroll() { return this.scroll; }

	@Override
	public void setScroll(int newScroll) { this.scroll = newScroll; }

	private void changeScroll(int delta) { this.setScroll(this.scroll + delta); this.validateScroll(); }

	@Override
	public int getMaxScroll() { return Math.max(0,this.menu.getBE().targets.getTradeReferences().size() - 1); }

}
