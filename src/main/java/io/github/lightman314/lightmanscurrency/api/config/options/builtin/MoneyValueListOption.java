package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;

public class MoneyValueListOption extends ListOption<MoneyValue> {

    private final ConfigParser<MoneyValue> parser;

    protected MoneyValueListOption(@Nonnull NonNullSupplier<List<MoneyValue>> defaultValue, @Nonnull Predicate<MoneyValue> allowed) { super(defaultValue); this.parser = MoneyValueOption.createParser(allowed); }

    @Override
    protected ConfigParser<MoneyValue> getPartialParser() { return this.parser; }

    public static MoneyValueListOption create(@Nonnull NonNullSupplier<List<MoneyValue>> defaultValue) { return new MoneyValueListOption(defaultValue, v -> true); }
    public static MoneyValueListOption createNonEmpty(@Nonnull NonNullSupplier<List<MoneyValue>> defaultValue) { return new MoneyValueListOption(defaultValue, v -> !v.isEmpty()); }
    public static MoneyValueListOption create(@Nonnull NonNullSupplier<List<MoneyValue>> defaultValue, @Nonnull Predicate<MoneyValue> allowed) { return new MoneyValueListOption(defaultValue, allowed); }
}
