package io.github.lightman314.lightmanscurrency.integration.impactor.money;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.integration.impactor.LCImpactorCompat;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class ImpactorValueParser extends MoneyValueParser {

    public static final MoneyValueParser INSTANCE = new ImpactorValueParser();

    public static final DynamicCommandExceptionType INVALID_KEY = new DynamicCommandExceptionType(LCText.ARGUMENT_MONEY_VALUE_IMPACTOR_INVALID_KEY::get);
    public static final DynamicCommandExceptionType INVALID_CURRENCY = new DynamicCommandExceptionType(LCText.ARGUMENT_MONEY_VALUE_IMPACTOR_INVALID_KEY::get);
    public static final DynamicCommandExceptionType INVALID_AMOUNT = new DynamicCommandExceptionType(LCText.ARGUMENT_MONEY_VALUE_IMPACTOR_INVALID_AMOUNT::get);
    public static final SimpleCommandExceptionType UNKNOWN_ERROR = new SimpleCommandExceptionType(LCText.ARGUMENT_MONEY_VALUE_IMPACTOR_ERROR.get());

    private ImpactorValueParser() { super("impactor"); }

    @Override
    protected MoneyValue parseValueArgument(@Nonnull StringReader reader) throws CommandSyntaxException {
        try {
            String currencyType = readStringUntil(reader,';');
            try {
                Key type = Key.key(currencyType,':');
                Currency currency = LCImpactorCompat.getCurrency(type);
                if(currency == null)
                    throw INVALID_CURRENCY.createWithContext(reader,currencyType);
                String number = reader.getRemaining();
                try {
                    BigDecimal value = new BigDecimal(number);
                    int comparison = value.compareTo(BigDecimal.ZERO);
                    if(comparison == 0)
                        throw NO_VALUE_EXCEPTION.createWithContext(reader);
                    else if(comparison < 0)
                        throw INVALID_AMOUNT.createWithContext(reader,number);
                    return ImpactorMoneyValue.of(currency,value);
                } catch (NumberFormatException e) { throw INVALID_AMOUNT.createWithContext(reader,number); }
            } catch (InvalidKeyException e) { throw INVALID_KEY.createWithContext(reader,currencyType); }
        } catch (Exception e) {
            if(e instanceof CommandSyntaxException)
                throw e;
            throw UNKNOWN_ERROR.createWithContext(reader);
        }
    }

    @Override
    protected String writeValueArgument(@Nonnull MoneyValue value) {
        return "";
    }
}