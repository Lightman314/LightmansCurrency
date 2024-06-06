package io.github.lightman314.lightmanscurrency.api.money.bank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public interface IBankAccount extends IMoneyHolder {


    /**
     * Direct access to the bank accounts money storage.<br>
     * Should only be accessed and interacted with if your certain that the player/machine is allowed to deposit/withdraw money from this account.
     */
    @Nonnull
    MoneyStorage getMoneyStorage();

    /**
     * The name of the bank account.<br>
     * Typically, returns text along the lines of <code><b>USER's Bank Account</b></code>
     */
    @Nonnull
    MutableComponent getName();


    //Low Balance Notification

    /**
     * A map of all Low-Balance notification levels.
     */
    @Nonnull
    Map<String,MoneyValue> getNotificationLevels();

    /**
     * The Low-Balance notification level for the given {@link MoneyValue#getUniqueName() Money Type}<br>
     * If none is defined, will return {@link MoneyValue#empty()}
     */
    @Nonnull
    MoneyValue getNotificationLevelFor(@Nonnull String type);

    /**
     * Defines the Low-Balance notification level for the given {@link MoneyValue#getUniqueName() Money Type}<br>
     * If an empty value is given, it will clear that money type from having a Low-Balance notification.
     */
    void setNotificationLevel(@Nonnull String type, @Nonnull MoneyValue level);

    //Notification System

    /**
     * Stores the {@link Notification} in the Bank Accounts local logger.
     */
    void pushLocalNotification(@Nonnull Notification notification);

    /**
     * Pushes the given {@link Notification} to the Bank Accounts local logger <b>AND</b> to all relevant players.
     */
    default void pushNotification(@Nonnull NonNullSupplier<Notification> notification) { this.pushNotification(notification,true); }
    /**
     * Pushes the given {@link Notification} to the Bank Accounts local logger.<br>
     * @param notifyPlayers Whether to also push the notification to all relevant players.
     */
    void pushNotification(@Nonnull NonNullSupplier<Notification> notification, boolean notifyPlayers);

    /**
     * All {@link Notification Notifications} stored on the Bank Accounts local logger.
     */
    @Nonnull
    List<Notification> getNotifications();

    @Nonnull
    default Component getBalanceText() { return LCText.GUI_BANK_BALANCE.get(this.getMoneyStorage().getRandomValueText()); }


    /**
     * Adds the given {@link MoneyValue amount} to the bank accounts money storage.
     */
    void depositMoney(@Nonnull MoneyValue amount);

    /**
     * Withdraws the given {@link MoneyValue amount} from the bank accounts money storage.<br>
     * If the requested amount is not available, it with instead withdraw as much as possible.<br>
     * Will automatically trigger a Low-Balance notification if this withdrawl pushes the accounts balance below the defined notification level for this {@link MoneyValue#getUniqueName() Money Type}.
     * @return The {@link MoneyValue amount} successfully withdrawn from the bank account.
     */
    @Nonnull
    MoneyValue withdrawMoney(@Nonnull MoneyValue withdrawAmount);

    /**
     * Applies interest to all money contained in this bank account.
     * @param interestRate The multiplier-based interest to be applied to the money total.
     * @param limits A list of upper limits of money that can be earned from interest.
     */
    void applyInterest(double interestRate, @Nonnull List<MoneyValue> limits, boolean forceInterst, boolean notifyPlayers);

}
