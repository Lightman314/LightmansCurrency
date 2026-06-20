package io.github.lightman314.lightmanscurrency.api.money.resource.builtin;

import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class IterableMoneyResourceHandler implements MoneyResourceHandler {

    protected abstract Iterable<? extends MoneyResourceHandler> insertIterable();
    protected abstract Iterable<? extends MoneyResourceHandler> extractIterable();

    @Override
    public MoneyValue insert(MoneyValue value, TransactionContext transaction) {
        MoneyValue totalInserted = MoneyValue.empty();
        for(MoneyResourceHandler child : this.insertIterable())
        {
            //Create a sub-transaction so that we can abort if the math isn't mathing
            try(Transaction tx = Transaction.open(transaction))
            {
                MoneyValue inserted = child.insert(value,transaction);
                if(!inserted.isEmpty())
                {
                    //Subtract from the requested insertion, and add to the total inserted value
                    MoneyValue newValue = value.subtractValue(inserted);
                    MoneyValue newTotal = totalInserted.addValue(inserted);
                    //Confirm that the math succeeded
                    if(newValue != null && newTotal != null)
                    {
                        //Commit the sub-transaction
                        totalInserted = newTotal;
                        value = newValue;
                        tx.commit();
                        //Check if we should close the loop (i.e. if there's nothing left to insert)z
                        if(value.isEmpty())
                            return totalInserted;
                    }
                }
            }
        }
        return totalInserted;
    }

    @Override
    public MoneyValue extract(MoneyValue value, TransactionContext transaction) {
        MoneyValue totalExtracted = MoneyValue.empty();
        for(MoneyResourceHandler child : this.extractIterable())
        {
            //Create a sub-transaction so that we can abort if the math isn't mathing
            try(Transaction tx = Transaction.open(transaction))
            {
                MoneyValue extracted = child.extract(value,transaction);
                if(!extracted.isEmpty())
                {
                    //Subtract from the requested extraction, and add to the total extracted value
                    MoneyValue newValue = value.subtractValue(extracted);
                    MoneyValue newTotal = totalExtracted.addValue(extracted);
                    //Confirm that the math succeeded
                    if(newValue != null && newTotal != null)
                    {
                        //Commit the sub-transaction
                        totalExtracted = newTotal;
                        value = newValue;
                        tx.commit();
                        //Check if we should close the loop (i.e. if there's nothing left to extract)
                        if(value.isEmpty())
                            return totalExtracted;
                    }
                }
            }
        }
        return totalExtracted;
    }

}
