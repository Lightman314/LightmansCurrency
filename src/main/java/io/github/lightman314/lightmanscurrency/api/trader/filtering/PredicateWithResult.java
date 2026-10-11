package io.github.lightman314.lightmanscurrency.api.trader.filtering;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class PredicateWithResult<T> implements Predicate<T>, BooleanSupplier {

    boolean passed = false;
    private final Predicate<T> test;
    private PredicateWithResult(Predicate<T> test) { this.test = test; }
    public static <T> PredicateWithResult<T> create(Predicate<T> test) { return new PredicateWithResult<>(test); }
    public static <T> PredicateWithResult<T> create(Predicate<T> test, AtomicReference<Predicate<T>> holder) { return new PredicateWithResult<>(test).andStore(holder); }

    @Override
    public boolean test(T t) {
        if(this.test.test(t)) {
            this.passed = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean getAsBoolean() { return this.passed; }

    public PredicateWithResult<T> andStore(AtomicReference<Predicate<T>> holder) { holder.set(this); return this; }

}