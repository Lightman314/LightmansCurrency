package io.github.lightman314.lightmanscurrency.api.money.coins.data.client;

import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.CoinInputType;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.input.builtin.CoinDisplayInput;
import io.github.lightman314.lightmanscurrency.api.money.input.builtin.CoinValueInput;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;


public class CoinInputTypeHelper {

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public static MoneyInputHandler getHandler(@Nonnull CoinInputType type, @Nonnull ChainData chain)
    {
        return switch (type) {
            case DEFAULT -> new CoinValueInput(chain);
            case TEXT -> new CoinDisplayInput(chain);
        };
    }

}
