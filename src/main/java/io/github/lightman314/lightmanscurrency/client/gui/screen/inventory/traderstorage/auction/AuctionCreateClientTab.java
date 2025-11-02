package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.auction.AuctionCreateTab;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import io.github.lightman314.lightmanscurrency.network.message.persistentdata.CPacketCreatePersistentAuction;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AuctionCreateClientTab extends TraderStorageClientTab<AuctionCreateTab> {

    public static final long CLOSE_DELAY = TimeUtil.DURATION_SECOND * 5;

    public AuctionCreateClientTab(Object screen, AuctionCreateTab commonTab) { super(screen, commonTab); }

    @Override
    public IconData getIcon() { return IconUtil.ICON_PLUS; }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_AUCTION_CREATE.get(); }

    @Override
    public boolean blockInventoryClosing() { return LCAdminMode.isAdminPlayer(this.screen.getMenu().getPlayer()); }

    AuctionTradeData pendingAuction;

    TradeButton tradeDisplay;

    MoneyValueWidget priceSelect;
    EasyButton buttonTogglePriceMode;
    boolean startingBidMode = true;

    EasyButton buttonToggleOvertime;
    ScreenArea overtimeTextArea;

    EasyButton buttonSubmitAuction;

    boolean locked = false;
    long successTime = 0;

    EasyButton buttonSubmitPersistentAuction;
    EditBox persistentAuctionIDInput;

    TimeInputWidget timeInput;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        if(firstOpen)
        {
            this.pendingAuction = new AuctionTradeData(this.menu.getPlayer());
            this.locked = false;
            this.successTime = 0;
            this.startingBidMode = true;
            this.commonTab.getAuctionItems().addListener(c -> this.UpdateAuctionItems());
        }

        this.tradeDisplay = this.addChild(TradeButton.builder()
                .position(screenArea.pos.offset(15,5))
                .context(this.menu::getContext)
                .trade(this.pendingAuction)
                .build());

        this.priceSelect = this.addChild(MoneyValueWidget.builder()
                .position(screenArea.pos.offset(screenArea.width / 2 - MoneyValueWidget.WIDTH / 2, 34))
                .oldIfNotFirst(firstOpen,this.priceSelect)
                .valueHandler(this::onPriceChanged)
                .blockFreeInputs()
                .allowHandlerChange(() -> this.startingBidMode)
                .build());

        this.buttonTogglePriceMode = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(114,4))
                .width(screenArea.width - 119)
                .text(this::getBidModeText)
                .pressAction(this::TogglePriceTarget)
                .addon(EasyAddonHelper.activeCheck(this::isUnlocked))
                .build());

        this.buttonToggleOvertime = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(15,26))
                .pressAction(this::ToggleOvertime)
                .sprite(SpriteUtil.createCheckbox(() -> this.pendingAuction.isOvertimeAllowed()))
                .addon(EasyAddonHelper.activeCheck(this::isUnlocked))
                .build());
        this.overtimeTextArea = ScreenArea.of(26, 27, this.getFont().width(LCText.GUI_TRADER_AUCTION_OVERTIME.get()),10);


        //Duration Input
        long minDuration = Math.max(LCConfig.SERVER.auctionHouseDurationMin.get() * TimeUtil.DURATION_DAY, TimeUtil.DURATION_HOUR);
        this.timeInput = this.addChild(TimeInputWidget.builder()
                .position(screenArea.pos.offset(80,112))
                .unitRange(TimeUnit.HOUR,TimeUnit.DAY)
                .handler(this::updateDuration)
                .minDuration(minDuration)
                .maxDuration(Math.max(LCConfig.SERVER.auctionHouseDurationMax.get(), LCConfig.SERVER.auctionHouseDurationMin.get()) * TimeUtil.DURATION_DAY)
                .startTime(AuctionTradeData.GetDefaultDuration())
                .addon(EasyAddonHelper.activeCheck(this::isUnlocked))
                .build());

        //Submit Button
        this.buttonSubmitAuction = this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 41,120))
                .icon(IconUtil.ICON_PLUS)
                .pressAction(this::submitAuction)
                .addon(EasyAddonHelper.activeCheck(this::canSubmit))
                .addon(EasyAddonHelper.tooltips(this::getSubmitButtonTooltips,TooltipHelper.DEFAULT_TOOLTIP_WIDTH,true))
                .build());
        this.buttonSubmitAuction.active = false;

        this.buttonSubmitPersistentAuction = this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 20, -20))
                .pressAction(this::submitPersistentAuction)
                .icon(IconUtil.ICON_PERSISTENT_DATA)
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_PERSISTENT_CREATE_AUCTION))
                .addon(EasyAddonHelper.visibleCheck(this::isAdminMode))
                .build());
        this.buttonSubmitPersistentAuction.active = false;

        int idWidth = this.getFont().width(LCText.GUI_PERSISTENT_ID.get());
        this.persistentAuctionIDInput = this.addChild((new EditBox(this.getFont(), screenArea.x + idWidth + 2, screenArea.y - 20, screenArea.width - idWidth - 2, 18, EasyText.empty())));
        this.persistentAuctionIDInput.visible = LCAdminMode.isAdminPlayer(this.screen.getPlayer());

    }

    @Override
    public void closeAction() { this.commonTab.getAuctionItems().removeListener(c -> this.UpdateAuctionItems()); }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        gui.resetColor();
        for(EasySlot slot : this.commonTab.getSlots())
        {
            //Render Slot BG's
            gui.renderSlot(this.screen,slot);
        }

        //Item Slot label
        gui.drawString(LCText.GUI_TRADER_AUCTION_ITEMS.get(), TraderMenu.SLOT_OFFSET + 7, 112, 0x404040);

        //Overtime Label
        gui.drawString(LCText.GUI_TRADER_AUCTION_OVERTIME.get(), this.overtimeTextArea.pos, 0x404040);

        if(this.locked && this.successTime != 0)
            TextRenderUtil.drawCenteredText(gui, LCText.GUI_TRADER_AUCTION_CREATE_SUCCESS.getWithStyle(ChatFormatting.BOLD), this.screen.getXSize() / 2, 34, 0x404040);

        if(LCAdminMode.isAdminPlayer(this.screen.getPlayer()))
            gui.drawString(LCText.GUI_PERSISTENT_ID.get(), 0, -15, 0xFFFFFF);

    }

    @Override
    public void renderAfterWidgets(EasyGuiGraphics gui) {
        if(this.overtimeTextArea.offsetPosition(this.screen.getCorner()).isMouseInArea(gui.mousePos))
            gui.renderComponentTooltip(TooltipHelper.splitTooltips(LCText.TOOLTIP_TRADER_AUCTION_OVERTIME.get()));
    }

    private boolean isUnlocked() { return !this.locked; }

    private boolean canSubmit() {
        if(this.isUnlocked() && this.pendingAuction.isValid())
        {
            if(LCAdminMode.isAdminPlayer(this.menu.getPlayer()))
                return true;
            if(this.menu.getTrader() instanceof AuctionHouseTrader ah)
            {
                if(LCAdminMode.isAdminPlayer(this.menu.getPlayer()))
                    return true;
                MoneyValue price = LCConfig.SERVER.auctionHouseSubmitPrice.get();
                if(!price.isEmpty())
                {
                    IMoneyHandler handler = MoneyAPI.getApi().GetPlayersMoneyHandler(this.menu.getPlayer());
                    if(!handler.extractMoney(price,true).isEmpty())
                        return false;
                }
                int count = ah.getPlayerTradeCount(this.menu.getPlayer());
                return count < LCConfig.SERVER.auctionHousePlayerLimit.get();
            }
        }
        return false;
    }

    private boolean isAdminMode() { return LCAdminMode.isAdminPlayer(this.menu.getPlayer()); }

    private List<Component> getSubmitButtonTooltips()
    {
        List<Component> tooltip = new ArrayList<>();
        if(this.menu.getTrader() instanceof AuctionHouseTrader ah)
        {
            if(this.canSubmit())
                tooltip.add(LCText.BUTTON_TRADER_AUCTION_CREATE.getWithStyle(ChatFormatting.BOLD));
            MoneyValue price = LCConfig.SERVER.auctionHouseSubmitPrice.get();
            if(!price.isEmpty())
                tooltip.add(LCText.TOOLTIP_TRADER_AUCTION_PRICE.get(price.getText()));
            int feePercent = LCConfig.SERVER.auctionHouseFeePercentage.get();
            if(feePercent > 0)
                tooltip.add(LCText.TOOLTIP_TRADER_AUCTION_FEE_WARNING.get(feePercent));
            if(!LCAdminMode.isAdminPlayer(this.menu.getPlayer()))
            {
                int count = ah.getPlayerTradeCount(this.menu.getPlayer());
                int limit = LCConfig.SERVER.auctionHousePlayerLimit.get();
                if(count >= limit)
                    tooltip.addAll(LCText.TOOLTIP_TRADER_AUCTION_LIMIT_EXCEEDED.get(limit,count));
            }
        }
        return tooltip;
    }

    @Override
    public void tick() {
        if(this.locked && !this.priceSelect.isLocked())
            this.priceSelect.lock();
        if(this.locked && this.successTime != 0)
        {
            if(TimeUtil.compareTime(CLOSE_DELAY, this.successTime))
            {
                this.screen.ChangeTab(TraderStorageTab.TAB_TRADE_BASIC);
                return;
            }
        }

        if(LCAdminMode.isAdminPlayer(this.screen.getPlayer()))
        {
            this.buttonSubmitPersistentAuction.visible = this.persistentAuctionIDInput.visible = !this.locked;
            this.buttonSubmitPersistentAuction.active = this.pendingAuction.isValid();
        }
        else
            this.buttonSubmitPersistentAuction.visible = this.persistentAuctionIDInput.visible = false;

    }

    private void UpdateAuctionItems() {
        this.pendingAuction.setAuctionItems(this.commonTab.getAuctionItems());
    }

    private void onPriceChanged(MoneyValue newPrice) {
        if(this.startingBidMode)
            this.pendingAuction.setStartingBid(newPrice);
        else
            this.pendingAuction.setMinBidDifferent(newPrice);
    }

    private void TogglePriceTarget() {
        this.startingBidMode = !this.startingBidMode;
        if(this.startingBidMode)
            this.priceSelect.changeValue(this.pendingAuction.getLastBidAmount());
        else
            this.priceSelect.changeValue(this.pendingAuction.getMinBidDifference());
    }

    private void ToggleOvertime(EasyButton button) {
        this.pendingAuction.setOvertimeAllowed(!this.pendingAuction.isOvertimeAllowed());
    }

    private Component getBidModeText()
    {
        return this.startingBidMode ? LCText.BUTTON_TRADER_AUCTION_PRICE_MODE_STARTING_BID.get() : LCText.BUTTON_TRADER_AUCTION_PRICE_MODE_MIN_BID_SIZE.get();
    }

    private void updateDuration(TimeData newTime) {
        this.pendingAuction.setDuration(newTime.miliseconds);
    }

    private void submitAuction() {
        //LightmansCurrency.LogInfo("Sending Auction to the server!\n" + this.pendingAuction.getAsNBT().getAsString());
        this.commonTab.createAuction(this.pendingAuction);
        this.locked = true;
        for(EasySlot slot : this.commonTab.getSlots())
            slot.locked = true;
    }

    private void submitPersistentAuction(EasyButton button) {
        new CPacketCreatePersistentAuction(this.pendingAuction.getAsNBT(), this.persistentAuctionIDInput.getValue()).send();
    }

    @Override
    public void receiveServerMessage(LazyPacketData message) {
        if(message.contains("AuctionCreated"))
        {
            //LightmansCurrency.LogInfo("Received create response message from the server.\nAuction Created: " + message.getBoolean("AuctionCreated"));
            if(message.getBoolean("AuctionCreated"))
                this.successTime = TimeUtil.getCurrentTime();
            else
            {
                this.locked = false;
                for(EasySlot slot : this.commonTab.getSlots())
                    slot.locked = false;
            }
        }
    }

}