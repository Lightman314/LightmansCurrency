package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.client.sprites.builtin.WidgetStateSprite;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.client.gui.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.sprites.builtin.NormalSprite;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.slot_machine.SlotMachineRenderer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.api.traders.trade.AlertData;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.slot_machine.SlotMachineEntryDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.menus.slot_machine.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.SlotMachineNode;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SlotMachineScreen extends EasyMenuScreen<SlotMachineMenu> implements IScrollable {

    public static final ResourceLocation GUI_TEXTURE = LightmansCurrency.id("textures/gui/container/slot_machine.png");

    public static final int WIDTH = 176;
    public static final int HEIGHT = 222;

    public static final int ENTRY_ROWS = 2;

    public static final FixedSizeSprite SPRITE_INFO = WidgetStateSprite.lazyHoverable(new NormalSprite(new SpriteSource(GUI_TEXTURE,WIDTH,36,10,11)),new NormalSprite(new SpriteSource(GUI_TEXTURE,WIDTH,47,10,11)));
    public static final FixedSizeSprite SPRITE_INTERACT_1 = WidgetStateSprite.lazyHoverable(new NormalSprite(new SpriteSource(GUI_TEXTURE,WIDTH,0,18,18)),new NormalSprite(new SpriteSource(GUI_TEXTURE,WIDTH,18,18,18)));
    public static final FixedSizeSprite SPRITE_INTERACT_5 = WidgetStateSprite.lazyHoverable(new NormalSprite(new SpriteSource(GUI_TEXTURE,WIDTH + 18,0,18,18)),new NormalSprite(new SpriteSource(GUI_TEXTURE,WIDTH + 18,18,18,18)));
    public static final FixedSizeSprite SPRITE_INTERACT_10 = WidgetStateSprite.lazyHoverable(new NormalSprite(new SpriteSource(GUI_TEXTURE,WIDTH + 36,0,18,18)),new NormalSprite(new SpriteSource(GUI_TEXTURE,WIDTH + 36,18,18,18)));

    private boolean interactMode = true;
    private int scroll = 0;

    IconButton buttonOpenStorage;
    IconButton buttonCollectCoins;

    IconButton buttonOpenTerminal;

    EasyButton buttonInteract;
    EasyButton buttonInteract5;
    EasyButton buttonInteract10;

    EasyButton buttonInfo;

    ScrollListener scrollListener;

    private final ScreenPosition INFO_WIDGET_POSITION = ScreenPosition.of(160, HEIGHT - 96);

    public final LazyWidgetPositioner rightEdgePositioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.createTopdown(), WIDTH, 0, 20);

    public SlotMachineScreen(SlotMachineMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
        this.resize(WIDTH, HEIGHT);
    }

    private final SlotMachineRenderer slotRenderer = new SlotMachineRenderer(this);

    public final ScreenPosition SM_INFO_WIDGET = ScreenPosition.of(WIDTH - 16, 8);

    @Override
    protected void initialize(ScreenArea screenArea) {

        this.buttonOpenStorage = this.addChild(IconButton.builder()
                .pressAction(this::OpenStorage)
                .icon(IconUtil.ICON_STORAGE)
                .addon(EasyAddonHelper.visibleCheck(() -> this.menu.getTrader() != null && this.menu.getTrader().hasPermission(this.menu.player, Permissions.OPEN_STORAGE)))
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_OPEN_STORAGE))
                .build());
        this.buttonCollectCoins = this.addChild(ButtonUtil.finishCollectCoinButton(IconButton.builder().pressAction(this::CollectCoins), this.menu.player, this.menu::getTrader));
        this.buttonOpenTerminal = this.addChild(IconButton.builder()
                        .pressAction(this::OpenTerminal)
                        .icon(IconUtil.ICON_BACK)
                        .addon(EasyAddonHelper.visibleCheck(this::showTerminalButton))
                        .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_NETWORK_BACK))
                        .build());

        this.rightEdgePositioner.clear();
        this.rightEdgePositioner.addWidgets(this.buttonOpenTerminal, this.buttonOpenStorage, this.buttonCollectCoins);
        this.addChild(this.rightEdgePositioner);

        this.buttonInteract = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(52,107))
                .pressAction(() -> this.ExecuteTrade(1))
                .sprite(SPRITE_INTERACT_1)
                .addon(EasyAddonHelper.tooltips(() -> this.getInteractionTooltip(1)))
                .addon(EasyAddonHelper.activeCheck(this::allowInteraction))
                .addon(EasyAddonHelper.visibleCheck(this::isInteractMode))
                .build());
        this.buttonInteract5 = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(29,107))
                .pressAction(() -> this.ExecuteTrade(5))
                .sprite(SPRITE_INTERACT_5)
                .addon(EasyAddonHelper.tooltips(() -> this.getInteractionTooltip(5)))
                .addon(EasyAddonHelper.activeCheck(this::allowInteraction))
                .addon(EasyAddonHelper.visibleCheck(this::isInteractMode))
                .build());
        this.buttonInteract10 = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(7,107))
                .pressAction(() -> this.ExecuteTrade(10))
                .sprite(SPRITE_INTERACT_10)
                .addon(EasyAddonHelper.tooltips(() -> this.getInteractionTooltip(10)))
                .addon(EasyAddonHelper.activeCheck(this::allowInteraction))
                .addon(EasyAddonHelper.visibleCheck(this::isInteractMode))
                .build());

        this.buttonInfo = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(SM_INFO_WIDGET))
                .pressAction(this::ToggleMode)
                .sprite(SPRITE_INFO)
                .addon(EasyAddonHelper.tooltips(this::getInfoTooltip))
                .build());


        this.scrollListener = this.addChild(ScrollListener.builder()
                .area(screenArea)
                .listener(this)
                .addon(EasyAddonHelper.activeCheck(this::isInfoMode))
                .build());

        for(int i = 0; i < ENTRY_ROWS; ++i)
        {
            int displayIndex = i;
            this.addChild(SlotMachineEntryDisplayWidget.builder()
                    .position(screenArea.pos.offset(19,20 + (i * SlotMachineEntryDisplayWidget.HEIGHT)))
                    .trader(this.menu::getTrader)
                    .index(() -> this.getTrueIndex(displayIndex))
                    .addon(EasyAddonHelper.visibleCheck(this::isInfoMode))
                    .build());
        }

        this.addChild(ScrollBarWidget.builder()
                .position(screenArea.pos.offset(8,20))
                .height(ENTRY_ROWS * SlotMachineEntryDisplayWidget.HEIGHT)
                .scrollable(this)
                .addon(EasyAddonHelper.visibleCheck(this::isInfoMode))
                .build());

        //Add renderer as child for auto-ticking
        this.addChild(this.slotRenderer);

    }

    private boolean isInteractMode() { return this.interactMode; }
    private boolean isInfoMode() { return !this.interactMode; }

    private void ToggleMode(EasyButton button) { if(this.menu.hasPendingReward()) return; this.interactMode = !this.interactMode; if(this.isInfoMode()) this.validateScroll(); }

    private boolean allowInteraction()
    {
        TraderData trader = this.menu.getTrader();
        SlotMachineNode node = this.menu.getNode(SlotMachineNode.TYPE);
        if(trader != null && node != null)
        {
            TradeEvent.PreTradeEvent event = trader.runPreTradeEvent(trader.getTrade(0),this.menu.getContext());
            return !this.menu.hasPendingReward() && node.getTradeStock(0) > 0 && trader.hasValidTrade() && !event.isCanceled();
        }
        return false;
    }

    private boolean showTerminalButton() {
        if(this.menu.getTrader() != null)
            return this.menu.getValidator().isThroughNetwork;
        return false;
    }

    @Override
    protected void renderBG(EasyGuiGraphics gui) {

        gui.resetColor();

        //Main BG
        gui.renderNormalBackground(GUI_TEXTURE, this);

        //Coin info widget
        gui.blit(TraderScreen.GUI_TEXTURE, INFO_WIDGET_POSITION, 244, 0, 10, 10);

        //Slot Machine
        if(this.isInteractMode())
            this.slotRenderer.render(gui);
        else
        {
            SlotMachineNode node = this.menu.getNode(SlotMachineNode.TYPE);
            if(node != null)
                TextRenderUtil.drawCenteredText(gui,LCText.GUI_TRADER_SLOT_MACHINE_FAIL_CHANCE.get(node.getFailOddsText()),this.getXSize() / 2, 6,0x404040);
        }

        //Labels
        gui.drawString(this.playerInventoryTitle, 8, this.getYSize() - 94, 0x404040);

        //Coin Value Text
        Component valueText = this.menu.getContext(null).getAvailableFunds().getRandomValueLine();
        gui.drawString(valueText, 170 - gui.font.width(valueText) - 10, this.getYSize() - 94, 0x404040);

    }

    @Override
    protected void renderAfterWidgets(EasyGuiGraphics gui) {
        if(INFO_WIDGET_POSITION.offset(this).isMouseInArea(gui.mousePos, 10, 10))
            gui.renderComponentTooltip(this.menu.getContext().getAvailableFundsDescription());
    }

    @Nullable
    private List<Component> getInfoTooltip()
    {
        SlotMachineNode node = this.menu.getNode(SlotMachineNode.TYPE);
        if(node != null)
        {
            List<Component> info = node.getSlotMachineInfo();
            if(this.isInfoMode())
                LCText.TOOLTIP_SLOT_MACHINE_TO_INTERACT.tooltip(info);
            else
                LCText.TOOLTIP_SLOT_MACHINE_TO_INFO.tooltip(info);
            return info;
        }
        return null;
    }

    private List<Component> getInteractionTooltip(int count) {
        TraderData trader = this.menu.getTrader();
        SlotMachineNode node = this.menu.getNode(SlotMachineNode.TYPE);
        if(trader != null && node != null)
        {
            MoneyValue normalCost = node.getPrice();
            MoneyValue currentCost = trader.runTradeCostEvent(trader.getTrade(0), this.menu.getContext()).getCostResult();
            Component costText = currentCost.isFree() ? LCText.TOOLTIP_SLOT_MACHINE_COST_FREE.get() : currentCost.getText();
            List<Component> result;
            if(count == 1)
                result = LCText.TOOLTIP_SLOT_MACHINE_ROLL_ONCE.get(count,costText);
            else
                result = LCText.TOOLTIP_SLOT_MACHINE_ROLL_MULTI.get(count,costText);
            //If the price is modified by a trade rule, display the "normal cost" as well just in case it changes in-between rolls
            if(!currentCost.equals(normalCost) && count > 1)
                result.add(LCText.TOOLTIP_SLOT_MACHINE_NORMAL_COST.get(normalCost.isFree() ? LCText.TOOLTIP_SLOT_MACHINE_COST_FREE.get() : normalCost.getText()));
            //Run pre-trade event to get alert tooltips
            TradeEvent.PreTradeEvent event = trader.runPreTradeEvent(trader.getTrade(0),this.menu.getContext());
            for(AlertData alert : event.getAlertInfo())
                result.add(alert.getFormattedMessage());
            return result;
        }
        return ImmutableList.of();
    }

    private void ExecuteTrade(int count) { this.menu.SendMessageToServer(this.builder().setInt("ExecuteTrade", count)); }

    private void OpenStorage() {
        if(this.menu.getTrader() != null)
            this.menu.openStorage();
    }

    private void CollectCoins() {
        if(this.menu.getTrader() != null)
            this.menu.collectMoney();
    }

    private void OpenTerminal() {
        if(this.showTerminalButton())
            this.menu.openTerminal();
    }

    @Override
    protected void screenTick() {
        if(this.isInfoMode())
            this.validateScroll();
    }

    private List<SlotMachineEntry> getEntries()
    {
        SlotMachineNode node = this.menu.getNode(SlotMachineNode.TYPE);
        if(node != null)
            return node.getValidEntries();
        return new ArrayList<>();
    }

    private int getTrueIndex(int displayIndex) { return displayIndex + this.scroll; }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(ENTRY_ROWS, this.getEntries().size()); }

}
