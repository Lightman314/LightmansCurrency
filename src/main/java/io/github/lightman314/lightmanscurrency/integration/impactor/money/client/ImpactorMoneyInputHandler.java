package io.github.lightman314.lightmanscurrency.integration.impactor.money.client;

import io.github.lightman314.lightmanscurrency.api.money.input.templates.SimpleDisplayInput;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.integration.impactor.LCImpactorCompat;
import io.github.lightman314.lightmanscurrency.integration.impactor.money.ImpactorCurrencyType;
import io.github.lightman314.lightmanscurrency.integration.impactor.money.ImpactorMoneyValue;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class ImpactorMoneyInputHandler extends SimpleDisplayInput {

    private final Currency currency;

    public ImpactorMoneyInputHandler(Currency currency) {
        this.currency = currency;
        //Get the prefix & postfix
        String format = this.currency.format(BigDecimal.ZERO).toString();
        String[] split = format.split("0",2);
        if(split.length == 1)
            this.setPrefix(split[0]);
        else
        {
            this.setPrefix(split[0]);
            this.setPostfix(split[1]);
        }
    }

    @Nonnull
    @Override
    protected MoneyValue getValueFromInput(double inputValue) { return ImpactorMoneyValue.of(this.currency, BigDecimal.valueOf(inputValue)); }

    @Override
    protected double getTextFromDisplay(@Nonnull MoneyValue value) {
        if(value instanceof ImpactorMoneyValue val)
            return val.getValue().doubleValue();
        return 0;
    }

    @Override
    protected int getRelevantDecimals() { return this.currency.decimals(); }

    @Nonnull
    @Override
    public MutableComponent inputName() { return LCImpactorCompat.convertComponent(this.currency.singular()); }

    @Nonnull
    @Override
    public String getUniqueName() { return MoneyValue.generateCustomUniqueName(ImpactorCurrencyType.TYPE,this.currency.key().toString()); }

}