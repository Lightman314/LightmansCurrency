package io.github.lightman314.lightmanscurrency.common.items.data.register;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.money.value.FlexibleMoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TransactionList {

    public static final TransactionList EMPTY = new TransactionList(MoneyValue.empty(),ImmutableList.of());

    public final MoneyValue startingValue;
    private Optional<MoneyValue> optionalStartingValue() { return this.startingValue.isEmpty() ? Optional.empty() : Optional.of(this.startingValue); }
    public final List<TransactionData> transactions;
    private TransactionList(Optional<MoneyValue> startingValue,List<TransactionData> transactions) { this(startingValue.orElse(MoneyValue.empty()),transactions); }
    public TransactionList(MoneyValue startingValue,List<TransactionData> transactions)
    {
        this.startingValue = startingValue;
        this.transactions = ImmutableList.copyOf(transactions);
    }

    public TransactionList withStartingValue(MoneyValue startingValue) {
        if(this.hasValueConflict(false,-1,startingValue))
            return this;
        return new TransactionList(startingValue,this.transactions).performAllCalculations();
    }
    public TransactionList withAddedTransaction() {
        List<TransactionData> newList = new ArrayList<>(this.transactions);
        newList.add(TransactionData.EMPTY);
        return new TransactionList(this.startingValue,newList);
    }
    public TransactionList withEditedTransaction(int index, TransactionData newData)
    {
        if(this.hasValueConflict(index,newData))
            return this;
        List<TransactionData> newList = new ArrayList<>(this.transactions);
        if(index >= 0 && index < newList.size())
            newList.set(index,newData);
        return new TransactionList(this.startingValue,performCalculations(index,this.startingValue,newList));
    }
    public TransactionList withDeletedTransaction(int index)
    {
        if(index >= 0 && index < this.transactions.size())
        {
            List<TransactionData> newList = new ArrayList<>(this.transactions);
            newList.remove(index);
            return new TransactionList(this.startingValue,performCalculations(index,this.startingValue,newList));
        }
        return this;
    }

    private boolean hasValueConflict(boolean checkStartingValue,int ignoreTransaction,MoneyValue newValue)
    {
        if(newValue.isEmpty())
            return false;
        if(checkStartingValue && !this.startingValue.isEmpty())
        {
            if(!this.startingValue.sameType(newValue))
                return true;
        }
        AtomicBoolean hasConflict = new AtomicBoolean(false);
        for(int i = 0; i < this.transactions.size(); ++i)
        {
            if(i == ignoreTransaction)
                continue;
            TransactionData d = this.transactions.get(i);
            d.argument.ifLeft(val -> {
                if(!val.isEmpty() && !val.sameType(newValue))
                    hasConflict.set(true);
            });
            if(hasConflict.get())
                return true;
        }
        return false;
    }

    private boolean hasValueConflict(int transactionIndex,TransactionData data) {
        AtomicReference<MoneyValue> newValue = new AtomicReference<>(MoneyValue.empty());
        data.argument.ifLeft(newValue::set);
        return hasValueConflict(true,transactionIndex,newValue.get());
    }

    private static List<TransactionData> performCalculations(int startingIndex, MoneyValue startingValue, List<TransactionData> transactions)
    {
        if(startingIndex < 0 || startingIndex >= transactions.size())
            return transactions;
        FlexibleMoneyValue currentBalance = startingIndex == 0 ? FlexibleMoneyValue.positive(startingValue) : transactions.get(startingIndex - 1).resultValue;
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
        List<TransactionData> result = performCalculations(0,this.startingValue,this.transactions);
        return new TransactionList(this.startingValue,result);
    }

    public FlexibleMoneyValue getCurrentValue()
    {
        if(this.transactions.isEmpty())
            return this.transactions.get(this.transactions.size() - 1).resultValue;
        return FlexibleMoneyValue.of(false,this.startingValue);
    }

    @Override
    public int hashCode() { return Objects.hash(this.startingValue,this.transactions); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TransactionList other)
            return other.startingValue.equals(this.startingValue) && this.transactions.equals(other.transactions);
        return false;
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.put("startingValue",this.startingValue.save());
        ListTag list = new ListTag();
        for(TransactionData t : this.transactions)
            list.add(t.save());
        tag.put("transactions",list);
        return tag;
    }

    public static TransactionList load(CompoundTag tag)
    {
        MoneyValue startingValue = MoneyValue.load(tag.getCompound("startingValue"));
        ListTag list = tag.getList("transactions", Tag.TAG_COMPOUND);
        List<TransactionData> transactions = new ArrayList<>();
        for(int i = 0; i < list.size(); ++i)
            transactions.add(TransactionData.load(list.getCompound(i)));
        return new TransactionList(startingValue,transactions);
    }

}