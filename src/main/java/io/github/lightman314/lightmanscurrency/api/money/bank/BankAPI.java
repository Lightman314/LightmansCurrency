package io.github.lightman314.lightmanscurrency.api.money.bank;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.DepositWithdrawNotification;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BankAPI {

    private static final Map<ResourceLocation, BankReferenceType> TYPES = new HashMap<>();

    /**
     * Method used to register a {@link BankReferenceType}.<br>
     * I recommend calling during the {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent Common Setup Event}
     */
    public static void registerType(@Nonnull BankReferenceType type)
    {
        ResourceLocation id = type.id;
        if(TYPES.containsKey(id))
            LightmansCurrency.LogWarning("Attempted to registerNotification the AccountReferenceType '" + id + "' twice!");
        else
        {
            TYPES.put(id, type);
            LightmansCurrency.LogDebug("Registered BankReferenceType '" + id + "'!");
        }
    }

    @Nullable
    public static BankReferenceType getType(@Nonnull ResourceLocation type) { return TYPES.get(type); }

    public static void DepositCoins(@Nonnull IBankAccountMenu menu, @Nonnull MoneyValue amount)
    {
        if(menu == null)
            return;
        DepositCoins(menu.getPlayer(), menu.getCoinInput(), menu.getBankAccount(), amount);
    }

    public static void DepositCoins(@Nonnull Player player, @Nonnull Container coinInput, @Nonnull IBankAccount account, @Nonnull MoneyValue amount)
    {
        if(account == null)
            return;

        MoneyView valueOfContainer = MoneyAPI.valueOfContainer(coinInput);
        for(MoneyValue value : valueOfContainer.allValues())
        {
            if(value.sameType(amount))
            {
                MoneyValue depositAmount = amount;
                if(depositAmount.isEmpty() || !value.containsValue(depositAmount))
                    depositAmount = value;
                //Take the money from the container
                MoneyAPI.takeMoneyFromContainer(coinInput, player, depositAmount);
                //Add the money to the bank account
                account.depositMoney(depositAmount);
                if(account instanceof BankAccount ba)
                    ba.LogInteraction(player, depositAmount, true);
                return;
            }
        }

    }

    public static boolean ServerGiveCoins(@Nonnull IBankAccount account, @Nonnull MoneyValue amount)
    {
        if(account == null || amount.isEmpty())
            return false;

        account.depositMoney(amount);
        account.pushNotification(() -> new DepositWithdrawNotification.Server(account.getName(), true, amount));
        return true;
    }

    public static Pair<Boolean, MoneyValue> ServerTakeCoins(@Nonnull IBankAccount account, MoneyValue amount)
    {
        if(account == null || amount.isEmpty())
            return Pair.of(false, MoneyValue.empty());

        MoneyValue taken = account.withdrawMoney(amount);
        account.pushNotification(() -> new DepositWithdrawNotification.Server(account.getName(), false, taken));
        return Pair.of(true, taken);
    }

    public static void WithdrawCoins(@Nonnull IBankAccountMenu menu, @Nonnull MoneyValue amount)
    {
        if(menu == null)
            return;
        WithdrawCoins(menu.getPlayer(), menu.getCoinInput(), menu.getBankAccount(), amount);
    }

    public static void WithdrawCoins(@Nonnull Player player, @Nonnull Container coinOutput, @Nonnull IBankAccount account, @Nonnull MoneyValue amount)
    {
        if(account == null || amount.isEmpty())
            return;

        MoneyValue withdrawnAmount = account.withdrawMoney(amount);

        CurrencyType currencyType = withdrawnAmount.getCurrency();
        if(currencyType == null)
        {
            account.depositMoney(withdrawnAmount);
            return;
        }
        MoneyAPI.addMoneyToContainer(coinOutput, player, withdrawnAmount);
        if(account instanceof BankAccount ba)
            ba.LogInteraction(player, withdrawnAmount, false);
    }

    public static MutableComponent TransferCoins(@Nonnull IBankAccountAdvancedMenu menu, @Nonnull MoneyValue amount, @Nonnull BankReference destination)
    {
        return TransferCoins(menu.getPlayer(), menu.getBankAccount(), amount, destination == null ? null : destination.get());
    }

    public static MutableComponent TransferCoins(@Nonnull Player player, @Nonnull IBankAccount fromAccount, @Nonnull MoneyValue amount, @Nonnull IBankAccount destinationAccount)
    {
        if(fromAccount == null)
            return EasyText.translatable("gui.bank.transfer.error.null.from");
        if(destinationAccount == null)
            return EasyText.translatable("gui.bank.transfer.error.null.to");
        if(amount.isEmpty())
            return EasyText.translatable("gui.bank.transfer.error.amount", amount.getString("nothing"));
        if(fromAccount == destinationAccount)
            return EasyText.translatable("gui.bank.transfer.error.same");

        MoneyValue withdrawnAmount = fromAccount.withdrawMoney(amount);
        if(withdrawnAmount.isEmpty())
            return EasyText.translatable("gui.bank.transfer.error.nobalance", amount.getString());

        destinationAccount.depositMoney(withdrawnAmount);
        if(fromAccount instanceof BankAccount ba)
            ba.LogTransfer(player, withdrawnAmount, destinationAccount.getName(), false);
        if(destinationAccount instanceof BankAccount ba)
            ba.LogTransfer(player, withdrawnAmount, fromAccount.getName(), true);

        return EasyText.translatable("gui.bank.transfer.success", withdrawnAmount.getString(), destinationAccount.getName());

    }


}
