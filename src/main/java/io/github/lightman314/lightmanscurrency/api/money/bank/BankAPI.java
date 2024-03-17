package io.github.lightman314.lightmanscurrency.api.money.bank;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.money.bank.source.BankAccountSource;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.impl.BankAPIImpl;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class BankAPI {

    public static final BankAPI API = BankAPIImpl.INSTANCE;

    /**
     * Method used to register a {@link BankReferenceType}.<br>
     * I recommend calling during the {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent Common Setup Event}
     */
    public abstract void RegisterReferenceType(@Nonnull BankReferenceType type);


    /**
     * @deprecated Use {@link #RegisterReferenceType(BankReferenceType)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.1.1")
    public static void registerType(@Nonnull BankReferenceType type) { API.RegisterReferenceType(type); }

    public abstract void RegisterBankAccountSource(@Nonnull BankAccountSource source);

    /**
     * Method used to get a registered {@link BankReferenceType} from the available map.
     * Used to load {@link BankReference} from NBT data.
     */
    @Nullable
    public abstract BankReferenceType GetReferenceType(@Nonnull ResourceLocation type);
    /**
     * @deprecated Use {@link #RegisterReferenceType(BankReferenceType)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.1.1")
    @Nullable
    public static BankReferenceType getType(@Nonnull ResourceLocation type) { return API.GetReferenceType(type); }

    public abstract List<IBankAccount> GetAllBankAccounts(boolean isClient);
    public abstract List<BankReference> GetAllBankReferences(boolean isClient);


    /**
     * Executes a bank deposit interaction.
     * This is the shortcut method called from the context of a player interacting with a {@link IBankAccountMenu} menu.
     * @param requestedAmount The amount the player has requested to deposit. If not enough funds are available, the largest available amount will be deposited.
     * @see #BankDeposit(Player, Container, BankReference, MoneyValue) for the non-shortcut version of the function.
     */
    public abstract void BankDeposit(@Nonnull IBankAccountMenu menu, @Nonnull MoneyValue requestedAmount);
    /**
     * Executes a bank deposit interaction.
     * @param player The player who is attempting to make the deposit.
     * @param container A container for items that could contain money. Used in {@link io.github.lightman314.lightmanscurrency.api.money.MoneyAPI#GetATMMoneyHandler(Player, Container)} to get the funds available for deposit.
     * @param account A {@link BankReference} for the bank account that the player wished to make the deposit to. Will be used to confirm that the player has access to the bank account before depositing via {@link BankReference#allowedAccess(Player)}.
     * @param requestedAmount The amount the player has requested to deposit. If not enough funds are available, the largest available amount will be deposited.
     */
    public abstract void BankDeposit(@Nonnull Player player, @Nonnull Container container, @Nonnull BankReference account, @Nonnull MoneyValue requestedAmount);

    /**
     * @deprecated Use {@link #BankDeposit(IBankAccountMenu, MoneyValue)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.1.1")
    public static void DepositCoins(@Nonnull IBankAccountMenu menu, @Nonnull MoneyValue amount) { API.BankDeposit(menu,amount); }

    /**
     * @deprecated Use {@link #BankDeposit(Player, Container, BankReference, MoneyValue)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.1.1")
    @SuppressWarnings("unused")
    public static void DepositCoins(@Nonnull Player player, @Nonnull Container coinInput, @Nonnull IBankAccount account, @Nonnull MoneyValue amount) { }

    /**
     * Called by admins/commands to forcibly create and deposit money into the given bank account.
     * @param account The bank account to deposit money into.
     * @param amount The amount of money to deposit.
     */
    public abstract boolean BankDepositFromServer(@Nonnull IBankAccount account, @Nonnull MoneyValue amount);
    /**
     * Called by admins/commands to forcibly withdraw and destroy money from the given bank account.
     * @param account The bank account to take money from.
     * @param amount The amount of money to take.
     */
    @Nonnull
    public abstract Pair<Boolean, MoneyValue> BankWithdrawFromServer(@Nonnull IBankAccount account, @Nonnull MoneyValue amount);
    /**
     * @deprecated Use {@link #BankDepositFromServer(IBankAccount, MoneyValue)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.1.1")
    public static boolean ServerGiveCoins(@Nonnull IBankAccount account, @Nonnull MoneyValue amount) {return API.BankDepositFromServer(account,amount); }

    /**
     * @deprecated Use {@link #BankWithdrawFromServer(IBankAccount, MoneyValue)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.1.1")
    public static Pair<Boolean, MoneyValue> ServerTakeCoins(@Nonnull IBankAccount account, MoneyValue amount) { return API.BankWithdrawFromServer(account,amount); }

    /**
     * Executes a bank withdraw interaction.
     * This is the shortcut method called from the context of a player interacting with a {@link IBankAccountMenu} menu.
     * @param amount The amount the player has requested to withdraw. If not enough funds are available, the largest available amount will be withdrawn.
     * @see #BankWithdraw(Player, Container, BankReference, MoneyValue) for the non-shortcut version of the function.
     */
    public abstract void BankWithdraw(@Nonnull IBankAccountMenu menu, @Nonnull MoneyValue amount);
    /**
     * Executes a bank withdraw interaction.
     * @param player The player who is attempting to make the withdrawl.
     * @param container A container for items that could contain money. Used in {@link io.github.lightman314.lightmanscurrency.api.money.MoneyAPI#GetATMMoneyHandler(Player, Container)} to get the funds available for deposit.
     * @param account A {@link BankReference} for the bank account that the player wished to make the deposit to. Will be used to confirm that the player has access to the bank account before depositing via {@link BankReference#allowedAccess(Player)}.
     * @param amount The amount the player has requested to withdraw. If not enough funds are available, the largest available amount will be withdrawn.
     */
    public abstract void BankWithdraw(@Nonnull Player player, @Nonnull Container container, @Nonnull BankReference account, @Nonnull MoneyValue amount);

    /**
     * @deprecated Use {@link #BankWithdraw(IBankAccountMenu, MoneyValue)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.1.1")
    public static void WithdrawCoins(@Nonnull IBankAccountMenu menu, @Nonnull MoneyValue amount) { API.BankWithdraw(menu,amount); }
    /**
     * @deprecated Use {@link #BankWithdraw(Player,Container,BankReference, MoneyValue)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.1.1")
    @SuppressWarnings("unused")
    public static void WithdrawCoins(@Nonnull Player player, @Nonnull Container coinOutput, @Nonnull IBankAccount account, @Nonnull MoneyValue amount) { }

    /**
     * Executes a bank account money transfer interaction.
     * This is the shortcut method called from the context of a player interacting with a {@link IBankAccountAdvancedMenu} menu.
     * @param amount The amount the player has requested to transfer. If not enough funds are available, the largest available amount will be transferred.
     * @return A message sent as feedback to the player who initiated the transfer.
     * @see #BankTransfer(Player, BankReference, MoneyValue, IBankAccount) for the non-shortcut version of the function.
     */
    @Nonnull
    public abstract MutableComponent BankTransfer(@Nonnull IBankAccountAdvancedMenu menu, @Nonnull MoneyValue amount, @Nonnull IBankAccount destination);
    /**
     * Executes a bank withdraw interaction from the given player.
     * @param player The player who is attempting to make the withdrawl.
     * @param fromAccount The bank account that money will be transferred from. Will be used to confirm that the player has access to the bank account before depositing via {@link BankReference#allowedAccess(Player)}.
     * @param amount The amount the player has requested to transfer. If not enough funds are available, the largest available amount will be transferred.
     * @param destination The bank account that money will be transferred to. Does not require that the player has access to this account as this will be purely beneficial to the target.
     * @return A message sent as feedback to the player who initiated the transfer.
     */
    @Nonnull
    public abstract MutableComponent BankTransfer(@Nonnull Player player, @Nonnull BankReference fromAccount, @Nonnull MoneyValue amount, @Nonnull IBankAccount destination);

    /**
     * @deprecated Use {@link #BankTransfer(IBankAccountAdvancedMenu, MoneyValue, IBankAccount)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.1.1")
    public static MutableComponent TransferCoins(@Nonnull IBankAccountAdvancedMenu menu, @Nonnull MoneyValue amount, @Nonnull BankReference destination) { return TransferCoins(menu.getPlayer(), menu.getBankAccount(), amount, destination == null ? null : destination.get()); }

    /**
     * @deprecated Use {@link #BankTransfer(Player,BankReference, MoneyValue, IBankAccount)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.1.1")
    @SuppressWarnings("unused")
    public static MutableComponent TransferCoins(@Nonnull Player player, @Nonnull IBankAccount fromAccount, @Nonnull MoneyValue amount, @Nonnull IBankAccount destinationAccount) { return EasyText.literal("Outdated API usage!"); }


}