package io.github.lightman314.lightmanscurrency.api.money.bank;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.money.bank.source.BankAccountSource;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.impl.BankAPIImpl;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
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
     * I recommend calling during the {@link net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent Common Setup Event}
     */
    public abstract void RegisterReferenceType(@Nonnull BankReferenceType type);

    public abstract void RegisterBankAccountSource(@Nonnull BankAccountSource source);

    /**
     * Method used to get a registered {@link BankReferenceType} from the available map.
     * Used to load {@link BankReference} from NBT data.
     */
    @Nullable
    public abstract BankReferenceType GetReferenceType(@Nonnull ResourceLocation type);

    @Nonnull
    public abstract List<IBankAccount> GetAllBankAccounts(boolean isClient);
    @Nonnull
    public final List<IBankAccount> GetAllBankAccounts(@Nonnull IClientTracker context) { return this.GetAllBankAccounts(context.isClient()); }
    @Nonnull
    public abstract List<BankReference> GetAllBankReferences(boolean isClient);
    @Nonnull
    public final List<BankReference> GetAllBankReferences(@Nonnull IClientTracker context) { return this.GetAllBankReferences(context.isClient()); }


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
     * Called by admins/commands to forcibly create and deposit money into the given bank account.
     * @param account The bank account to deposit money into.
     * @param amount The amount of money to deposit.
     */
    public final boolean BankDepositFromServer(@Nonnull IBankAccount account, @Nonnull MoneyValue amount) { return BankDepositFromServer(account, amount, true); }
    /**
     * Called by admins/commands to forcibly create and deposit money into the given bank account.
     * @param account The bank account to deposit money into.
     * @param amount The amount of money to deposit.
     * @param notifyPlayers Whether the owner of the bank account should have the deposit notification pushed to their personal notifications.
     */
    public abstract boolean BankDepositFromServer(@Nonnull IBankAccount account, @Nonnull MoneyValue amount, boolean notifyPlayers);
    /**
     * Called by admins/commands to forcibly withdraw and destroy money from the given bank account.
     * @param account The bank account to take money from.
     * @param amount The amount of money to take.
     */
    @Nonnull
    public final Pair<Boolean, MoneyValue> BankWithdrawFromServer(@Nonnull IBankAccount account, @Nonnull MoneyValue amount) { return this.BankWithdrawFromServer(account, amount, true); }
    /**
     * Called by admins/commands to forcibly withdraw and destroy money from the given bank account.
     * @param account The bank account to take money from.
     * @param amount The amount of money to take.
     * @param notifyPlayers Whether the owner of the bank account should have the withdrawl notification pushed to their personal notifications.
     */
    @Nonnull
    public abstract Pair<Boolean, MoneyValue> BankWithdrawFromServer(@Nonnull IBankAccount account, @Nonnull MoneyValue amount, boolean notifyPlayers);
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
    public abstract MutableComponent BankTransfer(@Nonnull Player player, BankReference fromAccount, @Nonnull MoneyValue amount, IBankAccount destination);

}
