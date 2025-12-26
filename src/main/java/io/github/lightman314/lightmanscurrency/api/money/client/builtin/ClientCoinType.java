package io.github.lightman314.lightmanscurrency.api.money.client.builtin;

import io.github.lightman314.lightmanscurrency.api.money.client.ClientCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.client.CoinInputTypeHelper;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.CoinCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display.CoinPriceEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClientCoinType extends ClientCurrencyType {

    public static final ClientCurrencyType INSTANCE = new ClientCoinType();

    private ClientCoinType() { super(CoinCurrencyType.INSTANCE); }

    @Override
    public List<MoneyInputHandler> getInputHandlers(@Nullable Player player) {
        List<MoneyInputHandler> results = new ArrayList<>();
        for(ChainData chain : CoinAPI.getApi().AllChainData())
        {
            //Only add input handler if the chain is visible to the player
            if(player == null || chain.isVisibleTo(player))
                results.add(CoinInputTypeHelper.getHandler(chain.getInputType(),chain));
        }
        return results;
    }

    @Override
    public DisplayEntry getDisplayEntry(MoneyValue value, @Nullable List<Component> additionalTooltips, boolean overrideTooltips) {
        if(value instanceof CoinValue val)
            return new CoinPriceEntry(val, additionalTooltips, overrideTooltips);
        return this.throwIllegalDisplayException(value,CoinValue.class);
    }
}
