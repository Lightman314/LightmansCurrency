package io.github.lightman314.lightmanscurrency.api.money;

import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.common.impl.MoneyAPIImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Utility class with Money Related utilities such as processing a payment to or from a player,
 * registering custom {@link CurrencyType}'s, etc.
 */
public abstract class MoneyAPI {

    public static final String MODID = "lightmanscurrency";
    public static final MoneyAPI API = MoneyAPIImpl.INSTANCE;

    /**
     * Returns a list of all registered currency types
     */
    @Nonnull
    public abstract List<CurrencyType> AllCurrencyTypes();

    /**
     * Returns the {@link CurrencyType} registered with the given id.
     * Will return <code>null</code> if no type was registered with that id.
     */
    @Nullable
    public abstract CurrencyType GetRegisteredCurrencyType(@Nonnull ResourceLocation id);

    /**
     * Registers the given {@link CurrencyType} to the system.
     * Required before loading any custom {@link MoneyValue} data.
     * I recommend registering these during the {@link net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent FMLCommonSetupEvent}
     */
    public abstract void RegisterCurrencyType(@Nonnull CurrencyType type);

    ///Player related functions

    /**
     * Gets a safe {@link io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder} for the given player.<br>
     * Should not be saved locally unless used in a situation where the player is guaranteed to not leave the dimension while in-use (such as a menu/screen).
     */
    @Nonnull
    public abstract IMoneyHolder GetPlayersMoneyHandler(@Nonnull Player player);

    ///Container related functions

    /**
     * Creates a {@link IMoneyHandler} for the given container that will allow the handling of all applicable {@link CurrencyType CurrencyTypes}.<br>
     * Uses the players inventory as the item overflow handler so that the transactions can more easily be processed without having to worry about container slot limits.
     * @see #GetContainersMoneyHandler(Container, Consumer)
     */
    @Nonnull
    public final IMoneyHandler GetContainersMoneyHandler(@Nonnull Container container, @Nonnull Player player)  { return this.CreateContainersMoneyHandler(container, s -> ItemHandlerHelper.giveItemToPlayer(player,s)); }
    /**
     * Creates a {@link IMoneyHandler} for the given container that will allow the handling of all applicable {@link CurrencyType CurrencyTypes}.<br>
     * Uses the given overflow handler to avoid limiting transactions by container size.
     * @see #GetContainersMoneyHandler(Container, Consumer)
     */
    @Nonnull
    public final IMoneyHandler GetContainersMoneyHandler(@Nonnull Container container, @Nonnull Consumer<ItemStack> overflowHandler) { return CreateContainersMoneyHandler(container, overflowHandler); }

    protected abstract IMoneyHandler CreateContainersMoneyHandler(@Nonnull Container container, @Nonnull Consumer<ItemStack> overflowHandler);

    /**
     * Creates a {@link IMoneyHandler} for the given ATM menu for depositing/withdrawing money from bank accounts.<br>
     * Used by {@link io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI BankAPI}, {@link io.github.lightman314.lightmanscurrency.common.menus.ATMMenu ATMMenu}, and {@link io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletBankMenu WalletBankMenu}
     */
    @Nonnull
    public abstract IMoneyHandler GetATMMoneyHandler(@Nonnull Player player, @Nonnull Container container);

}
