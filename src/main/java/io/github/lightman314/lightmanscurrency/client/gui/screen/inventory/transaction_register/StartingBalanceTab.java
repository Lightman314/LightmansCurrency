package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.transaction_register;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TransactionRegisterScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;

public class StartingBalanceTab extends TransactionRegisterTab {

    public StartingBalanceTab(TransactionRegisterScreen screen) { super(screen); }

    private MoneyValueWidget valueInput;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        //Starting Value Input
        this.onValueInputInit(this.addChild(MoneyValueWidget.builder()
                .position(screenArea.pos.offset((screenArea.width / 2) - (MoneyValueWidget.WIDTH / 2),64))
                .startingValue(this.getData().startingValue)
                .allowHandlerChange(this::canChangeStartingValueHandler)
                .allowFreeInputs(false)
                .valueHandler(this::changeStartingValue)
                .textColor(0)
                .build()));

        this.createBackButton(screenArea);

    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {
        //Render Starting Balance Label
        TextRenderUtil.drawCenteredText(gui, LCText.GUI_TRANSACTION_REGISTER_LABEL_STARTING_BALANCE.get(),this.screen.getXSize() / 2,50,0);
    }

    private void changeStartingValue(MoneyValue newValue) { this.menu.ChangeStartingValue(newValue); }


}
