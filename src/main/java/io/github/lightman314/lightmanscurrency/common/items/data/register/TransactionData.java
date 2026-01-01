package io.github.lightman314.lightmanscurrency.common.items.data.register;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.api.money.value.FlexibleMoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class TransactionData {

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

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        this.optionalMoneyAmount().ifPresent(val -> tag.put("amount",val.save()));
        this.optionalMultiplier().ifPresent(val -> tag.putDouble("mult",val));
        tag.putInt("type",this.type.ordinal());
        tag.putString("comment",this.comment);
        tag.put("resultValue",this.resultValue.save());
        return tag;
    }

    public static TransactionData load(CompoundTag tag)
    {
        Optional<MoneyValue> moneyArg = Optional.empty();
        Optional<Double> multArg = Optional.empty();
        if(tag.contains("amount"))
            moneyArg = Optional.of(MoneyValue.load(tag.getCompound("amount")));
        if(tag.contains("mult"))
            multArg = Optional.of(tag.getDouble("mult"));
        TransactionType type = EnumUtil.enumFromOrdinal(tag.getInt("type"),TransactionType.values(),TransactionType.ADD);
        String comment = tag.getString("comment");
        FlexibleMoneyValue result = FlexibleMoneyValue.load(tag.getCompound("resultValue"));
        return new TransactionData(moneyArg,multArg,type,comment,result);
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