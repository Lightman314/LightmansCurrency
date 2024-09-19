package io.github.lightman314.lightmanscurrency.common.items.experimental;

import io.github.lightman314.lightmanscurrency.api.capability.money.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.DepositWithdrawNotification;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ATMCardMoneyHandler extends MoneyHandler implements ISidedObject {

    private boolean isClient = false;
    @Override
    public boolean isClient() { return this.isClient; }

    private final ItemStack card;

    public ATMCardMoneyHandler(@Nonnull ItemStack card) { this.card = card; }

    private MutableComponent getCardName() { return EasyText.makeMutable(this.card.getHoverName()); }

    @Nonnull
    @Override
    public ATMCardMoneyHandler flagAsClient() { this.isClient = true; return this; }
    @Nonnull
    @Override
    public ATMCardMoneyHandler flagAsClient(boolean isClient) { this.isClient = isClient; return this; }
    @Nonnull
    @Override
    public ATMCardMoneyHandler flagAsClient(@Nonnull IClientTracker tracker) { this.isClient = tracker.isClient(); return this; }

    @Nullable
    protected IBankAccount getAccount()
    {
        ATMCardData data = this.card.getOrDefault(ModDataComponents.ATM_CARD_DATA, ATMCardData.EMPTY);
        BankReference reference = data.getBankReference(this);
        if(reference != null)
        {
            IBankAccount account = reference.get();
            if(account != null && account.isCardValid(data.validation()))
                return account;
        }
        return null;
    }

    @Nonnull
    @Override
    public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation)
    {
        IBankAccount account = this.getAccount();
        if(account != null)
        {
            if(!simulation)
            {
                account.depositMoney(insertAmount);
                account.pushLocalNotification(new DepositWithdrawNotification.Custom(this.getCardName(),account.getName(),true,insertAmount));
            }
            return MoneyValue.empty();
        }
        return insertAmount;
    }

    @Nonnull
    @Override
    public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) {
        IBankAccount account = this.getAccount();
        if(account != null)
        {
            MoneyValue result = account.getMoneyStorage().extractMoney(extractAmount,simulation);
            MoneyValue amountTaken = extractAmount.subtractValue(result);
            if(!amountTaken.isEmpty() && !simulation)
                account.pushLocalNotification(new DepositWithdrawNotification.Custom(this.getCardName(),account.getName(),false,amountTaken));
            return result;
        }
        return extractAmount;
    }

    @Override
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) { return true; }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        IBankAccount account = this.getAccount();
        if(account != null)
            builder.merge(account.getMoneyStorage());

    }

}
