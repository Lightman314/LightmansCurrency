package io.github.lightman314.lightmanscurrency.integration.impactor.money;

import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.integration.impactor.LCImpactorCompat;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.impactdev.impactor.api.economy.transactions.EconomyTransaction;
import net.impactdev.impactor.core.economy.EconomyConfig;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class ImpactorPlayerMoneyProvider implements IPlayerMoneyHandler {

    private Player player;
    public ImpactorPlayerMoneyProvider(Player player) { this.player = player; }
    @Override
    public void updatePlayer(@Nonnull Player player) { this.player = player; }

    private static BigDecimal getMinBalance() {
        if(LCImpactorCompat.getConfigValue(EconomyConfig.APPLY_RESTRICTIONS).orElse(false))
            return LCImpactorCompat.getConfigValue(EconomyConfig.MIN_BALANCE).orElse(BigDecimal.ZERO);
        return BigDecimal.valueOf(Double.NEGATIVE_INFINITY);
    }

    private static BigDecimal getMaxBalance() {
        if(LCImpactorCompat.getConfigValue(EconomyConfig.APPLY_RESTRICTIONS).orElse(false))
            return LCImpactorCompat.getConfigValue(EconomyConfig.MAX_BALANCE).orElse(BigDecimal.valueOf(Double.MAX_VALUE));
        return BigDecimal.valueOf(Double.MAX_VALUE);
    }

    @Nonnull
    @Override
    public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) {
        if(insertAmount instanceof ImpactorMoneyValue value)
        {
            Account account = LCImpactorCompat.getPlayerAccount(this.player,value.getImpactorCurrency());
            if(account == null)
                return insertAmount;
            if(simulation)
            {
                BigDecimal totalCount = account.balance().add(value.getValue());
                BigDecimal maxBalance = getMaxBalance();
                if(maxBalance.compareTo(totalCount) < 0)
                {
                    //If max balance is less than the total deposited amount, assume we're unable to deposit the rest
                    BigDecimal leftovers = totalCount.subtract(maxBalance);
                    return ImpactorMoneyValue.of(value.getImpactorCurrency(),leftovers);
                }
                //Otherwise, it'll all fit and we've deposited everything
                return MoneyValue.empty();
            }
            BigDecimal amountToDeposit = value.getValue();
            BigDecimal maxBalance = getMaxBalance();
            if(account.balance().add(amountToDeposit).compareTo(maxBalance) > 0)
            {
                //If the added amount is greater than the max balance, only deposit the amount that will fit
                amountToDeposit = maxBalance.subtract(account.balance());
                if(amountToDeposit.compareTo(BigDecimal.ZERO) <= 0)
                {
                    //If amount to deposit is less than or equal to zero, we can't deposit anything so abort mission
                    return insertAmount;
                }
            }
            EconomyTransaction result = account.deposit(amountToDeposit);
            if(result.successful())
            {
                BigDecimal deposited = result.amount();
                BigDecimal didntFit = value.getValue().subtract(deposited);
                if(didntFit.compareTo(BigDecimal.ZERO) > 0)
                {
                    //Return the amount unable to be deposited
                    return ImpactorMoneyValue.of(value.getImpactorCurrency(),didntFit);
                }
                else
                    return MoneyValue.empty();
            }
        }
        return insertAmount;
    }

    @Nonnull
    @Override
    public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) {
        if(extractAmount instanceof ImpactorMoneyValue value)
        {
            Account account = LCImpactorCompat.getPlayerAccount(this.player,value.getImpactorCurrency());
            if(account == null)
                return extractAmount;
            if(simulation)
            {
                BigDecimal balance = account.balance().subtract(getMinBalance());
                BigDecimal extraFunds = balance.subtract(value.getValue());
                if(extraFunds.compareTo(BigDecimal.ZERO) < 0)
                {
                    //If value is negative, make it positive and return it as the amout we were unable to withdraw
                    return ImpactorMoneyValue.of(value.getImpactorCurrency(),extraFunds.negate());
                }
                else
                    return MoneyValue.empty();
            }
            BigDecimal withdrawAmount = value.getValue();
            BigDecimal balance = account.balance().subtract(value.getValue());
            if(value.getValue().compareTo(balance) > 0)
            {
                //If funds to take is greater than the current balance, only take what we can
                withdrawAmount = balance;
            }
            EconomyTransaction transaction = account.withdraw(withdrawAmount);
            if(transaction.successful())
            {
                BigDecimal amountTaken = transaction.amount();
                BigDecimal couldntTake = value.getValue().subtract(amountTaken);
                if(couldntTake.compareTo(BigDecimal.ZERO) > 0)
                {
                    //Return the amount unable to be withdrawn
                    return ImpactorMoneyValue.of(value.getImpactorCurrency(),couldntTake);
                }
                else
                    return MoneyValue.empty();
            }
        }
        return extractAmount;
    }

    @Override
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) { return value instanceof ImpactorMoneyValue; }

    @Nonnull
    @Override
    public MoneyView getStoredMoney() {
        MoneyView.Builder builder = MoneyView.builder();
        for(Currency currency : EconomyService.instance().currencies().registered())
        {
            Account account = LCImpactorCompat.getPlayerAccount(this.player,currency);
            if(account.balance().compareTo(BigDecimal.ZERO) > 0)
                builder.add(ImpactorMoneyValue.of(currency,account.balance()));
        }
        return builder.build();
    }
}