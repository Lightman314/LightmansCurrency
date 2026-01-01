package io.github.lightman314.lightmanscurrency.client.impl;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.client.ClientCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.client.ClientMoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.client.builtin.ClientPlaceholderType;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientMoneyAPIImpl extends ClientMoneyAPI {

    private final Map<ResourceLocation,ClientCurrencyType> registeredCurrencyTypes = new HashMap<>();

    private boolean loopProtect = false;

    @Override
    public void RegisterClientType(ClientCurrencyType type) {
        if(this.registeredCurrencyTypes.containsKey(type.type.getType()))
        {
            ClientCurrencyType existingType = this.registeredCurrencyTypes.get(type.getType());
            if(existingType == type)
                LightmansCurrency.LogWarning("Client Money Type " + type.getType() + " was registered twice!");
            else
                LightmansCurrency.LogError("Tried to register Client Money Type " + type.getType() + ", but another type has already been registered under that id!");
            return;
        }
        this.registeredCurrencyTypes.put(type.getType(),type);
        LightmansCurrency.LogDebug("Registered Client Currency Type: " + type.getType());
    }

    @Override
    public ClientCurrencyType GetClientType(CurrencyType type) {
        ClientCurrencyType result = GetClientType(type.getType());
        if(result == null)
        {
            LightmansCurrency.LogWarning("Currency Type " + type.getType() + " did not register a client currency type!");
            result = new ClientPlaceholderType(type);
            this.RegisterClientType(result);
        }
        return result;
    }

    @Nullable
    @Override
    public ClientCurrencyType GetClientType(ResourceLocation type) { return this.registeredCurrencyTypes.get(type); }
    @Override
    public List<ClientCurrencyType> AllClientTypes() { return new ArrayList<>(this.registeredCurrencyTypes.values()); }

    @Override
    @SuppressWarnings("deprecation")
    public List<MoneyInputHandler> GetMoneyInputs(@Nullable Player player) {
        List<MoneyInputHandler> result = new ArrayList<>();
        for(ClientCurrencyType type : new ArrayList<>(this.registeredCurrencyTypes.values()))
            result.addAll(type.getInputHandlers(player));
        //Deprecated Method
        for(CurrencyType type : MoneyAPI.getApi().AllCurrencyTypes())
        {
            for(Object o : type.getInputHandlers(player))
            {
                if(o instanceof MoneyInputHandler h)
                    result.add(h);
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("deprecation")
    public DisplayEntry GetDisplayEntry(MoneyValue value, @Nullable List<Component> additionalTooltips, boolean overrideTooltips) {
        if(this.loopProtect)
        {
            //If looping is detected, throw an exception
            this.loopProtect = false;
            throw new IllegalStateException(value.getCurrency().getType() + " does not have a properly registered ClientCurrencyType, nor the deprecated getDisplayEntry method!");
        }
        ClientCurrencyType type = GetClientType(value.getCurrency());
        DisplayEntry entry = type.getDisplayEntry(value,additionalTooltips,overrideTooltips);
        if(entry == null)
        {
            //Set the loop protect flag as the default implementation of the deprecated method calls this API function
            this.loopProtect = true;
            entry = value.getDisplayEntry(additionalTooltips,overrideTooltips);
            this.loopProtect = false;
        }
        return entry;
    }
}