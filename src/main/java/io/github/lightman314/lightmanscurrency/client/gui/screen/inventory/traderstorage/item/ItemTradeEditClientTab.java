package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.item;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget.IItemEditListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.item.ItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemTradeEditClientTab extends TraderStorageClientTab<ItemTradeEditTab> implements TradeInteractionHandler, IItemEditListener, IMouseListener {

	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 71;
	private static final int COLUMNS = 10;
	private static final int ROWS = 3;
	
	public ItemTradeEditClientTab(Object screen, ItemTradeEditTab commonTab) { super(screen, commonTab); }
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconData.Null(); }

	@Override
	public MutableComponent getTooltip() { return EasyText.empty(); }

	@Override
	public boolean tabVisible() { return false; }
	
	@Override
	public boolean blockInventoryClosing() { return true; }
	
	@Override
	public int getTradeRuleTradeIndex() { return this.commonTab.getTradeIndex(); }

	TradeButton tradeDisplay;
	MoneyValueWidget priceSelection;
	EditBox customNameInput;
	
	ItemEditWidget itemEdit = null;

	private int selection = -1;

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {


		ItemTradeData trade = this.getTrade();
		
		this.tradeDisplay = this.addChild(TradeButton.builder()
				.position(screenArea.pos.offset(10,18))
				.context(this.menu::getContext)
				.trade(this.commonTab::getTrade)
				.build());
		this.priceSelection = this.addChild(MoneyValueWidget.builder()
				.position(screenArea.pos.offset(screenArea.width / 2 - MoneyValueWidget.WIDTH / 2, 40))
				.oldIfNotFirst(firstOpen,this.priceSelection)
				.startingValue(trade)
				.valueHandler(this::onValueChanged)
				.build());
		
		this.itemEdit = this.addChild(ItemEditWidget.builder()
				.position(screenArea.pos.offset(X_OFFSET,Y_OFFSET))
				.columns(COLUMNS)
				.rows(ROWS)
				.oldWidget(this.itemEdit)
				.handler(this)
				.build());
		
		int labelWidth = this.getFont().width(LCText.GUI_NAME.get());
		this.customNameInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 15 + labelWidth, screenArea.y + 38, screenArea.width - 28 - labelWidth, 18, EasyText.empty()));
		if(this.selection >= 0 && this.selection < 2 && trade != null)
			this.customNameInput.setValue(trade.getCustomName(this.selection));

		this.addChild(DropdownWidget.builder()
				.position(screenArea.pos.offset(113,18))
				.width(80)
				.option(LCText.GUI_TRADE_DIRECTION.get(TradeDirection.SALE))
				.option(LCText.GUI_TRADE_DIRECTION.get(TradeDirection.PURCHASE))
				.option(LCText.GUI_TRADE_DIRECTION.get(TradeDirection.BARTER))
				.selected(trade == null ? 0 : Math.max(0,trade.getTradeDirection().index))
				.selectAction(this::ChangeTradeType)
				.build());

		this.addChild(PlainButton.builder()
				.position(screenArea.pos.offset(113,4))
				.pressAction(this::ToggleNBTEnforcement)
				.sprite(IconAndButtonUtil.SPRITE_CHECK(this::getEnforceNBTState))
				.addon(EasyAddonHelper.visibleCheck(this::isNBTButtonVisible))
				.build());

	}

	private boolean getEnforceNBTState() {
		ItemTradeData trade = this.getTrade();
		if(trade != null)
			return trade.getEnforceNBT(this.selection);
		return true;
	}
	
	@Override
	public void closeAction() { this.selection = -1; }

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		if(this.getTrade() == null)
			return;
		
		this.validateRenderables();
		
		//Render a down arrow over the selected position
		gui.resetColor();

		gui.blit(TraderScreen.GUI_TEXTURE, this.getArrowPosition(), 10, TraderScreen.WIDTH + 8, 18, 8, 6);
		
		if(this.customNameInput.visible)
			gui.drawString(LCText.GUI_NAME.get(), 13, 42, 0x404040);

		if(this.isNBTButtonVisible())
			gui.drawString(LCText.GUI_TRADER_ITEM_ENFORCE_NBT.get(), 124, 5, 0x404040);

	}
	
	private int getArrowPosition() {
		
		ItemTradeData trade = this.getTrade();
		if(this.selection == -1)
		{
			if(trade.isSale())
				return 25;
			if(trade.isPurchase())
				return 81;
			else
				return -1000;
		}
		else
		{
			if(this.selection >= 2 && !trade.isBarter())
				return -1000;
			int horizSlot = this.selection;
			if(trade.isSale() || trade.isBarter())
				horizSlot += 2;
			int spacing = horizSlot % 4 >= 2 ? 20 : 0;
			return 16 + (18 * (horizSlot % 4)) + spacing;
		}
	}
	
	private void validateRenderables() {
		ItemTradeData trade = this.getTrade();
		if(trade == null)
		{
			this.priceSelection.visible = this.itemEdit.visible = this.customNameInput.visible = false;
			return;
		}
		this.priceSelection.visible = this.selection < 0 && !this.getTrade().isBarter();
		this.itemEdit.visible = (this.getTrade().isBarter() && this.selection >=2) || (this.getTrade().isPurchase() && this.selection >= 0);
		this.customNameInput.visible = this.selection >= 0 && this.selection < 2 && !this.getTrade().isPurchase();
		if(this.customNameInput.visible && !this.customNameInput.getValue().contentEquals(this.getTrade().getCustomName(this.selection)))
			this.commonTab.setCustomName(this.selection, this.customNameInput.getValue());
	}

	private boolean isNBTButtonVisible() {
		ItemTradeData trade = this.getTrade();
		if(trade == null)
			return false;
		else
			return this.selection >= 0 && !trade.alwaysEnforcesNBT(this.selection);
	}

	@Override
	public void OpenMessage(@Nonnull LazyPacketData message) {
		if(message.contains("StartingSlot"))
			this.selection = message.getInt("StartingSlot");
	}

	@Override
	public void HandleTradeInputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data, int index) {
		if(trade instanceof ItemTradeData t)
		{
			ItemStack heldItem = this.menu.getHeldItem();
			if(t.isSale())
				this.changeSelection(-1);
			else if(t.isPurchase())
			{
				if(this.selection != index && heldItem.isEmpty())
					this.changeSelection(index);
				else
					this.commonTab.defaultInteraction(index, heldItem, data.mouseButton());
			}
			else if(t.isBarter())
			{
				if(this.selection != (index + 2) && heldItem.isEmpty())
					this.changeSelection(index + 2);
				else
					this.commonTab.defaultInteraction(index + 2, heldItem, data.mouseButton());
			}
				
		}
	}

	@Override
	public void HandleTradeOutputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data,int index) {
		if(trade instanceof ItemTradeData t)
		{
			ItemStack heldItem = this.menu.getHeldItem();
			if(t.isSale() || t.isBarter())
			{
				if(this.selection != index && heldItem.isEmpty())
					this.changeSelection(index);
				else
					this.commonTab.defaultInteraction(index, heldItem, data.mouseButton());
			}	
			else if(t.isPurchase())
				this.changeSelection(-1);
		}
	}
	
	private void changeSelection(int newSelection) {
		this.selection = newSelection;
		if(this.selection == -1)
			this.priceSelection.changeValue(this.getTrade().getCost());
		if(this.selection >= 0 && this.selection < 2)
		{
			this.itemEdit.refreshSearch();
			this.customNameInput.setValue(this.commonTab.getTrade().getCustomName(this.selection));
		}
		if(this.selection >= 2)
			this.itemEdit.refreshSearch();
	}

	@Override
	public void HandleOtherTradeInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data) { }
	
	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		this.tradeDisplay.HandleInteractionClick((int)mouseX, (int)mouseY, button, this);
		return false;
	}

	public void onValueChanged(MoneyValue value) { this.commonTab.setPrice(value); }

	@Override
	public ItemTradeData getTrade() { return this.commonTab.getTrade(); }

	@Override
	public boolean restrictItemEditItems() { return this.selection < 2; }

	@Override
	public void onItemClicked(ItemStack item) { this.commonTab.setSelectedItem(this.selection, item); }
	
	private void ChangeTradeType(int index) {
		if(this.getTrade() != null)
		{
			this.commonTab.setType(TradeDirection.fromIndex(index));
			this.itemEdit.refreshSearch();
		}
	}

	private void ToggleNBTEnforcement(EasyButton button) {
		if(this.getTrade() != null)
			this.commonTab.setNBTEnforced(this.selection, !this.getTrade().getEnforceNBT(this.selection));
	}
	
}
