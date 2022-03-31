package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget.IItemEditListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.item.ItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

public class ItemTradeEditClientTab extends TraderStorageClientTab<ItemTradeEditTab> implements InteractionConsumer, ICoinValueInput, IItemEditListener {

	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 71;
	private static final int COLUMNS = 10;
	private static final int ROWS = 2;
	
	public ItemTradeEditClientTab(TraderStorageScreen screen, ItemTradeEditTab commonTab) {
		super(screen, commonTab); 
	}
	
	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

	@Override
	public Component getTooltip() { return new TextComponent(""); }

	@Override
	public boolean tabButtonVisible() { return false; }
	
	@Override
	public boolean blockInventoryClosing() { return true; }
	
	@Override
	public int getTradeRuleTradeIndex() { return this.commonTab.getTradeIndex(); }

	TradeButton tradeDisplay;
	CoinValueInput priceSelection;
	EditBox customNameInput;
	
	ItemEditWidget itemEdit;
	
	Button buttonToggleTradeType;
	
	private int selection = -1;
	
	@Override
	public void onOpen() {
		
		ItemTradeData trade = this.getTrade();
		
		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getContext, this.commonTab::getTrade, button -> {}));
		this.tradeDisplay.move(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 18);
		this.priceSelection = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiTop() + 40, new TextComponent(""), trade == null ? CoinValue.EMPTY : trade.getCost(), this));
		this.priceSelection.drawBG = false;
		this.priceSelection.init();
		
		this.itemEdit = this.screen.addRenderableTabWidget(new ItemEditWidget(this.screen.getGuiLeft() + X_OFFSET, this.screen.getGuiTop() + Y_OFFSET, COLUMNS, ROWS, this));
		this.itemEdit.init(this.screen::addRenderableTabWidget, this.screen::addTabListener);
		
		this.customNameInput = this.screen.addRenderableTabWidget(new EditBox(this.font, this.screen.getGuiLeft() + 13, this.screen.getGuiTop() + 38, this.screen.getXSize() - 26, 18, new TextComponent("")));
		if(this.selection >= 0 && this.selection < 2 && trade != null)
			this.customNameInput.setValue(trade.getCustomName(this.selection));
		
		this.buttonToggleTradeType = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 113, this.screen.getGuiTop() + 15, 80, 20, new TextComponent(""), this::ToggleTradeType));
		
	}
	
	@Override
	public void onClose() { this.selection = -1; }

	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.getTrade() == null)
			return;
		
		this.validateRenderables();
		
		//Render a down arrow over the selected position
		RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		this.screen.blit(pose, this.getArrowPosition(), this.screen.getGuiTop() + 10, TraderScreen.WIDTH + 8, 18, 8, 6);
		
	}
	
	private int getArrowPosition() {
		
		ItemTradeData trade = this.getTrade();
		if(this.selection == -1)
		{
			if(trade.isSale())
				return this.screen.getGuiLeft() + 25;
			if(trade.isPurchase())
				return this.screen.getGuiLeft() + 81;
			else
				return -100;
		}
		else
		{
			if(this.selection >= 2 && !trade.isBarter())
				return -100;
			int horizSlot = this.selection;
			if(trade.isSale() || trade.isBarter())
				horizSlot += 2;
			int spacing = horizSlot % 4 >= 2 ? 20 : 0;
			return this.screen.getGuiLeft() + 16 + (18 * (horizSlot % 4)) + spacing;
		}
	}
	
	private void validateRenderables() {
		
		this.priceSelection.visible = this.selection < 0 && !this.getTrade().isBarter();
		if(this.priceSelection.visible)
			this.priceSelection.tick();
		this.itemEdit.visible = this.selection >= 0 && (this.getTrade().isBarter() ? this.selection < 4 : this.selection < 2);
		this.customNameInput.visible = this.selection >= 0 && this.selection < 2 && !this.getTrade().isPurchase();
		if(this.customNameInput.visible && !this.customNameInput.getValue().contentEquals(this.getTrade().getCustomName(this.selection)))
			this.commonTab.setCustomName(this.selection, this.customNameInput.getValue());
		this.buttonToggleTradeType.setMessage(new TranslatableComponent("gui.button.lightmanscurrency.tradedirection." + this.getTrade().getTradeType().name().toLowerCase()));
	}

	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
		this.tradeDisplay.renderTooltips(pose, mouseX, mouseY);
		
		if(this.selection >= 0)
			this.itemEdit.renderTooltips(this.screen, pose, mouseX, mouseY);
		
	}
	
	@Override
	public void receiveSelfMessage(CompoundTag message) {
		if(message.contains("TradeIndex"))
			this.commonTab.setTradeIndex(message.getInt("TradeIndex"));
		if(message.contains("StartingSlot"))
			this.selection = message.getInt("StartingSlot");
	}

	@Override
	public void onTradeButtonInputInteraction(ITrader trader, ITradeData trade, int index, int mouseButton) {
		if(trade instanceof ItemTradeData)
		{
			ItemTradeData t = (ItemTradeData)trade;
			if(t.isSale())
				this.changeSelection(-1);
			else if(t.isPurchase())
				this.changeSelection(index);
			else if(t.isBarter())
				this.changeSelection(index + 2);
		}
	}

	@Override
	public void onTradeButtonOutputInteraction(ITrader trader, ITradeData trade, int index, int mouseButton) {
		if(trade instanceof ItemTradeData)
		{
			ItemTradeData t = (ItemTradeData)trade;
			if(t.isSale() || t.isBarter())
				this.changeSelection(index);
			else if(t.isPurchase())
				this.changeSelection(-1);
		}
	}
	
	private void changeSelection(int newSelection) {
		this.selection = newSelection;
		if(this.selection == -1)
			this.priceSelection.setCoinValue(this.getTrade().getCost());
		if(this.selection >= 0 && this.selection < 2)
			this.customNameInput.setValue(this.commonTab.getTrade().getCustomName(this.selection));
		if(this.selection >= 0)
			this.itemEdit.refreshSearch();
	}

	@Override
	public void onTradeButtonInteraction(ITrader trader, ITradeData trade, int localMouseX, int localMouseY, int mouseButton) { }
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.tradeDisplay.onInteractionClick((int)mouseX, (int)mouseY, button, this);
		return false;
	}

	@Override
	public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomWidget(T button) {
		if(button instanceof AbstractWidget)
			this.screen.addRenderableTabWidget((AbstractWidget)button);
		return button;
	}

	@Override
	public int getWidth() { return this.screen.width; }

	@Override
	public Font getFont() { return this.font; }

	@Override
	public void OnCoinValueChanged(CoinValueInput input) { this.commonTab.setPrice(input.getCoinValue()); }

	@Override
	public ItemTradeData getTrade() { return this.commonTab.getTrade(); }

	@Override
	public boolean restrictItemEditItems() { return this.selection < 2; }

	@Override
	public void onItemClicked(ItemStack item) { this.commonTab.setSelectedItem(this.selection, item); }
	
	private void ToggleTradeType(Button button) {
		if(this.getTrade() != null)
			this.commonTab.setType(this.getTrade().getTradeType().next());
	}
	
}
