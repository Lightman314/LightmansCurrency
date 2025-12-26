package io.github.lightman314.lightmanscurrency.api.money;

import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.common.impl.MoneyAPIImpl;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;

/**
 * Utility class with Money Related utilities such as processing a payment to or from a player,
 * registering custom {@link CurrencyType}'s, etc.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class MoneyAPI {

    public static final String MODID = "lightmanscurrency";
    private static MoneyAPI instance;
    public static MoneyAPI getApi()
    {
        if(instance == null)
            instance = new MoneyAPIImpl();
        return instance;
    }

    protected MoneyAPI() { if(instance != null)  throw new IllegalCallerException("Cannot create a new MoneyAPI instance as one is already present!"); }

    /**
     * Returns a list of all registered currency types
     */
    public abstract List<CurrencyType> AllCurrencyTypes();

    /**
     * Returns the {@link CurrencyType} registered with the given id.
     * Will return <code>null</code> if no type was registered with that id.
     */
    @Nullable
    public abstract CurrencyType GetRegisteredCurrencyType(ResourceLocation id);

    /**
     * Registers the given {@link CurrencyType} to the system.
     * Required before loading any custom {@link MoneyValue} data.
     * I recommend registering these during the {@link net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent FMLCommonSetupEvent}
     */
    public abstract void RegisterCurrencyType(CurrencyType type);

    ///Player related functions

    /**
     * Gets a safe {@link io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder} for the given player.<br>
     * Should not be saved locally unless used in a situation where the player is guaranteed to not leave the dimension while in-use (such as a menu/screen).
     */
    public abstract IMoneyHolder GetPlayersMoneyHandler(Player player);

    /**
     * Gets an unsafe version of the {@link IMoneyHolder} for the given player.
     */
    public abstract IMoneyHolder GetPlayersMoneyHandlerUnsafe(Player player);

    ///Container related functions

    /**
     * Creates a {@link IMoneyHandler} for the given container that will allow the handling of all applicable {@link CurrencyType CurrencyTypes}.<br>
     * Uses the players inventory as the item overflow handler so that the transactions can more easily be processed without having to worry about container slot limits.
     * @see #GetContainersMoneyHandler(Container,Consumer,IClientTracker)
     */
    public final IMoneyHandler GetContainersMoneyHandler(Container container, Player player)  { return this.CreateContainersMoneyHandler(container, s -> ItemHandlerHelper.giveItemToPlayer(player,s), IClientTracker.entityWrapper(player)); }
    /**
     * Creates a {@link IMoneyHandler} for the given container that will allow the handling of all applicable {@link CurrencyType CurrencyTypes}.<br>
     * Uses the given overflow handler to avoid limiting transactions by container size.
     * @see #GetContainersMoneyHandler(Container,Consumer,IClientTracker)
     */
    public final IMoneyHandler GetContainersMoneyHandler(Container container, Consumer<ItemStack> overflowHandler, IClientTracker tracker) { return CreateContainersMoneyHandler(container, overflowHandler, tracker); }

    protected abstract IMoneyHandler CreateContainersMoneyHandler(Container container, Consumer<ItemStack> overflowHandler, IClientTracker tracker);

    /**
     * Creates a {@link IMoneyHandler} for the given ATM menu for depositing/withdrawing money from bank accounts.<br>
     * Used by {@link io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI BankAPI}, {@link io.github.lightman314.lightmanscurrency.common.menus.ATMMenu ATMMenu}, and {@link io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletBankMenu WalletBankMenu}
     */
    public abstract IMoneyHandler GetATMMoneyHandler(Player player, Container container);

    /**
     * Whether the given item is allowed within the {@link io.github.lightman314.lightmanscurrency.api.misc.menus.MoneySlot MoneySlot}<br>
     * Player is required for context
     */
    public abstract boolean ItemAllowedInMoneySlot(Player player, ItemStack stack);

}
