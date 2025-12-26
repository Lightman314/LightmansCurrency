package io.github.lightman314.lightmanscurrency.api.money.client.builtin;

import io.github.lightman314.lightmanscurrency.api.money.client.ClientCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.NullCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display.EmptyPriceEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClientNullType extends ClientCurrencyType {

    public static final ClientCurrencyType INSTANCE = new ClientNullType();

    private ClientNullType() { super(NullCurrencyType.INSTANCE); }
    @Override
    public List<MoneyInputHandler> getInputHandlers(@Nullable Player player) { return new ArrayList<>(); }
    @Override
    public DisplayEntry getDisplayEntry(MoneyValue value, @Nullable List<Component> additionalTooltips, boolean overrideTooltips) { return new EmptyPriceEntry(value, additionalTooltips); }

}
