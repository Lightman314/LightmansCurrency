package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
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
import net.minecraft.client.gui.components.Button;
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

	Button acceptChangesButton;

	private final ScreenArea WARNING_AREA = ScreenArea.of(ScreenPosition.ZERO, 16, 16);
	private ScreenArea currentWarningArea;

	@Nonnull
    @Override
	public @NotNull IconData getIcon() { return IconData.of(Items.PAPER); }

	@Override
	public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.interface.info"); }

	@Override
	public boolean blockInventoryClosing() { return false; }

	@Override
	public void onOpen() {

		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getTradeContext, this.screen.getMenu().getBE()::getReferencedTrade, TradeButton.NULL_PRESS));
		this.tradeDisplay.move(this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 47);
		this.tradeDisplay.displayOnly = true;
		this.newTradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getTradeContext, this.screen.getMenu().getBE()::getTrueTrade, TradeButton.NULL_PRESS));
		this.newTradeDisplay.move(this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 91);
		this.newTradeDisplay.visible = false;
		this.newTradeDisplay.displayOnly = true;

		this.currentWarningArea = this.WARNING_AREA.atPosition(ScreenPosition.of(45, 69).offset(this.screen));

		this.interactionDropdown = this.screen.addRenderableTabWidget(IconAndButtonUtil.interactionTypeDropdown(this.screen.getGuiLeft() + 104, this.screen.getGuiTop() + 25, 97, this.font, this.screen.getMenu().getBE().getInteractionType(), this::onInteractionSelect, this.screen::addRenderableTabWidget, this.menu.getBE().getBlacklistedInteractions()));

		this.acceptChangesButton = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + 181, this.screen.getGuiTop() + 90, this::AcceptTradeChanges, IconAndButtonUtil.ICON_CHECKMARK, new IconAndButtonUtil.SimpleTooltip(EasyText.translatable("tooltip.lightmanscurrency.interface.info.acceptchanges"))));
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
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {

		if(this.menu.getBE() == null)
			return;

		//Block name
		this.font.draw(pose, this.menu.getBE().getBlockState().getBlock().getName(), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040);
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
		this.font.draw(pose, TextRenderUtil.fitString(infoText, this.screen.getXSize() - 16), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 16, 0x404040);

		this.tradeDisplay.visible = this.menu.getBE().getInteractionType().trades;
		this.newTradeDisplay.visible = this.tradeDisplay.visible && this.changeInTrades();
		this.acceptChangesButton.visible = this.newTradeDisplay.visible;

		if(this.tradeDisplay.visible)
		{
			//If no defined trade, give "No Trade Selected" message.
			if(this.menu.getBE().getReferencedTrade() == null)
				this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.interface.info.trade.notdefined"), this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 40, 0x404040);
		}
		if(this.newTradeDisplay.visible)
		{
			//Render the down arrow
			RenderSystem.setShaderTexture(0, TraderInterfaceScreen.GUI_TEXTURE);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			this.screen.blit(pose, this.screen.getGuiLeft() - 2 + (this.tradeDisplay.getWidth() / 2), this.screen.getGuiTop() + 67, TraderInterfaceScreen.WIDTH, 18, 16, 22);

			//If no found trade, give "Trade No Longer Exists" message.
			if(this.menu.getBE().getTrueTrade() == null)
				this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.interface.info.trade.missing").withStyle(ChatFormatting.RED), this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 109 - this.font.lineHeight, 0x404040);

		}

		BankAccount account = this.menu.getBE().getBankAccount();
		if(account != null && this.menu.getBE().getInteractionType().trades)
		{
			Component accountName = TextRenderUtil.fitString(account.getName(), 160);
			this.font.draw(pose, accountName, this.screen.getGuiLeft() + TraderInterfaceMenu.SLOT_OFFSET + 88 - (this.font.width(accountName) / 2), this.screen.getGuiTop() + 120, 0x404040);
			Component balanceText = EasyText.translatable("gui.lightmanscurrency.bank.balance", account.getCoinStorage().getString("0"));
			this.font.draw(pose, balanceText, this.screen.getGuiLeft() + TraderInterfaceMenu.SLOT_OFFSET + 88 - (this.font.width(balanceText) / 2), this.screen.getGuiTop() + 130, 0x404040);
		}

		if(this.getWarningMessages().size() > 0)
		{
			//Render warning widget
			RenderSystem.setShaderTexture(0, TraderInterfaceScreen.GUI_TEXTURE);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			this.screen.blit(pose, this.currentWarningArea.x, this.currentWarningArea.y, TraderInterfaceScreen.WIDTH, 40, 16, 16);
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

		if(this.currentWarningArea.isMouseInArea(mouseX, mouseY))
		{
			List<Component> warnings = this.getWarningMessages();
			if(warnings.size() > 0)
				this.screen.renderComponentTooltip(pose, warnings, mouseX, mouseY);
		}

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