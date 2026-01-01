package io.github.lightman314.lightmanscurrency.api.money.client;

import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.impl.ClientMoneyAPIImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ClientMoneyAPI {

    private static ClientMoneyAPI instance;
    public static ClientMoneyAPI getApi()
    {
        if(instance == null)
            instance = new ClientMoneyAPIImpl();
        return instance;
    }

    protected ClientMoneyAPI() { if(instance != null)  throw new IllegalCallerException("Cannot create a new ClientMoneyAPI instance as one is already present!"); }

    /**
     * Registers the given {@link ClientCurrencyType} to the system.
     * Required on the physical client for any {@link CurrencyType} registered via {@link MoneyAPI#RegisterCurrencyType(CurrencyType)}
     * I recommend registering these during the {@link net.neoforged.fml.event.lifecycle.FMLClientSetupEvent FMLClientSetupEvent}
     */
    public abstract void RegisterClientType(ClientCurrencyType type);

    /**
     * Returns the {@link ClientCurrencyType} registered for the given {@link CurrencyType}
     */
    public abstract ClientCurrencyType GetClientType(CurrencyType type);
    /**
     * Returns the {@link ClientCurrencyType} registered with the given id.
     * Will return <code>null</code> if no type was registered with that id.
     */
    @Nullable
    public abstract ClientCurrencyType GetClientType(ResourceLocation type);

    /**
     * Returns a list of all registered {@link ClientCurrencyType}s
     */
    public abstract List<ClientCurrencyType> AllClientTypes();

    /**
     * Returns a list of all {@link MoneyInputHandler}s that are visible to the given player
     */
    public abstract List<MoneyInputHandler> GetMoneyInputs(@Nullable Player player);

    /**
     * Obtains a {@link DisplayEntry} for the given Money Value
     */
    public abstract DisplayEntry GetDisplayEntry(MoneyValue value, @Nullable List<Component> additionalTooltips, boolean overrideTooltips);


}