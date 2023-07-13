package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.slot_machine.SlotMachineRenderer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class SlotMachineScreen extends EasyMenuScreen<SlotMachineMenu> {

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/slot_machine.png");

    public static final int WIDTH = 176;
    public static final int HEIGHT = 222;

    public static final Sprite SPRITE_INTERACT_1 = Sprite.SimpleSprite(GUI_TEXTURE, WIDTH, 0, 18, 18);
    public static final Sprite SPRITE_INTERACT_5 = Sprite.SimpleSprite(GUI_TEXTURE, WIDTH + 18, 0, 18, 18);
    public static final Sprite SPRITE_INTERACT_10 = Sprite.SimpleSprite(GUI_TEXTURE, WIDTH + 36, 0, 18, 18);

    IconButton buttonOpenStorage;
    IconButton buttonCollectCoins;

    IconButton buttonOpenTerminal;

    EasyButton buttonInteract;
    EasyButton buttonInteract5;
    EasyButton buttonInteract10;


    private final ScreenPosition INFO_WIDGET_POSITION = ScreenPosition.of(160, HEIGHT - 96);

    public final LazyWidgetPositioner leftEdgePositioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_BOTTOMUP, -20, HEIGHT - 20, 20);

    public SlotMachineScreen(SlotMachineMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
        this.resize(WIDTH, HEIGHT);
    }

    private final SlotMachineRenderer slotRenderer = new SlotMachineRenderer(this);

    public final ScreenPosition SM_INFO_WIDGET = ScreenPosition.of(154, 12);

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
                .withAddons(EasyAddonHelper.tooltips(() -> this.getInteractionTooltip(1))));
        this.buttonInteract5 = this.addChild(new PlainButton(this.leftPos + 29, this.topPos + 107, b -> this.ExecuteTrade(5), SPRITE_INTERACT_5)
                .withAddons(EasyAddonHelper.tooltips(() -> this.getInteractionTooltip(5))));
        this.buttonInteract10 = this.addChild(new PlainButton(this.leftPos + 7, this.topPos + 107, b -> this.ExecuteTrade(10), SPRITE_INTERACT_10)
                .withAddons(EasyAddonHelper.tooltips(() -> this.getInteractionTooltip(10))));

        this.buttonInteract.active = this.buttonInteract5.active = this.buttonInteract10.active = this.allowInteraction();

        //Add renderer as child for auto-ticking
        this.addChild(this.slotRenderer);

    }

    @Override
    protected void screenTick() {
        //Disable buttons if the trader has a pending reward or if the trader isn't properly set up.
        this.buttonInteract.active = this.buttonInteract5.active = this.buttonInteract10.active = this.allowInteraction();
    }

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

        //Info widget
        gui.blit(TraderScreen.GUI_TEXTURE, INFO_WIDGET_POSITION, 244, 0, 10, 10);

        //Slot Machine
        this.slotRenderer.render(gui);

        //Labels
        gui.drawString(this.playerInventoryTitle, 8, this.getYSize() - 94, 0x404040);

        //Moved to underneath the coin slots
        String valueText = MoneyUtil.getStringOfValue(this.menu.getContext(null).getAvailableFunds());
        gui.drawString(valueText, 170 - gui.font.width(valueText) - 10, this.getYSize() - 94, 0x404040);

    }

    @Override
    protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
        gui.pushPose().TranslateToForeground();
        if(INFO_WIDGET_POSITION.offset(this).isMouseInArea(gui.mousePos, 10, 10))
            gui.renderComponentTooltip(this.menu.getContext().getAvailableFundsDescription());

        SlotMachineTraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            if(SM_INFO_WIDGET.offset(this).isMouseInArea(gui.mousePos, 10, 10))
            {
                List<Component> tooltip = trader.getSlotMachineInfo();
                if(tooltip != null && tooltip.size() > 0)
                    gui.renderComponentTooltip(tooltip);
            }
        }

    }

    @Override
    protected void renderAfterTooltips(@Nonnull EasyGuiGraphics gui) { gui.popPose(); }

    private List<Component> getInteractionTooltip(int count) {
        SlotMachineTraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            if(count == 1)
                return ImmutableList.of(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.roll"), EasyText.translatable("tooltip.lightmanscurrency.slot_machine.roll.cost", trader.getPrice().getString()));
            else
                return ImmutableList.of(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.rolls", count), EasyText.translatable("tooltip.lightmanscurrency.slot_machine.rolls.cost", trader.getPrice().getString()));
        }
        return ImmutableList.of();
    }

    private void ExecuteTrade(int count) { this.menu.SendMessageToServer(LazyPacketData.builder().setInt("ExecuteTrade", count)); }

    private void OpenStorage(EasyButton button) {
        if(this.menu.getTrader() != null)
            LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.menu.getTrader().getID()));
    }

    private void CollectCoins(EasyButton button) {
        if(this.menu.getTrader() != null)
            LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
    }

    private void OpenTerminal(EasyButton button) {
        if(this.showTerminalButton())
        {
            this.menu.player.closeContainer();
            LightmansCurrency.PROXY.openTerminalScreen();
        }
    }

}
