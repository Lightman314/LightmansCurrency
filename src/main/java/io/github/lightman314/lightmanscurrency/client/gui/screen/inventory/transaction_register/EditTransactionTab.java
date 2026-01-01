package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.transaction_register;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TransactionRegisterScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.items.data.register.TransactionData;
import io.github.lightman314.lightmanscurrency.common.items.data.register.TransactionType;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.network.chat.Component;

public class EditTransactionTab extends TransactionRegisterTab {

    private final int index;
    public EditTransactionTab(TransactionRegisterScreen screen, int index) { super(screen); this.index = index; }

    protected final TransactionData getTransaction() { return this.getTransaction(this.index); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        //Back Button
        this.createBackButton(screenArea);

        this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width,20))
                .icon(IconUtil.ICON_X)
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRANSACTION_REGISTER_DELETE_TRANSACTION))
                .pressAction(this::delete)
                .build());

        TransactionData data = this.getTransaction();

        //Description Input
        this.addChild(TextInputUtil.stringBuilder()
                .position(screenArea.pos.offset(20,30))
                .width(screenArea.width - 40)
                .startingValue(data.comment)
                .maxLength(32)
                .handler(this::changeComment)
                .build());

        //Type Input
        this.addChild(DropdownWidget.builder()
                .position(screenArea.pos.offset(40,70))
                .width(screenArea.width - 80)
                .enumOptions(LCText.GUI_TRANSACTION_REGISTER_TYPE_NAME,TransactionType.values())
                .selected(data.type.ordinal())
                .selectAction(this::changeType)
                .build());

        //Money Value Input
        this.onValueInputInit(this.addChild(MoneyValueWidget.builder()
                .position(screenArea.pos.offset((screenArea.width / 2) - (MoneyValueWidget.WIDTH / 2),110))
                .startingValue(data.optionalMoneyAmount().orElse(MoneyValue.empty()))
                .allowFreeInputs(false)
                .allowHandlerChange(() -> this.canChangeValueHandler(this.index))
                .addon(EasyAddonHelper.visibleCheck(this::showValueInput))
                .valueHandler(this::changeMoneyValue)
                .textColor(0)
                .build()));

        //Price Multiplier Input
        this.addChild(TextInputUtil.doubleBuilder()
                .position(screenArea.pos.offset(60,120))
                .width(80)
                .startingValue(data.optionalMultiplier().orElse(1d))
                .handler(this::changeNumber)
                .wrap()
                .addon(EasyAddonHelper.visibleCheck(this::showNumberInput))
                .build());

    }

    private boolean showValueInput() { return !this.showNumberInput(); }
    private boolean showNumberInput() { return this.getTransaction().type.needsNumber; }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        int centerX = this.screen.getXSize() / 2;

        //Render Labels
        TextRenderUtil.drawCenteredText(gui,LCText.GUI_TRANSACTION_REGISTER_LABEL_COMMENT.get(),centerX,20,0);
        TextRenderUtil.drawCenteredText(gui,LCText.GUI_TRANSACTION_REGISTER_LABEL_TYPE.get(),centerX,60,0);
        Component argumentLabel = LCText.GUI_TRANSACTION_REGISTER_TYPE_ARGUMENT_LABEL.get(this.getTransaction().type).get();
        TextRenderUtil.drawCenteredText(gui,argumentLabel,centerX,100,0);

    }

    @Override
    public void tick() {
        if(!this.hasTransaction(this.index))
            this.screen.closeTab();
    }

    private void changeComment(String newComment) { this.menu.ChangeTransactionComment(this.index,newComment); }

    private void changeType(int newTypeOrdinal)
    {
        TransactionType newType = EnumUtil.enumFromOrdinal(newTypeOrdinal,TransactionType.values(),TransactionType.ADD);
        this.menu.ChangeTransactionType(this.index,newType);
    }

    private void changeMoneyValue(MoneyValue newValue) { this.menu.ChangeTransactionValue(this.index,newValue); }

    private void changeNumber(double newNumber) { this.menu.ChangeTransactionNumber(this.index,newNumber); }

    private void delete() { this.menu.DeleteTransaction(this.index); this.screen.closeTab(); }

    @Override
    public boolean blockInventoryClosing() { return true; }

}