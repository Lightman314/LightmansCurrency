package io.github.lightman314.lightmanscurrency.api.coins.client;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.builtin.CoinDisplayInput;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.builtin.CoinValueInput;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.coins.data.CoinInputType;
import io.github.lightman314.lightmanscurrency.api.money.client.ClientMoneyValueType;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class ClientCoinType extends ClientMoneyValueType.WithItemDisplay {

    public static final ClientMoneyValueType INSTANCE = new ClientCoinType();

    protected ClientCoinType() {}

    @Override
    public void collectMoneyInputs(Player player,Consumer<MoneyInputHandler> builder) {
        for(ChainData chain : LCApi.getCoinAPI().lookupAllChains())
        {
            if(chain.isVisibleTo(player))
            {
                if(chain.getInputType() == CoinInputType.DEFAULT)
                    builder.accept(new CoinValueInput(chain));
                else if(chain.getInputType() == CoinInputType.TEXT)
                    builder.accept(new CoinDisplayInput(chain));
            }
        }
    }

}