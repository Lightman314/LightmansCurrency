package io.github.lightman314.lightmanscurrency.api.money.input.templates;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public abstract class SimpleDisplayInput extends MoneyInputHandler {

    protected SimpleDisplayInput() { }

    private Component prefix = EasyText.empty();
    protected void setPrefix(@Nonnull String prefix) { this.prefix = EasyText.literal(prefix); }
    protected void setPrefix(@Nonnull Component prefix) { this.prefix = prefix; }
    private Component postfix = EasyText.empty();
    protected void setPostfix(@Nonnull String postfix) { this.postfix = EasyText.literal(postfix); }
    protected void setPostfix(@Nonnull Component postfix) { this.postfix = postfix; }

    private EditBox input;
    private Component error = null;

    @Override
    public void initialize(@Nonnull ScreenArea widgetArea) {
        int prefixWidth = this.getFont().width(this.prefix);
        if(prefixWidth > 0)
            prefixWidth += 2;
        int postfixWidth = this.getFont().width(this.postfix);
        if(postfixWidth > 0)
            postfixWidth += 2;
        if(prefixWidth + postfixWidth > widgetArea.width + 40)
        {
            LightmansCurrency.LogError("Prefix & Postfix are too long. Cannot setup display!\nPrefix: " + this.prefix.getString() + "\nPostfix: " + this.postfix.getString());
            this.error = EasyText.empty().append(this.prefix).append(EasyText.literal("###")).append(this.postfix);
            return;
        }
        this.input = this.addChild(new EditBox(this.getFont(), widgetArea.x + 10 + prefixWidth, widgetArea.y + 22, MoneyValueWidget.WIDTH - 20 - prefixWidth - postfixWidth, 20, EasyText.empty()));
        this.input.setResponder(this::onValueTextChanges);
        this.input.setMaxLength(this.maxLength());
        this.input.setFilter(TextInputUtil::isPositiveDouble);
        this.onValueChanged(this.currentValue());
    }

    protected int maxLength() { return 32; }

    @Override
    public void renderTick() {
        if(this.input == null)
            return;
        this.input.visible = this.isVisible();
        this.input.active = !this.isFree() && !this.isLocked();
    }

    protected Component getErrorText() { return EasyText.literal("DISPLAY FORMAT TOO LONG"); }

    @Override
    protected void renderBG(@Nonnull ScreenArea widgetArea, @Nonnull EasyGuiGraphics gui) {
        super.renderBG(widgetArea, gui);
        if(this.input == null)
        {
            if(this.error != null)
            {
                TextRenderUtil.drawCenteredText(gui, this.getErrorText(), widgetArea.width / 2, (widgetArea.height / 2) - 10, 0xFF0000);
                TextRenderUtil.drawCenteredText(gui, this.error, widgetArea.width / 2, (widgetArea.height / 2), 0xFF0000);
            }
            return;
        }
        if(this.isFree())
            this.input.setValue("");
        if(!this.prefix.getString().isEmpty())
            gui.drawShadowed(this.prefix, 10, 28, 0xFFFFFF);
        if(!this.postfix.getString().isEmpty())
        {
            int width = gui.font.width(this.postfix);
            gui.drawShadowed(this.postfix, widgetArea.width - 10 - width, 28, 0xFFFFFF);
        }
    }

    private void onValueTextChanges(@Nonnull String newText)
    {
        if(this.isFree())
            return;
        final double valueNumber = TextInputUtil.getDoubleValue(this.input);
        MoneyValue newValue = this.getValueFromInput(valueNumber);
        this.changeValue(newValue);
    }

    @Override
    public void onValueChanged(@Nonnull MoneyValue newValue) {
        String text;
        double valueNumber = 0d;
        if(newValue.getUniqueName().equals(this.getUniqueName()))
            valueNumber = this.getTextFromDisplay(newValue);
        if(valueNumber % 1d == 0d)
            text = String.valueOf((long)valueNumber);
        else
            text = String.valueOf(valueNumber);
        if(this.input != null)
            this.input.setValue(text);
    }

    @Nonnull
    protected abstract MoneyValue getValueFromInput(double inputValue);
    protected abstract double getTextFromDisplay(@Nonnull MoneyValue value);

}
