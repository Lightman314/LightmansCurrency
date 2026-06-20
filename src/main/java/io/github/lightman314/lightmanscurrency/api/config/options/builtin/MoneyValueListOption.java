package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import com.google.common.base.Predicates;
import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class MoneyValueListOption extends ListOption<MoneyValue> {

    private final ConfigParser<MoneyValue> parser;

    protected MoneyValueListOption(Supplier<List<MoneyValue>> defaultValue) { super(defaultValue); this.parser = MoneyValueOption.createParser(Predicates.alwaysTrue()); }

    @Override
    public boolean isValidEntryType(Class<?> clazz) { return MoneyValue.class.isAssignableFrom(clazz); }

    @Nullable
    @Override
    protected String bonusComment() { return MoneyValueOption.bonusComment; }

    @Override
    protected ConfigParser<MoneyValue> getPartialParser() { return this.parser; }

    public static MoneyValueListOption create(Supplier<List<MoneyValue>> defaultValue) { return new MoneyValueListOption(defaultValue); }
}