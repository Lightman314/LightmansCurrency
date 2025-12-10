package io.github.lightman314.lightmanscurrency.common.items.data.register;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.LCCodecs;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class TransactionData {

    public static final Codec<TransactionType> TYPE_CODEC = EnumUtil.buildCodec(TransactionType.class,"Transaction Type");

    public static final Codec<TransactionData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            LCCodecs.MONEY_VALUE.optionalFieldOf("amount").forGetter(TransactionData::optionalMoneyAmount),
            Codec.DOUBLE.optionalFieldOf("mult").forGetter(TransactionData::optionalMultiplier),
            TYPE_CODEC.fieldOf("type").forGetter(d -> d.type),
            Codec.STRING.fieldOf("comment").forGetter(d -> d.comment),
            LCCodecs.MONEY_VALUE.fieldOf("resultValue").forGetter(d -> d.resultValue)
    ).apply(builder,TransactionData::new));

    public static final StreamCodec<FriendlyByteBuf,TransactionData> STREAM_CODEC = StreamCodec.of((buffer,data) -> {
        Optional<MoneyValue> moneyArgument = data.optionalMoneyAmount();
        Optional<Double> multArgument = data.optionalMultiplier();
        buffer.writeBoolean(moneyArgument.isPresent());
        if(moneyArgument.isPresent())
            moneyArgument.get().encode(buffer);
        else if(multArgument.isPresent())
            buffer.writeDouble(multArgument.get());
        else
            throw new IllegalStateException("Somehow neither the Money Value OR Multiplier arguments are present!");
        buffer.writeInt(data.type.ordinal());
        buffer.writeUtf(data.comment);
        data.resultValue.encode(buffer);
    },buffer -> {
        Either<MoneyValue,Double> arg;
        if(buffer.readBoolean())
            arg = Either.left(MoneyValue.decode(buffer));
        else
            arg = Either.right(buffer.readDouble());
        return new TransactionData(arg,EnumUtil.enumFromOrdinal(buffer.readInt(),TransactionType.values(),TransactionType.ADD),buffer.readUtf(),MoneyValue.decode(buffer));
    });

    public static final TransactionData EMPTY = new TransactionData(Either.left(MoneyValue.empty()),TransactionType.ADD,"",MoneyValue.empty());

    public final Either<MoneyValue,Double> argument;
    private Optional<MoneyValue> optionalMoneyAmount() {
        AtomicReference<MoneyValue> h = new AtomicReference<>(null);
        this.argument.ifLeft(h::set);
        return Optional.ofNullable(h.get());
    }
    private Optional<Double> optionalMultiplier() {
        AtomicReference<Double> h = new AtomicReference<>(null);
        this.argument.ifRight(h::set);
        return Optional.ofNullable(h.get());
    }
    public final TransactionType type;
    public final String comment;
    public final MoneyValue resultValue;
    private TransactionData(Optional<MoneyValue> moneyArg, Optional<Double> multArg, TransactionType type, String comment, MoneyValue result)
    {
        if(moneyArg.isPresent())
            this.argument = Either.left(moneyArg.get());
        else
            this.argument = Either.right(multArg.orElse(1d));
        this.type = type;
        this.comment = comment;
        this.resultValue = result;
    }
    private TransactionData(Either<MoneyValue,Double> argument, TransactionType type, String comment, MoneyValue result)
    {
        this.argument = argument;
        this.type = type;
        this.comment = comment;
        this.resultValue = result;
    }

    public boolean isValid() { return this.type.isValidArgument(this.argument); }

    public TransactionData withArgument(MoneyValue amount) { return new TransactionData(Either.left(amount),this.type,this.comment,this.resultValue); }
    public TransactionData withArgument(double mult) { return new TransactionData(Either.right(mult),this.type,this.comment,this.resultValue); }
    public TransactionData withType(TransactionType type) {
        Either<MoneyValue,Double> newArg = this.argument;
        if(!type.isValidArgument(newArg))
        {
            if(type.needsNumber)
                newArg = Either.right(1d);
            else
                newArg = Either.left(MoneyValue.empty());
        }
        return new TransactionData(newArg,type,this.comment,this.resultValue);
    }
    public TransactionData withComment(String comment) { return new TransactionData(this.argument,this.type,comment,this.resultValue); }

    public TransactionData calculateResult(MoneyValue currentBalance) { return new TransactionData(this.argument,this.type,this.comment,this.type.calculateResult(currentBalance,this.argument)); }

    public enum TransactionType {
        ADD(MoneyValue::addValue),
        SUBTRACT(MoneyValue::subtractValue),
        MULTIPLY(null,MoneyValue::multiplyValue);


        private final boolean needsNumber;
        private final BiFunction<MoneyValue,Either<MoneyValue,Double>,MoneyValue> calculation;

        public boolean isValidArgument(Either<MoneyValue,Double> argument)
        {
            AtomicBoolean result = new AtomicBoolean(false);
            argument.ifLeft(v -> result.set(!this.needsNumber))
                    .ifRight(m -> result.set(this.needsNumber));
            return result.get();
        }
        public MoneyValue calculateResult(MoneyValue currentBalance,Either<MoneyValue,Double> argument) { return this.calculation.apply(currentBalance,argument); }

        TransactionType(boolean needsNumber,BiFunction<MoneyValue,Either<MoneyValue,Double>,MoneyValue> calculation)
        {
            this.needsNumber = needsNumber;
            this.calculation = calculation;
        }
        TransactionType(Void dummy,BiFunction<MoneyValue,Double,MoneyValue> calculation)
        {
            this(true,(old,arg) -> {
                AtomicReference<MoneyValue> result = new AtomicReference<>(old);
                arg.ifRight(mult -> result.set(calculation.apply(old,mult)));
                return result.get();
            });
        }
        TransactionType(BiFunction<MoneyValue,MoneyValue,MoneyValue> calculation)
        {
            this(false,(old,arg) -> {
                AtomicReference<MoneyValue> result = new AtomicReference<>(old);
                arg.ifLeft(value -> result.set(calculation.apply(old,value)));
                return result.get();
            });
        }

    }

}
