package io.github.lightman314.lightmanscurrency.common.items.data.register;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.money.value.FlexibleMoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class TransactionData {

    public static final Codec<TransactionData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            MoneyValue.CODEC.optionalFieldOf("amount").forGetter(TransactionData::optionalMoneyAmount),
            Codec.DOUBLE.optionalFieldOf("mult").forGetter(TransactionData::optionalMultiplier),
            TransactionType.CODEC.fieldOf("type").forGetter(d -> d.type),
            Codec.STRING.fieldOf("comment").forGetter(d -> d.comment),
            FlexibleMoneyValue.CODEC.fieldOf("resultValue").forGetter(d -> d.resultValue)
    ).apply(builder,TransactionData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,TransactionData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(MoneyValue.STREAM_CODEC),TransactionData::optionalMoneyAmount,
            ByteBufCodecs.optional(ByteBufCodecs.DOUBLE),TransactionData::optionalMultiplier,
            TransactionType.STREAM_CODEC,data -> data.type,
            ByteBufCodecs.STRING_UTF8,data -> data.comment,
            FlexibleMoneyValue.STREAM_CODEC,data -> data.resultValue,
            TransactionData::new);

    public static final TransactionData EMPTY = new TransactionData(Either.left(MoneyValue.empty()),TransactionType.ADD,"", FlexibleMoneyValue.EMPTY);

    public final Either<MoneyValue,Double> argument;
    public Optional<MoneyValue> optionalMoneyAmount() {
        AtomicReference<MoneyValue> h = new AtomicReference<>(null);
        this.argument.ifLeft(h::set);
        return Optional.ofNullable(h.get());
    }
    public Optional<Double> optionalMultiplier() {
        AtomicReference<Double> h = new AtomicReference<>(null);
        this.argument.ifRight(h::set);
        return Optional.ofNullable(h.get());
    }
    public final TransactionType type;
    public final String comment;
    public final FlexibleMoneyValue resultValue;
    private TransactionData(Optional<MoneyValue> moneyArg, Optional<Double> multArg, TransactionType type, String comment, FlexibleMoneyValue result)
    {
        if(moneyArg.isPresent())
            this.argument = Either.left(moneyArg.get());
        else
            this.argument = Either.right(multArg.orElse(1d));
        this.type = type;
        this.comment = comment;
        this.resultValue = result;
    }
    private TransactionData(Either<MoneyValue,Double> argument, TransactionType type, String comment, FlexibleMoneyValue result)
    {
        this.argument = argument;
        this.type = type;
        this.comment = comment;
        this.resultValue = result;
    }

    public boolean isValid() { return this.type.isValidArgument(this.argument); }

    public TransactionData withArgument(MoneyValue amount) {
        if(this.type.needsNumber)
            return this;
        return new TransactionData(Either.left(amount),this.type,this.comment,this.resultValue);
    }
    public TransactionData withArgument(double mult) {
        if(!this.type.needsNumber)
            return this;
        return new TransactionData(Either.right(mult),this.type,this.comment,this.resultValue);
    }
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

    public TransactionData calculateResult(FlexibleMoneyValue currentBalance) { return new TransactionData(this.argument,this.type,this.comment,this.type.calculateResult(currentBalance,this.argument)); }

    @Override
    public int hashCode() { return Objects.hash(this.argument,this.type,this.comment,this.resultValue); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TransactionData other)
            return this.argument.equals(other.argument) && this.type == other.type && this.comment.equals(other.comment) && this.resultValue.equals(other.resultValue);
        return false;
    }

}
