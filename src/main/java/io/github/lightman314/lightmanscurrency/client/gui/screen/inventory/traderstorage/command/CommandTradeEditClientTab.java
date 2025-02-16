package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.command;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.command.CommandEditField;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.command.CommandTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.commands.tradedata.CommandTrade;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommandTradeEditClientTab extends TraderStorageClientTab<CommandTradeEditTab> implements IMouseListener, TradeInteractionHandler {

    public CommandTradeEditClientTab(Object screen, CommandTradeEditTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.Null(); }

    @Nullable
    @Override
    public Component getTooltip() { return EasyText.empty(); }

    @Override
    public boolean tabVisible() { return false; }

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public int getTradeRuleTradeIndex() { return this.commonTab.getTradeIndex(); }

    TradeButton tradeDisplay;
    MoneyValueWidget priceSelection;
    CommandEditField commandInput;

    boolean commandEdit = true;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        CommandTrade trade = this.commonTab.getTrade();

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
                .addon(EasyAddonHelper.visibleCheck(() -> !this.commandEdit))
                .build());

        this.commandInput = this.addChild(CommandEditField.builder()
                .position(screenArea.pos.offset(13,58))
                .width(screenArea.width - 26)
                .oldIfNotFirst(firstOpen,this.commandInput)
                .handler(this::onCommandChanged)
                .suggestions(5)
                .build());

        if(trade != null)
            this.commandInput.setCommand(trade.getCommand());

    }

    private void onValueChanged(MoneyValue value) { this.commonTab.setPrice(value); }

    private void onCommandChanged(String newCommand) {
        this.commonTab.setCommand(newCommand);
    }

    @Override
    public void tick() {
        this.commandInput.visible = this.commandEdit;
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {
        if(this.commandEdit)
            gui.drawString(LCText.GUI_TRADER_COMMAND_LABEL.get(),15,45,0x404040);
    }

    @Override
    protected void OpenMessage(@Nonnull LazyPacketData clientData) {
        if(clientData.contains("CommandEdit"))
            this.commandEdit = clientData.getBoolean("CommandEdit");
    }

    @Override
    public void HandleTradeInputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data, int inputIndex) {
        if(this.commandEdit)
        {
            this.commandEdit = false;
            this.tick();
        }
    }

    @Override
    public void HandleTradeOutputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data, int outputIndex) {
        if(!this.commandEdit)
        {
            this.commandEdit = true;
            this.tick();
        }
    }

    @Override
    public void HandleOtherTradeInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data) { }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        this.tradeDisplay.HandleInteractionClick((int)mouseX,(int)mouseY,button,this);
        return false;
    }
}
