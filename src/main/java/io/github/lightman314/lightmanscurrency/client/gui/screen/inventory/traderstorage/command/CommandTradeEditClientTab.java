package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.command;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.command.CommandEditField;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.command.CommandTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.commands.tradedata.CommandTrade;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandTradeEditClientTab extends TraderStorageClientTab<CommandTradeEditTab> implements IMouseListener, TradeInteractionHandler {

    public CommandTradeEditClientTab(Object screen, CommandTradeEditTab commonTab) { super(screen, commonTab); }

    @Override
    public IconData getIcon() { return IconData.Null(); }

    @Nullable
    @Override
    public Component getTooltip() { return EasyText.empty(); }

    @Override
    public boolean shouldRenderInventoryText() { return false; }

    @Override
    public boolean tabVisible() { return false; }

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public int getTradeRuleTradeIndex() { return this.commonTab.getTradeIndex(); }

    TradeButton tradeDisplay;
    MoneyValueWidget priceSelection;
    CommandEditField commandInput;
    PlainButton buttonToggleDetails;
    EditBox descriptionInput;
    EditBox tooltipInput;

    boolean commandEdit = true;
    static boolean displayDetails = false;

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

        this.buttonToggleDetails = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(109,45))
                .sprite(SpriteUtil.createCheckbox(() -> displayDetails))
                .pressAction(this::toggleDetailMode)
                .addon(EasyAddonHelper.visibleCheck(() -> this.commandEdit))
                .build());

        this.commandInput = this.addChild(CommandEditField.builder()
                .position(screenArea.pos.offset(13,55))
                .width(screenArea.width - 26)
                .oldIfNotFirst(firstOpen,this.commandInput)
                .handler(this::onCommandChanged)
                .suggestions(6)
                .showSuggestions(() -> !displayDetails)
                .startingValue(trade != null ? trade.getCommand() : "")
                .build());

        this.descriptionInput = this.addChild(new EditBox(this.getFont(),screenArea.x + 13,screenArea.y + 98,screenArea.width - 26,20,this.descriptionInput,EasyText.empty()));
        if(trade != null)
            this.descriptionInput.setValue(trade.getDescription());
        this.descriptionInput.setResponder(this::onDescriptionChanged);

        this.tooltipInput = this.addChild(new EditBox(this.getFont(),screenArea.x + 13,screenArea.y + 130,screenArea.width - 26,20,this.tooltipInput,EasyText.empty()));
        this.tooltipInput.setMaxLength(256);
        if(trade != null)
            this.tooltipInput.setValue(trade.getTooltip());
        this.tooltipInput.setResponder(this::onTooltipChanged);

    }

    private void onValueChanged(MoneyValue value) { this.commonTab.setPrice(value); }

    private void onCommandChanged(String newCommand) { this.commonTab.setCommand(newCommand); }

    private void onDescriptionChanged(String newDescription) { this.commonTab.setDescription(newDescription); }

    private void onTooltipChanged(String newTooltip) { this.commonTab.setTooltip(newTooltip); }

    @Override
    public void tick() {
        this.commandInput.visible = this.commandEdit;
        this.descriptionInput.visible = this.tooltipInput.visible = this.commandEdit && displayDetails;
    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {
        if(this.commandEdit)
        {
            gui.drawString(LCText.GUI_TRADER_COMMAND_LABEL.get(),15,46,0x404040);
            gui.drawString(LCText.GUI_TRADER_COMMAND_LABEL_DETAILS.get(),120, 46, 0x404040);
            if(displayDetails)
            {
                gui.drawString(LCText.GUI_TRADER_COMMAND_LABEL_DESCRIPTION.get(),15, 89, 0x404040);
                gui.drawString(LCText.GUI_TRADER_COMMAND_LABEL_TOOLTIP.get(),15, 121, 0x404040);
            }
        }
    }

    @Override
    protected void OpenMessage(LazyPacketData clientData) {
        if(clientData.contains("CommandEdit"))
            this.commandEdit = clientData.getBoolean("CommandEdit");
    }

    private void toggleDetailMode() { displayDetails = !displayDetails; }

    @Override
    public void HandleTradeInputInteraction(TraderData trader, TradeData trade, TradeInteractionData data, int inputIndex) {
        if(this.commandEdit)
        {
            this.commandEdit = false;
            this.tick();
        }
    }

    @Override
    public void HandleTradeOutputInteraction(TraderData trader, TradeData trade, TradeInteractionData data, int outputIndex) {
        if(!this.commandEdit)
        {
            this.commandEdit = true;
            this.tick();
        }
    }

    @Override
    public void HandleOtherTradeInteraction(TraderData trader, TradeData trade, TradeInteractionData data) { }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        this.tradeDisplay.HandleInteractionClick((int)mouseX,(int)mouseY,button,this);
        return false;
    }
}