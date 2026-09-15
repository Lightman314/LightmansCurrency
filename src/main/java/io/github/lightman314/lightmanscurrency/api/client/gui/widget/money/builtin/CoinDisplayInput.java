package io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.builtin;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownOption;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.templates.SimpleDisplayInput;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.coins.display.builtin.NumberDisplay;
import io.github.lightman314.lightmanscurrency.api.coins.value.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;

public class CoinDisplayInput extends SimpleDisplayInput {

    private final ChainData chain;
    private final MoneyKey key;
    public CoinDisplayInput(ChainData chain) {
        this.chain = chain;
        this.key = MoneyKey.create(CoinValue.TYPE,this.chain.chain);
        this.setPrefixAndSuffix();
    }

    @Override
    public DropdownOption inputOption() { return new DropdownOption(this.chain.getDisplayName(),this.chain.getDisplaySprite()); }

    @Override
    public MoneyKey getKey() { return this.key; }

    private void setPrefixAndSuffix() {
        if(this.chain.getDisplayData() instanceof NumberDisplay nd) {
            Pair<String,String> format = nd.getSplitWordyFormat();
            this.setPrefix(format.getFirst());
            this.setSuffix(format.getSecond());
        }
    }

    @Override
    protected MoneyValue getValueFromInput(double inputValue) { return this.chain.getDisplayData().parseDisplayInput(inputValue); }

    @Override
    protected double getTextFromDisplay(MoneyValue value) {
        double valueNumber = 0d;
        if(value instanceof CoinValue coinValue && coinValue.isChain(this.chain))
        {
            if(this.chain.getDisplayData() instanceof NumberDisplay nd)
                valueNumber = nd.getDisplayValue(value.getInternalValue());
            else
                valueNumber = value.getInternalValue();
        }
        return valueNumber;
    }

    @Override
    protected int getRelevantDecimals() {
        if(this.chain.getDisplayData() instanceof NumberDisplay nd)
            return nd.getMaxDecimal();
        return 0;
    }
}
