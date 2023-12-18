package io.github.lightman314.lightmanscurrency.api.money.input.builtin;

import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.builtin.NumberDisplay;
import io.github.lightman314.lightmanscurrency.api.money.input.templates.SimpleDisplayInput;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.CoinCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class CoinDisplayInput extends SimpleDisplayInput {

    private final ChainData chain;

    public CoinDisplayInput(@Nonnull ChainData chain)
    {
        this.chain = chain;
        this.setPrefixAndPostfix();
    }

    @Nonnull
    @Override
    public MutableComponent inputName() { return this.chain.getDisplayName(); }

    @Nonnull
    @Override
    public String getUniqueName() { return MoneyValue.generateCustomUniqueName(CoinCurrencyType.TYPE, this.chain.chain); }

    private void setPrefixAndPostfix()
    {
        if(this.chain.getDisplayData() instanceof NumberDisplay nd)
        {
            String format = nd.getWordyFormat().getString();
            //Have to replace the {value} with a non-illegal character in order to split the string
            String[] splitFormat = format.replace("{value}", "`").split("`",2);
            if(splitFormat.length < 2)
            {
                //Determine which is the prefix, and which is the postfix
                if(format.startsWith("{value}"))
                    this.setPostfix(splitFormat[0]);
                else
                    this.setPrefix(splitFormat[0]);
            }
            else
            {
                this.setPrefix(splitFormat[0]);
                this.setPostfix(splitFormat[1]);
            }
        }
    }

    @Nonnull
    @Override
    protected MoneyValue getValueFromInput(double inputValue) { return this.chain.getDisplayData().parseDisplayInput(inputValue); }

    @Override
    protected double getTextFromDisplay(@Nonnull MoneyValue value) {
        double valueNumber = 0d;
        if(value instanceof CoinValue coinValue && coinValue.getChain().equals(this.chain.chain))
        {
            if(this.chain.getDisplayData() instanceof NumberDisplay nd)
                valueNumber = nd.getDisplayValue(coinValue.getCoreValue());
            else
                valueNumber = coinValue.getCoreValue();
        }
        return valueNumber;
    }
}
