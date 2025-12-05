package io.github.lightman314.lightmanscurrency.api.money.bank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.CustomTarget;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IBankAccount extends IMoneyHolder, IClientTracker {

    int SALARY_LIMIT = 100;

    /**
     * Direct access to the bank accounts money storage.<br>
     * Should only be accessed and interacted with if your certain that the player/machine is allowed to deposit/withdraw money from this account.
     */
    MoneyStorage getMoneyStorage();

    /**
     * The current card validation level required for a bank card to properly link to this account
     */
    int getCardValidation();

    /**
     * Whether a bank card with the given validation level is allowed to access this account
     */
    boolean isCardValid(int validationLevel);

    /**
     * Increments the required {@link #getCardValidation() Validation Level} for this bank account, making all existing Bank Cards linked to this account invalid
     */
    void resetCards();

    /**
     * The name of the bank account.<br>
     * Typically, returns text along the lines of <code><b>USER's Bank Account</b></code>
     */
    MutableComponent getName();

    /**
     * The name of the bank accounts owner.<br>
     * Should not be used for display purposes, but more as a way to format sub-sections of the bank account in a less wordy manner (such as <code><b>USER's Salary #1</b></code>
     */
    Component getOwnerName();


    //Low Balance Notification
    /**
     * A map of all Low-Balance notification levels.
     */
    Map<String,MoneyValue> getNotificationLevels();

    /**
     * The Low-Balance notification level for the given {@link MoneyValue#getUniqueName() Money Type}<br>
     * If none is defined, will return {@link MoneyValue#empty()}
     */
    MoneyValue getNotificationLevelFor(String type);

    /**
     * Defines the Low-Balance notification level for the given {@link MoneyValue#getUniqueName() Money Type}<br>
     * If an empty value is given, it will clear that money type from having a Low-Balance notification.
     */
    void setNotificationLevel(String type, MoneyValue level);

    //Notification System

    /**
     * Stores the {@link Notification} in the Bank Accounts local logger.
     */
    void pushLocalNotification(Notification notification);

    /**
     * Pushes the given {@link Notification} to the Bank Accounts local logger <b>AND</b> to all relevant players.
     */
    default void pushNotification(Supplier<Notification> notification) { this.pushNotification(notification,true); }
    /**
     * Pushes the given {@link Notification} to the Bank Accounts local logger.<br>
     * @param notifyPlayers Whether to also push the notification to all relevant players.
     */
    void pushNotification(Supplier<Notification> notification, boolean notifyPlayers);

    /**
     * All {@link Notification Notifications} stored on the Bank Accounts local logger.
     */

    List<Notification> getNotifications();

    default MutableComponent getBalanceText() { return LCText.GUI_BANK_BALANCE.get(this.getMoneyStorage().getRandomValueText()); }


    /**
     * Adds the given {@link MoneyValue amount} to the bank accounts money storage.
     */
    void depositMoney(MoneyValue amount);

    /**
     * Withdraws the given {@link MoneyValue amount} from the bank accounts money storage.<br>
     * If the requested amount is not available, it with instead withdraw as much as possible.<br>
     * Will automatically trigger a Low-Balance notification if this withdrawl pushes the accounts balance below the defined notification level for this {@link MoneyValue#getUniqueName() Money Type}.
     * @return The {@link MoneyValue amount} successfully withdrawn from the bank account.
     */

    MoneyValue withdrawMoney(MoneyValue withdrawAmount);

    /**
     * Applies interest to all money contained in this bank account.
     * @param interestRate The multiplier-based interest to be applied to the money total.
     * @param limits A list of upper limits of money that can be earned from interest.
     */
    void applyInterest(double interestRate, List<MoneyValue> limits, List<String> blacklist, boolean forceInterst, boolean notifyPlayers);

    List<SalaryData> getSalaries();

    default void checkForOnlinePlayers() {
        for(SalaryData s : this.getSalaries())
            s.checkForOnlinePlayers(true);
    }

    Map<String, CustomTarget> extraSalaryTargets();

    void deleteSalary(SalaryData salary);

    @Nullable
    StatTracker getStatTracker();

    void markDirty();

    void tick();

    default void onPlayerJoined(ServerPlayer player)
    {
        for(SalaryData salary : new ArrayList<>(this.getSalaries()))
            salary.onPlayerJoin(player);
    }

}