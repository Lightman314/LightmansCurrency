package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.slot_machine.SlotMachineRenderer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.slot_machine.SlotMachineEntryDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.menus.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketOpenNetworkTerminal;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketOpenStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SlotMachineScreen extends EasyMenuScreen<SlotMachineMenu> implements IScrollable {

    public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "textures/gui/container/slot_machine.png");

    public static final int WIDTH = 176;
    public static final int HEIGHT = 222;

    public static final int ENTRY_ROWS = 2;
    public static final int ENTRY_COLUMNS = 2;
    public static final int ENTRIES_PER_PAGE = ENTRY_ROWS * ENTRY_COLUMNS;

    public static final Sprite SPRITE_INFO = Sprite.SimpleSprite(GUI_TEXTURE, WIDTH, 36, 10, 11);
    public static final Sprite SPRITE_INTERACT_1 = Sprite.SimpleSprite(GUI_TEXTURE, WIDTH, 0, 18, 18);
    public static final Sprite SPRITE_INTERACT_5 = Sprite.SimpleSprite(GUI_TEXTURE, WIDTH + 18, 0, 18, 18);
    public static final Sprite SPRITE_INTERACT_10 = Sprite.SimpleSprite(GUI_TEXTURE, WIDTH + 36, 0, 18, 18);

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

    public final LazyWidgetPositioner leftEdgePositioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_BOTTOMUP, -20, HEIGHT - 20, 20);

    public SlotMachineScreen(SlotMachineMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
        this.resize(WIDTH, HEIGHT);
    }

    private final SlotMachineRenderer slotRenderer = new SlotMachineRenderer(this);

    public final ScreenPosition SM_INFO_WIDGET = ScreenPosition.of(WIDTH - 16, 8);

    @Override
    protected void initialize(ScreenArea screenArea) {

        this.buttonOpenStorage = this.addChild(IconAndButtonUtil.storageButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + 118, this::OpenStorage, () -> this.menu.getTrader() != null && this.menu.getTrader().hasPermission(this.menu.player, Permissions.OPEN_STORAGE)));
        this.buttonCollectCoins = this.addChild(IconAndButtonUtil.collectCoinButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + 138, this::CollectCoins, this.menu.player, this.menu::getTrader));
        this.buttonOpenTerminal = this.addChild(IconAndButtonUtil.backToTerminalButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + this.imageHeight - 20, this::OpenTerminal, this::showTerminalButton));
        this.buttonOpenTerminal.visible = this.showTerminalButton();

        this.leftEdgePositioner.clear();
        this.leftEdgePositioner.addWidgets(this.buttonOpenTerminal, this.buttonOpenStorage, this.buttonCollectCoins);
        this.addChild(this.leftEdgePositioner);

        this.buttonInteract = this.addChild(new PlainButton(this.leftPos + 52, this.topPos + 107, b -> this.ExecuteTrade(1), SPRITE_INTERACT_1)
                .withAddons(EasyAddonHelper.tooltips(() -> this.getInteractionTooltip(1)),
                        EasyAddonHelper.activeCheck(this::allowInteraction),
                        EasyAddonHelper.visibleCheck(this::isInteractMode)));
        this.buttonInteract5 = this.addChild(new PlainButton(this.leftPos + 29, this.topPos + 107, b -> this.ExecuteTrade(5), SPRITE_INTERACT_5)
                .withAddons(EasyAddonHelper.tooltips(() -> this.getInteractionTooltip(5)),
                        EasyAddonHelper.activeCheck(this::allowInteraction),
                        EasyAddonHelper.visibleCheck(this::isInteractMode)));
        this.buttonInteract10 = this.addChild(new PlainButton(this.leftPos + 7, this.topPos + 107, b -> this.ExecuteTrade(10), SPRITE_INTERACT_10)
                .withAddons(EasyAddonHelper.tooltips(() -> this.getInteractionTooltip(10)),
                        EasyAddonHelper.activeCheck(this::allowInteraction),
                        EasyAddonHelper.visibleCheck(this::isInteractMode)));

        this.buttonInfo = this.addChild(new PlainButton(screenArea.pos.offset(SM_INFO_WIDGET), this::ToggleMode, SPRITE_INFO)
                .withAddons(EasyAddonHelper.tooltips(this::getInfoTooltip)));


        this.scrollListener = this.addChild(new ScrollListener(screenArea, this));
        this.scrollListener.active = this.isInfoMode();

        for(int y = 0; y < ENTRY_ROWS; ++y)
        {
            for(int x = 0; x < ENTRY_COLUMNS; x++)
            {
                int displayIndex = (y * ENTRY_COLUMNS) + x;
                this.addChild(new SlotMachineEntryDisplayWidget(screenArea.pos.offset(19 + (x * SlotMachineEntryDisplayWidget.WIDTH), 10 + (y * SlotMachineEntryDisplayWidget.HEIGHT)), this.menu::getTrader, () -> this.getTrueIndex(displayIndex))
                        .withAddons(EasyAddonHelper.visibleCheck(this::isInfoMode)));
            }
        }

        this.addChild(new ScrollBarWidget(screenArea.pos.offset(8, 10), 2 * SlotMachineEntryDisplayWidget.HEIGHT, this)
                .withAddons(EasyAddonHelper.visibleCheck(this::isInfoMode)));

        //Add renderer as child for auto-ticking
        this.addChild(this.slotRenderer);

    }

    private boolean isInteractMode() { return this.interactMode; }
    private boolean isInfoMode() { return !this.interactMode; }

    private void ToggleMode(EasyButton button) { if(this.menu.hasPendingReward()) return; this.interactMode = !this.interactMode; if(this.isInfoMode()) this.validateScroll(); }

    private boolean allowInteraction()
    {
        SlotMachineTraderData trader = this.menu.getTrader();
        return !this.menu.hasPendingReward() && trader != null && trader.hasStock() && trader.hasValidTrade();
    }

    private boolean showTerminalButton() {
        if(this.menu.getTrader() != null)
            return this.menu.getTrader().showOnTerminal();
        return false;
    }

    @Override
    protected void renderBG(@Nonnull EasyGuiGraphics gui) {

        gui.resetColor();

        //Main BG
        gui.renderNormalBackground(GUI_TEXTURE, this);

        //Coin info widget
        gui.blit(TraderScreen.GUI_TEXTURE, INFO_WIDGET_POSITION, 244, 0, 10, 10);

        //Slot Machine
        if(this.isInteractMode())
            this.slotRenderer.render(gui);

        //Labels
        gui.drawString(this.playerInventoryTitle, 8, this.getYSize() - 94, 0x404040);

        //Coin Value Text
        Component valueText = this.menu.getContext(null).getAvailableFunds().getRandomValueText();
        gui.drawString(valueText, 170 - gui.font.width(valueText) - 10, this.getYSize() - 94, 0x404040);

    }

    @Override
    protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
        gui.pushPose().TranslateToForeground();
        if(INFO_WIDGET_POSITION.offset(this).isMouseInArea(gui.mousePos, 10, 10))
            gui.renderComponentTooltip(this.menu.getContext().getAvailableFundsDescription());

    }

    @Nullable
    private List<Component> getInfoTooltip()
    {
        SlotMachineTraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            List<Component> info = trader.getSlotMachineInfo();
            if(this.isInfoMode())
                LCText.TOOLTIP_SLOT_MACHINE_TO_INTERACT.tooltip(info);
            else
                LCText.TOOLTIP_SLOT_MACHINE_TO_INFO.tooltip(info);
            return info;
        }
        return null;
    }

    @Override
    protected void renderAfterTooltips(@Nonnull EasyGuiGraphics gui) { gui.popPose(); }

    private List<Component> getInteractionTooltip(int count) {
        SlotMachineTraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            MoneyValue normalCost = trader.getPrice();
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
            return result;
        }
        return ImmutableList.of();
    }

    private void ExecuteTrade(int count) { this.menu.SendMessageToServer(this.builder().setInt("ExecuteTrade", count)); }

    private void OpenStorage(EasyButton button) {
        if(this.menu.getTrader() != null)
            new CPacketOpenStorage(this.menu.getTrader().getID()).send();
    }

    private void CollectCoins(EasyButton button) {
        if(this.menu.getTrader() != null)
            CPacketCollectCoins.sendToServer();
    }

    private void OpenTerminal(EasyButton button) {
        if(this.showTerminalButton())
            new CPacketOpenNetworkTerminal().send();
    }

    @Override
    protected void screenTick() {
        this.scrollListener.active = this.isInfoMode();
        if(this.isInfoMode())
            this.validateScroll();
    }

    @Nonnull
    private List<SlotMachineEntry> getEntries()
    {
        SlotMachineTraderData trader = this.menu.getTrader();
        if(trader != null)
            return trader.getValidEntries();
        return new ArrayList<>();
    }

    private int getTrueIndex(int displayIndex) { return displayIndex + (this.scroll * ENTRY_COLUMNS); }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(ENTRIES_PER_PAGE, ENTRY_COLUMNS, this.getEntries().size()); }

}
