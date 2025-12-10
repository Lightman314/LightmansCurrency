package io.github.lightman314.lightmanscurrency.common.items.data.register;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.LCCodecs;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionList {

    public static final TransactionList EMPTY = new TransactionList(MoneyValue.empty(),ImmutableList.of());

    public static final Codec<TransactionList> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                LCCodecs.MONEY_VALUE.optionalFieldOf("startingValue").forGetter(TransactionList::optionalStartingValue),
                TransactionData.CODEC.listOf().fieldOf("transactions").forGetter(l -> l.transactions))
            .apply(builder,TransactionList::new));

    public static final StreamCodec<FriendlyByteBuf,TransactionList> STREAM_CODEC = StreamCodec.of((buffer,list) -> {
        list.startingValue.encode(buffer);
        buffer.writeInt(list.transactions.size());
        for(TransactionData t : list.transactions)
            TransactionData.STREAM_CODEC.encode(buffer,t);
    },buffer -> {
        MoneyValue startingValue = MoneyValue.decode(buffer);
        List<TransactionData> transactions = new ArrayList<>();
        int count = buffer.readInt();
        while(count-- > 0)
            transactions.add(TransactionData.STREAM_CODEC.decode(buffer));
        return new TransactionList(startingValue,transactions);
    });

    public final MoneyValue startingValue;
    private Optional<MoneyValue> optionalStartingValue() { return this.startingValue.isEmpty() ? Optional.empty() : Optional.of(this.startingValue); }
    public final List<TransactionData> transactions;
    private TransactionList(Optional<MoneyValue> startingValue,List<TransactionData> transactions) { this(startingValue.orElse(MoneyValue.empty()),transactions); }
    public TransactionList(MoneyValue startingValue,List<TransactionData> transactions)
    {
        this.startingValue = startingValue;
        this.transactions = ImmutableList.copyOf(transactions);
    }

    public TransactionList withStartingValue(MoneyValue startingValue) { return new TransactionList(startingValue,this.transactions).performAllCalculations(); }
    public TransactionList withAddedTransaction() {
        List<TransactionData> newList = new ArrayList<>(this.transactions);
        newList.add(TransactionData.EMPTY);
        return new TransactionList(this.startingValue,newList);
    }
    public TransactionList withEditedTransaction(int index, TransactionData newData)
    {
        List<TransactionData> newList = new ArrayList<>(this.transactions);
        if(index >= 0 && index < newList.size())
            newList.set(index,newData);
        return new TransactionList(this.startingValue,performCalculations(index,this.startingValue,newList));
    }

    private static List<TransactionData> performCalculations(int startingIndex, MoneyValue startingValue, List<TransactionData> transactions)
    {
        if(startingIndex < 0 || startingIndex >= transactions.size())
            return transactions;
        MoneyValue currentBalance = startingIndex == 0 ? startingValue : transactions.get(startingIndex - 1).resultValue;
        List<TransactionData> result = new ArrayList<>(transactions);
        for(int i = startingIndex; i < transactions.size(); ++i)
        {
            TransactionData d = transactions.get(i);
            TransactionData newData = d.calculateResult(currentBalance);
            result.set(i,newData);
            currentBalance = newData.resultValue;
        }
        return result;
    }

    public TransactionList performAllCalculations()
    {
        MoneyValue currentBalance = this.startingValue;
        List<TransactionData> result = performCalculations(-1,this.startingValue,this.transactions);
        return new TransactionList(this.startingValue,result);
    }

    public MoneyValue getCurrentValue()
    {
        if(this.transactions.isEmpty())
            return this.transactions.getLast().resultValue;
        return this.startingValue;
    }


}
