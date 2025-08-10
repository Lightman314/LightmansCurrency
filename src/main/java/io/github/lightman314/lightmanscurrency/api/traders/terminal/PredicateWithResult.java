package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PredicateWithResult<T> implements Predicate<T>, Supplier<Boolean>
{
    boolean passed = false;
    private final Predicate<T> test;
    public PredicateWithResult(Predicate<T> test){ this.test = test; }
    public static <T> PredicateWithResult<T> create(Predicate<T> test) { return new PredicateWithResult<>(test); }
    public static <T> PredicateWithResult<T> create(Predicate<T> test,AtomicReference<Predicate<T>> holder) { return new PredicateWithResult<>(test).andStore(holder); }
    @Override
    public boolean test(T value) {
        if(this.test.test(value))
        {
            this.passed = true;
            return true;
        }
        return false;
    }
    @Override
    public Boolean get() { return this.passed; }
    public PredicateWithResult<T> andStore(AtomicReference<Predicate<T>> holder) { holder.set(this); return this; }
}