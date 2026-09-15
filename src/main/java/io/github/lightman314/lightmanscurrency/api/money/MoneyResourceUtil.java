package io.github.lightman314.lightmanscurrency.api.money;

import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;

public final class MoneyResourceUtil {

    private MoneyResourceUtil() {}

    public static MoneyValue move(MoneyResourceHandler from,MoneyResourceHandler to,MoneyValue amount,@Nullable TransactionContext transaction) {
        if(amount.isEmpty())
            return amount;
        try(Transaction tx = Transaction.open(transaction)) {
            MoneyValue extracted = from.extract(amount,tx);
            if(!extracted.isEmpty()) {
                MoneyValue inserted = to.insert(extracted,tx);
                if(inserted.equals(extracted)) {
                    tx.commit();
                    return inserted;
                }
                else if(!inserted.isEmpty() && inserted.getInternalValue() < extracted.getInternalValue()){
                    tx.close();
                    //Attempt again with the amount that was successfully inserted
                    try(Transaction t2 = Transaction.open(transaction)) {
                        extracted = from.extract(inserted,t2);
                        if(!extracted.isEmpty()) {
                            inserted = to.insert(extracted,t2);
                            if(inserted.equals(extracted)) {
                                t2.commit();
                                return inserted;
                            }
                        }
                    }
                }
            }
        }
        return MoneyValue.empty();
    }

    /**
     * Runs {@link MoneyResourceHandler#insert(MoneyValue, TransactionContext)} with the relevant arguments, but returns the money not yet inserted
     * @param handler The Money Handler to perform the insert interaction on
     * @param amount The amount of money to attempt to insert into the oney handler
     * @param transaction The transaction
     * @return The amount of money that was <b>not</b> yet inserted into the money handler
     */
    public static MoneyValue insertReturnRemainder(MoneyResourceHandler handler,MoneyValue amount,TransactionContext transaction) {
        if(amount.isEmpty())
            return amount;
        try(Transaction tx = Transaction.open(transaction)) {
            MoneyValue inserted = handler.insert(amount,transaction);
            MoneyValue remainder = amount.subtractValue(inserted);
            //If the remainder could not be calculated, don't commit the sub-transaction and return the amount requested to insert
            if(remainder == null)
                return amount;
            tx.commit();
            return remainder;
        }
    }

    /**
     * Runs {@link MoneyResourceHandler#extract(MoneyValue, TransactionContext)} with the relevant arguments, but returns the money not yet extracted
     * @param handler The Money Handler to perform the extract interaction on
     * @param amount The amount of money to attempt to extract into the oney handler
     * @param transaction The transaction
     * @return The amount of money that was <b>not</b> yet extracted from the money handler
     */
    public static MoneyValue extractReturnRemainder(MoneyResourceHandler handler,MoneyValue amount,TransactionContext transaction) {
        if(amount.isEmpty())
            return amount;
        try(Transaction tx = Transaction.open(transaction)) {
            MoneyValue extracted = handler.extract(amount,transaction);
            MoneyValue remainder = amount.subtractValue(extracted);
            //If the remainder could not be calculated, don't commit the sub-transaction and return the amount requested to extract
            if(remainder == null)
                return amount;
            tx.commit();
            return remainder;
        }
    }

}
