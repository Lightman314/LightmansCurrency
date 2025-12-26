package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.common;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DiscountCodeTab extends TraderClientTab {

    public DiscountCodeTab(ITraderScreen screen) { super(screen); }

    EditBox codeInput;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        //Input Field
        this.codeInput = this.addChild(TextInputUtil.stringBuilder()
                .position(screenArea.pos.offset(30,20))
                .width(screenArea.width - 60)
                .filter(TicketStationRecipe.CODE_INPUT_PREDICATE)
                .build());

        //Add button
        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(30,40))
                .width((screenArea.width - 60) / 2)
                .text(LCText.BUTTON_ADD)
                .pressAction(this::addCode)
                .addon(EasyAddonHelper.activeCheck(this::isInputValid))
                .build());
        //Remove Button
        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(screenArea.width / 2,40))
                .width((screenArea.width - 60) / 2)
                .text(LCText.BUTTON_REMOVE)
                .pressAction(this::removeCode)
                .addon(EasyAddonHelper.activeCheck(this::isInputValid))
                .build());

        //Display
        this.addChild(ScrollTextDisplay.builder()
                .position(screenArea.pos.offset(30,60))
                .size(screenArea.width - 60,58)
                .text(this::getCurrentCodes)
                .build());

    }

    private List<MutableComponent> getCurrentCodes() { return this.menu.getTypedDiscountCodes().stream().map(EasyText::literal).toList(); }

    protected boolean isInputValid() { return this.codeInput != null && !this.codeInput.getValue().isBlank(); }

    protected void addCode() {
        this.menu.submitDiscountCode(this.codeInput.getValue());
        this.codeInput.setValue("");
    }

    protected void removeCode() {
        this.menu.removeDiscountCode(this.codeInput.getValue());
        this.codeInput.setValue("");
    }

    protected void clearCodes() { this.menu.clearDiscountCodes(); }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

    }

    @Override
    public boolean blockInventoryClosing() { return true; }

}
