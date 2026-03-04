package io.github.lightman314.lightmanscurrency.api.money.coins.data.client;

import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.CoinInputType;
import io.github.lightman314.lightmanscurrency.api.money.client.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.client.input.builtin.CoinDisplayInput;
import io.github.lightman314.lightmanscurrency.api.money.client.input.builtin.CoinValueInput;

public class CoinInputTypeHelper {
    
    public static MoneyInputHandler getHandler(CoinInputType type, ChainData chain)
    {
        return switch (type) {
            case DEFAULT -> new CoinValueInput(chain);
            case TEXT -> new CoinDisplayInput(chain);
        };
    }

}
