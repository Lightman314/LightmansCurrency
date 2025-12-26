package io.github.lightman314.lightmanscurrency.common.items.data.register;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.money.value.FlexibleMoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public enum TransactionType {
    ADD(TransactionHelpfulness.HELPFUL, FlexibleMoneyValue::addValue),
    SUBTRACT(TransactionHelpfulness.HARMFUL, FlexibleMoneyValue::subtractValue),
    MULTIPLY((bal,arg) -> {
        if(bal.isEmpty())
            return TransactionHelpfulness.NEUTRAL;
        AtomicReference<TransactionHelpfulness> result = new AtomicReference<>(TransactionHelpfulness.NEUTRAL);
        arg.ifRight(mult -> {
            mult = mult - 1d;
            if(bal.negative)
                mult *= -1d;
            if(mult > 0d)
                result.set(TransactionHelpfulness.HELPFUL);
            else if(mult < 0d)
                result.set(TransactionHelpfulness.HARMFUL);
        });
        return result.get();
    }, FlexibleMoneyValue::multiplyValue);

    public static final Codec<TransactionType> CODEC = EnumUtil.buildCodec(TransactionType.class,"Transaction Type");

    public final boolean needsNumber;
    private final BiFunction<FlexibleMoneyValue,Either<MoneyValue,Double>,TransactionHelpfulness> helpfulness;
    private final BiFunction<FlexibleMoneyValue, Either<MoneyValue,Double>, FlexibleMoneyValue> calculation;

    public boolean isValidArgument(Either<MoneyValue,Double> argument)
    {
        AtomicBoolean result = new AtomicBoolean(false);
        argument.ifLeft(v -> result.set(!this.needsNumber))
                .ifRight(m -> result.set(this.needsNumber));
        return result.get();
    }
    public FlexibleMoneyValue calculateResult(FlexibleMoneyValue currentBalance, Either<MoneyValue,Double> argument) { return this.calculation.apply(currentBalance,argument); }

    public TransactionHelpfulness getHelpfulness(FlexibleMoneyValue currentBalance, Either<MoneyValue,Double> argument) { return this.helpfulness.apply(currentBalance,argument); }
    public int getTextColor(FlexibleMoneyValue currentBalance, Either<MoneyValue,Double> argument) { return this.getHelpfulness(currentBalance,argument).textColor; }

    TransactionType(boolean needsNumber,BiFunction<FlexibleMoneyValue,Either<MoneyValue,Double>,TransactionHelpfulness> helpfulness,BiFunction<FlexibleMoneyValue,Either<MoneyValue,Double>, FlexibleMoneyValue> calculation)
    {
        this.needsNumber = needsNumber;
        this.helpfulness = helpfulness;
        this.calculation = calculation;
    }
    TransactionType(BiFunction<FlexibleMoneyValue,Either<MoneyValue,Double>,TransactionHelpfulness> textColor,BiFunction<FlexibleMoneyValue,Double,FlexibleMoneyValue> calculation)
    {
        this(true,textColor,(old,arg) -> {
            AtomicReference<FlexibleMoneyValue> result = new AtomicReference<>(old);
            arg.ifRight(mult -> result.set(calculation.apply(old,mult)));
            return result.get();
        });
    }
    TransactionType(TransactionHelpfulness helpfulness,BiFunction<FlexibleMoneyValue,MoneyValue, FlexibleMoneyValue> calculation)
    {
        this(false,(bal,arg) -> {
            AtomicReference<TransactionHelpfulness> result = new AtomicReference<>(helpfulness);
            arg.ifLeft(val -> {
                if(val.isEmpty())
                    result.set(TransactionHelpfulness.NEUTRAL);
            }).ifRight(mult -> result.set(TransactionHelpfulness.NEUTRAL));
            return result.get();
        },(old,arg) -> {
            AtomicReference<FlexibleMoneyValue> result = new AtomicReference<>(old);
            arg.ifLeft(value -> result.set(calculation.apply(old,value)));
            return result.get();
        });
    }

}
