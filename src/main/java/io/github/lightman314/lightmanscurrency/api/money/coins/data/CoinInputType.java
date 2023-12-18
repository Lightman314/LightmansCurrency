package io.github.lightman314.lightmanscurrency.api.money.coins.data;

import io.github.lightman314.lightmanscurrency.api.money.input.builtin.CoinDisplayInput;
import io.github.lightman314.lightmanscurrency.api.money.input.builtin.CoinValueInput;

import javax.annotation.Nonnull;
import java.util.function.Function;

public enum CoinInputType {
    DEFAULT(CoinValueInput::new),
    TEXT(CoinDisplayInput::new);
    private final Function<ChainData,Object> generator;
    public Object createInputHandler(@Nonnull ChainData chain) { return this.generator.apply(chain); }
    CoinInputType(@Nonnull Function<ChainData, Object> generator) { this.generator = generator; }
}
