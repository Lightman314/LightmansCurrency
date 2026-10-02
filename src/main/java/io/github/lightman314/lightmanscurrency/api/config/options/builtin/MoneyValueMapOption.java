package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.MapLikeOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.minecraft.IdentifierException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class MoneyValueMapOption extends MapLikeOption<Map<DualKey,MoneyValue>> {

    private static final ConfigParser<MoneyValue> MONEY_PARSER = MoneyValueOption.createParser(v -> !v.isEmpty());
    public static final ConfigParser<Map<DualKey,MoneyValue>> PARSER = ListOption.makeParser(MONEY_PARSER).map(list -> {
        Map<DualKey,MoneyValue> map = new HashMap<>();
        for(MoneyValue value : list) {
            DualKey key = value.getKey();
            if(map.containsKey(key))
                LightmansCurrency.LogWarning("Cannot have multiple money values of type '" + key + "' in this option!");
            else
                map.put(key,value);
        }
        return ImmutableMap.copyOf(map);
    },map -> new ArrayList<>(map.values()));

    protected MoneyValueMapOption(Supplier<Map<DualKey,MoneyValue>> defaultValue) { super(defaultValue); }

    public final Optional<MoneyValue> get(DualKey key) { return Optional.ofNullable(this.get().get(key)); }
    public final MoneyValue getOrEmpty(DualKey key) { return this.get(key).orElseGet(MoneyValue::empty); }

    public static MoneyValueMapOption create() { return create(ImmutableMap::of); }
    public static MoneyValueMapOption create(Supplier<Map<DualKey,MoneyValue>> defaultValue) { return new MoneyValueMapOption(defaultValue); }

    @Override
    public Pair<Boolean,ConfigParsingException> editMap(String value,String key,boolean isSet) {
        try {
            DualKey moneyKey = DualKey.parse(key);
            Map<DualKey,MoneyValue> currentValue = new HashMap<>(this.getCurrentValue());
            if(isSet) {
                MoneyValue amount = MONEY_PARSER.tryParse(value);
                if(!amount.getKey().equals(moneyKey))
                    throw new ConfigParsingException("Cannot set the value of type '" + moneyKey + "' to a value of type '" + amount.getKey() + "'!");
                currentValue.put(moneyKey,amount);
                this.set(ImmutableMap.copyOf(currentValue));
            }
            else {
                if(currentValue.containsKey(moneyKey)) {
                    currentValue.remove(moneyKey);
                    this.set(ImmutableMap.copyOf(currentValue));
                }
                else throw new ConfigParsingException("Cannot remove the entry of type '" + moneyKey + "' as it is not present.");
            }
        } catch (IdentifierException e) { return Pair.of(false,new ConfigParsingException(e));
        } catch (ConfigParsingException e) { return Pair.of(false,e); }
        return Pair.of(true,null);
    }

    @Override
    protected ConfigParser<Map<DualKey, MoneyValue>> getParser() { return PARSER; }

}