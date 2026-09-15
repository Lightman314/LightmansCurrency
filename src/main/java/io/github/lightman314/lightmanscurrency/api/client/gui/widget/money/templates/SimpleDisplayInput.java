package io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.templates;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextSettings;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.DoubleParser;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;

public abstract class SimpleDisplayInput extends MoneyInputHandler {

    protected SimpleDisplayInput() {}

    private Component prefix = Component.empty();
    protected void setPrefix(String prefix) { this.prefix = Component.literal(prefix); }
    protected void setPrefix(Component prefix) { this.prefix = prefix; }
    private Component suffix = Component.empty();
    protected void setSuffix(String suffix) { this.suffix = Component.literal(suffix); }
    protected void setSuffix(Component suffix) { this.suffix = suffix; }

    private TextBoxWrapper<Double> input;
    private Component error = null;

    @Override
    public void initialize(ScreenArea area) {
        int prefixWidth = this.getFont().width(this.prefix);
        if(prefixWidth > 0)
            prefixWidth += 2;
        int suffixWidth = this.getFont().width(this.suffix);
        if(suffixWidth > 0)
            suffixWidth += 2;

        if(prefixWidth + suffixWidth > area.width + 40)
        {
            this.error = Component.empty().append(this.prefix).append("###").append(this.suffix);
            return;
        }

        this.input = this.addChild(TextBoxWrapper.doubleBuilder()
                .atPos(area.pos.offset(10 + prefixWidth,22))
                .ofWidth(MoneyValueWidget.WIDTH - 20 - prefixWidth - suffixWidth)
                .parser(DoubleParser.builder()
                        .min(0d)
                        .empty(0d)
                        .build())
                .withMaxLength(this.maxLength())
                .withHandler(this::onValueChanges)
                .withStartingString(this.getStartingText())
                .active(() -> !this.isFree() && !this.isLocked())
                .visible(this::isVisible)
                .build());

    }

    protected int maxLength() { return 32; }

    protected Component getErrorText() { return Component.literal("DISPLAY FORMAT TOO LONG"); }

    @Override
    protected void extractBG(FancyGuiExtractor gui,ScreenArea area,TextSettings settings) {
        if(this.input == null)
        {
            if(this.error != null)
            {
                int centerX = area.centerX();
                int centerY = area.centerY();
                gui.centeredText(this.getErrorText(),centerX,centerY - 10,0xFFFF0000,false);
                gui.centeredText(this.error,centerX,centerY,0xFFFF0000,false);
            }
            return;
        }
        if(this.isFree())
            this.input.setStringValue("");
        if(!this.prefix.getString().isEmpty())
            gui.text(this.prefix,10,28,settings.fancyTextColor(),false);
        if(!this.suffix.getString().isEmpty())
        {
            int width = gui.getFont().width(this.suffix);
            gui.text(this.suffix,area.width - 10 - width,28,settings.fancyTextColor(),false);
        }
    }

    private void onValueChanges(double newValueNumber)
    {
        if(this.isFree())
            return;
        this.changeValue(this.getValueFromInput(newValueNumber));
    }

    @Override
    public void onValueChanged(MoneyValue newValue) {
        String text;
        double valueNumber = 0d;
        if(newValue.getKey().equals(this.getKey()))
            valueNumber = this.getTextFromDisplay(newValue);
        if(valueNumber % 1d == 0d)
            text = String.valueOf(Math.round(valueNumber));
        else
            text = String.valueOf(valueNumber);
        if(this.input != null)
            this.input.setStringValue(text);
    }

    protected String getStartingText() {
        double value = this.getTextFromDisplay(this.currentValue());
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(this.getRelevantDecimals());
        return df.format(value);
    }

    protected abstract MoneyValue getValueFromInput(double inputValue);
    protected abstract double getTextFromDisplay(MoneyValue value);
    protected int getRelevantDecimals() { return 0; }

}
