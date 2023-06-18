package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.slot_machine.SlotMachineRenderer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.IScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SlotMachineScreen extends AbstractContainerScreen<SlotMachineMenu> implements IScreen {

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/slot_machine.png");

    public static final int WIDTH = 176;
    public static final int HEIGHT = 222;

    public final Font getFont() { return this.font; }

    private final List<Runnable> tickListeners = new ArrayList<>();

    IconButton buttonOpenStorage;
    IconButton buttonCollectCoins;

    IconButton buttonOpenTerminal;

    Button buttonInteract;
    Button buttonInteract5;
    Button buttonInteract10;


    private final ScreenPosition INFO_WIDGET_POSITION = ScreenPosition.of(160, HEIGHT - 96);

    public final LazyWidgetPositioner leftEdgePositioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_BOTTOMUP, -20, HEIGHT - 20, 20);

    public SlotMachineScreen(SlotMachineMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); this.imageWidth = WIDTH; this.imageHeight = HEIGHT; }

    private final SlotMachineRenderer slotRenderer = new SlotMachineRenderer(this);

    public final ScreenPosition SM_INFO_WIDGET = ScreenPosition.of(154, 12);

    @Override
    protected void init() {

        super.init();

        this.buttonOpenStorage = this.addRenderableWidget(IconAndButtonUtil.storageButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + 118, this::OpenStorage, () -> this.menu.getTrader() != null && this.menu.getTrader().hasPermission(this.menu.player, Permissions.OPEN_STORAGE)));
        this.buttonCollectCoins = this.addRenderableWidget(IconAndButtonUtil.collectCoinButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + 138, this::CollectCoins, this.menu.player, this.menu::getTrader));
        this.buttonOpenTerminal = this.addRenderableWidget(IconAndButtonUtil.backToTerminalButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + this.imageHeight - 20, this::OpenTerminal, this::showTerminalButton));
        this.buttonOpenTerminal.visible = this.showTerminalButton();

        this.leftEdgePositioner.clear();
        this.leftEdgePositioner.addWidgets(this.buttonOpenTerminal, this.buttonOpenStorage, this.buttonCollectCoins);
        this.leftEdgePositioner.reposition();

        this.buttonInteract = this.addRenderableWidget(new PlainButton(this.leftPos + 52, this.topPos + 107, 18, 18, b -> this.ExecuteTrade(1), GUI_TEXTURE, this.imageWidth, 0));
        this.buttonInteract5 = this.addRenderableWidget(new PlainButton(this.leftPos + 29, this.topPos + 107, 18, 18, b -> this.ExecuteTrade(5), GUI_TEXTURE, this.imageWidth + 18, 0));
        this.buttonInteract10 = this.addRenderableWidget(new PlainButton(this.leftPos + 7, this.topPos + 107, 18, 18, b -> this.ExecuteTrade(10), GUI_TEXTURE, this.imageWidth + 36, 0));

        this.buttonInteract.active = this.buttonInteract5.active = this.buttonInteract10.active = this.allowInteraction();

    }

    @Override
    public void addTickListener(Runnable r) {
        if(!this.tickListeners.contains(r))
            this.tickListeners.add(r);
    }

    @Override
    protected void containerTick() {
        this.slotRenderer.tick();
        for(Runnable r : this.tickListeners)
            r.run();
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
    protected void renderBg(@Nonnull PoseStack pose, float partialTicks, int mouseX, int mouseY) {

        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        //Main BG
        blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        //Info widget
        RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
        blit(pose, this.leftPos + INFO_WIDGET_POSITION.x, this.topPos + INFO_WIDGET_POSITION.y, 244, 0, 10, 10);

        //Slot Machine
        this.slotRenderer.render(pose, partialTicks);

    }

    @Override
    protected void renderLabels(@Nonnull PoseStack pose, int mouseX, int mouseY) {
        this.font.draw(pose, this.playerInventoryTitle, 8, this.imageHeight - 94, 0x404040);

        //Moved to underneath the coin slots
        String valueText = MoneyUtil.getStringOfValue(this.menu.getContext(null).getAvailableFunds());
        font.draw(pose, valueText, 170 - this.font.width(valueText) - 10, this.imageHeight - 94, 0x404040);

    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTicks);
        pose.pushPose();
        ItemRenderUtil.TranslateToForeground(pose);
        this.renderTooltip(pose, mouseX, mouseY);

        if(INFO_WIDGET_POSITION.offset(this).isMouseInArea(mouseX, mouseY, 10, 10))
            this.renderComponentTooltip(pose, this.menu.getContext().getAvailableFundsDescription(), mouseX, mouseY);

        SlotMachineTraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            if(SM_INFO_WIDGET.offset(this).isMouseInArea(mouseX, mouseY, 10, 10))
            {
                List<Component> tooltip = trader.getSlotMachineInfo();
                if(tooltip != null && tooltip.size() > 0)
                    this.renderComponentTooltip(pose, tooltip, mouseX, mouseY);
            }
            if(this.buttonInteract.isMouseOver(mouseX, mouseY))
                this.renderComponentTooltip(pose, ImmutableList.of(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.roll"), EasyText.translatable("tooltip.lightmanscurrency.slot_machine.roll.cost", trader.getPrice().getString())), mouseX, mouseY);
            else if(this.buttonInteract5.isMouseOver(mouseX, mouseY))
                this.renderComponentTooltip(pose, ImmutableList.of(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.rolls", 5), EasyText.translatable("tooltip.lightmanscurrency.slot_machine.rolls.cost", trader.getPrice().getString())), mouseX, mouseY);
            else if(this.buttonInteract10.isMouseOver(mouseX, mouseY))
                this.renderComponentTooltip(pose, ImmutableList.of(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.rolls", 10), EasyText.translatable("tooltip.lightmanscurrency.slot_machine.rolls.cost", trader.getPrice().getString())), mouseX, mouseY);
        }

        ITooltipSource.renderTooltips(this, pose, mouseX, mouseY);

        pose.popPose();

    }



    private void ExecuteTrade(int count) { this.menu.SendMessageToServer(LazyPacketData.builder().setInt("ExecuteTrade", count)); }

    private void OpenStorage(Button button) {
        if(this.menu.getTrader() != null)
            LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.menu.getTrader().getID()));
    }

    private void CollectCoins(Button button) {
        if(this.menu.getTrader() != null)
            LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
    }

    private void OpenTerminal(Button button) {
        if(this.showTerminalButton())
        {
            this.menu.player.closeContainer();
            LightmansCurrency.PROXY.openTerminalScreen();
        }
    }

}
