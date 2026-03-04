package io.github.lightman314.lightmanscurrency.client.impl;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.money.client.ClientCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.client.ClientMoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.client.builtin.ClientPlaceholderType;
import io.github.lightman314.lightmanscurrency.api.money.client.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.ClientRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClientMoneyAPIImpl extends ClientMoneyAPI {

    private final ClientRegistry<CurrencyType<?>,ClientCurrencyType> registry = new ClientRegistry<>(LCRegistries.CURRENCY_TYPE,"Client Currency Type",ClientMoneyAPIImpl::createPlaceholder);

    private boolean loopProtect = false;

    @Override
    public void RegisterClientType(ClientCurrencyType type) {
        registry.register(type.type,type);
        LightmansCurrency.LogDebug("Registered Client Currency Type: " + type.getType());
    }

    @Override
    public ClientCurrencyType GetClientType(CurrencyType<?> type) { return this.registry.getOrThrow(type); }

    private static ClientCurrencyType createPlaceholder(CurrencyType<?> type) {
        LightmansCurrency.LogWarning("Currency Type " + LCRegistries.CURRENCY_TYPE.getKey(type) + " did not register a client currency type!");
        return new ClientPlaceholderType(type);
    }

    @Nullable
    public ClientCurrencyType GetClientType(ResourceLocation type) { return this.GetClientType(LCRegistries.CURRENCY_TYPE.get(type)); }
    @Override
    public Iterable<ClientCurrencyType> AllClientTypes() { return this.registry; }

    @Override
    public List<MoneyInputHandler> GetMoneyInputs(@Nullable Player player) {
        List<MoneyInputHandler> result = new ArrayList<>();
        for(ClientCurrencyType type : this.registry)
            result.addAll(type.getInputHandlers(player));
        return result;
    }

    @Override
    public DisplayEntry GetDisplayEntry(MoneyValue value, @Nullable List<Component> additionalTooltips, boolean overrideTooltips) {
        if(this.loopProtect)
        {
            //If looping is detected, throw an exception
            this.loopProtect = false;
            throw new IllegalStateException(value.getType() + " does not have a properly registered ClientCurrencyType, nor the deprecated getDisplayEntry method!");
        }
        ClientCurrencyType type = GetClientType(value.getType());
        return type.getDisplayEntry(value,additionalTooltips,overrideTooltips);
    }
}
