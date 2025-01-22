package io.github.lightman314.lightmanscurrency.common.impl;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.money.bank.source.BankAccountSource;
import io.github.lightman314.lightmanscurrency.api.money.bank.source.builtin.PlayerBankAccountSource;
import io.github.lightman314.lightmanscurrency.api.money.bank.source.builtin.TeamBankAccountSource;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.data.types.BankDataCache;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.DepositWithdrawNotification;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankAPIImpl extends BankAPI {

    public static final BankAPIImpl INSTANCE = new BankAPIImpl();

    private BankAPIImpl() {
        NeoForge.EVENT_BUS.register(this);
        //Register built-in bank account sources
        this.RegisterBankAccountSource(PlayerBankAccountSource.INSTANCE);
        this.RegisterBankAccountSource(TeamBankAccountSource.INSTANCE);
    }

    private final Map<ResourceLocation, BankReferenceType> referenceTypes = new HashMap<>();
    private final List<BankAccountSource> accountSources = new ArrayList<>();

    @Override
    public void RegisterReferenceType(@Nonnull BankReferenceType type) {
        ResourceLocation id = type.id;
        if(this.referenceTypes.containsKey(id))
            LightmansCurrency.LogWarning("Attempted to register the AccountReferenceType '" + id + "' twice!");
        else
        {
            this.referenceTypes.put(id, type);
            LightmansCurrency.LogDebug("Registered BankReferenceType '" + id + "'!");
        }
    }

    @Nullable
    @Override
    public BankReferenceType GetReferenceType(@Nonnull ResourceLocation type) { return this.referenceTypes.get(type); }

    @Override
    public void RegisterBankAccountSource(@Nonnull BankAccountSource source) {
        if(this.accountSources.contains(source))
        {
            LightmansCurrency.LogWarning("Bank Account Source of type " + source.getClass().getSimpleName() + " was already registered!");
            return;
        }
        this.accountSources.add(source);
    }

    @Override
    @Nonnull
    public List<BankReference> GetAllBankReferences(boolean isClient) {
        List<BankReference> references = new ArrayList<>();
        for(BankAccountSource source : this.accountSources)
            references.addAll(source.CollectAllReferences(isClient));
        return references;
    }

    @Nonnull
    @Override
    public List<IBankAccount> GetAllBankAccounts(boolean isClient) {
        List<IBankAccount> accounts = new ArrayList<>();
        for(BankAccountSource source : this.accountSources)
            accounts.addAll(source.CollectAllBankAccounts(isClient));
        return accounts;
    }

    @Override
    public void BankDeposit(@Nonnull IBankAccountMenu menu, @Nonnull MoneyValue requestedAmount) { this.BankDeposit(menu.getPlayer(),menu.getCoinInput(),menu.getBankAccountReference(),requestedAmount); }

    @Override
    public void BankDeposit(@Nonnull Player player, @Nonnull Container container, @Nonnull BankReference reference, @Nonnull MoneyValue requestedAmount) {
        if(reference == null || !reference.allowedAccess(player))
            return;
        IBankAccount account = reference.get();
        if(account == null)
            return;

        IMoneyHandler handler = MoneyAPI.API.GetATMMoneyHandler(player,container);
        MoneyView availableFunds = handler.getStoredMoney();
        for(MoneyValue value : availableFunds.allValues())
        {
            if(value.sameType(requestedAmount))
            {
                MoneyValue depositAmount = requestedAmount;
                if(depositAmount.isEmpty() || !value.containsValue(depositAmount))
                    depositAmount = value;
                //Take the money from the container
                handler.extractMoney(depositAmount,false);
                //Add the money to the bank account
                account.depositMoney(depositAmount);
                if(account instanceof BankAccount ba)
                    ba.LogInteraction(player, depositAmount, true);
                return;
            }
        }
    }

    @Override
    public void BankWithdraw(@Nonnull IBankAccountMenu menu, @Nonnull MoneyValue amount) { this.BankWithdraw(menu.getPlayer(), menu.getCoinInput(), menu.getBankAccountReference(), amount); }

    @Override
    public void BankWithdraw(@Nonnull Player player, @Nonnull Container container, @Nonnull BankReference reference, @Nonnull MoneyValue amount) {
        if(reference == null || !reference.allowedAccess(player) || amount.isEmpty())
            return;
        IBankAccount account = reference.get();
        if(account == null)
            return;

        MoneyValue withdrawnAmount = account.withdrawMoney(amount);

        IMoneyHandler handler = MoneyAPI.API.GetATMMoneyHandler(player,container);
        if(!handler.insertMoney(withdrawnAmount,true).isEmpty())
        {
            //Abort the withdrawal if we can't give the withdrawn amount to the player.
            account.depositMoney(withdrawnAmount);
            return;
        }

        handler.insertMoney(withdrawnAmount,false);
        if(account instanceof BankAccount ba)
            ba.LogInteraction(player, withdrawnAmount, false);
    }

    @Nonnull
    @Override
    public MutableComponent BankTransfer(@Nonnull IBankAccountAdvancedMenu menu, @Nonnull MoneyValue amount, @Nonnull IBankAccount destination) { return this.BankTransfer(menu.getPlayer(), menu.getBankAccountReference(), amount, destination); }

    @Nonnull
    @Override
    public MutableComponent BankTransfer(@Nonnull Player player, BankReference fromReference, @Nonnull MoneyValue amount, IBankAccount destination) {
        if(fromReference == null)
            return LCText.GUI_BANK_TRANSFER_ERROR_NULL_FROM.get();
        if(!fromReference.allowedAccess(player))
            return LCText.GUI_BANK_TRANSFER_ERROR_ACCESS.get();
        IBankAccount fromAccount = fromReference.get();
        if(fromAccount == null)
            return LCText.GUI_BANK_TRANSFER_ERROR_NULL_FROM.get();
        if(destination == null)
            return LCText.GUI_BANK_TRANSFER_ERROR_NULL_TARGET.get();
        if(amount.isEmpty())
            return LCText.GUI_BANK_TRANSFER_ERROR_AMOUNT.get(amount.getText(LCText.GUI_MONEY_STORAGE_EMPTY.get()));

        if(fromAccount == destination)
            return LCText.GUI_BANK_TRANSFER_ERROR_SAME.get();


        MoneyValue withdrawnAmount = fromAccount.withdrawMoney(amount);
        if(withdrawnAmount.isEmpty())
            return LCText.GUI_BANK_TRANSFER_ERROR_NO_BALANCE.get(amount.getText());

        destination.depositMoney(withdrawnAmount);
        if(fromAccount instanceof BankAccount ba)
            ba.LogTransfer(player, withdrawnAmount, destination.getName(), false);
        if(destination instanceof BankAccount ba)
            ba.LogTransfer(player, withdrawnAmount, fromAccount.getName(), true);

        return LCText.GUI_BANK_TRANSFER_SUCCESS.get(withdrawnAmount.getText(), destination.getName());
    }

    @Override
    public boolean BankDepositFromServer(@Nonnull IBankAccount account, @Nonnull MoneyValue amount, boolean notifyPlayers) {
        if(account == null || amount.isEmpty())
            return false;

        account.depositMoney(amount);
        account.pushNotification(DepositWithdrawNotification.Server.create(account.getName(), true, amount), notifyPlayers);

        return true;
    }

    @Nonnull
    @Override
    public Pair<Boolean, MoneyValue> BankWithdrawFromServer(@Nonnull IBankAccount account, @Nonnull MoneyValue amount, boolean notifyPlayers) {
        if(account == null || amount.isEmpty())
            return Pair.of(false, MoneyValue.empty());

        MoneyValue taken = account.withdrawMoney(amount);
        account.pushNotification(DepositWithdrawNotification.Server.create(account.getName(), false, taken), notifyPlayers);
        return Pair.of(true, taken);
    }

    @SubscribeEvent
    public void ServerTick(@Nonnull ServerTickEvent.Pre event)
    {
        double interestRate = LCConfig.SERVER.bankAccountInterestRate.get();
        if(interestRate > 0)
        {
            BankDataCache data = BankDataCache.TYPE.get(false);
            int interest = data.interestTick();
            if(interest >= LCConfig.SERVER.bankAccountInterestTime.get())
            {
                data.resetInterestTick();
                LightmansCurrency.LogDebug("Applying interest to all bank accounts!");
                List<MoneyValue> limits = LCConfig.SERVER.bankAccountInterestLimits.get();
                boolean forceInterest = LCConfig.SERVER.bankAccountForceInterest.get();
                boolean notifyPlayers = LCConfig.SERVER.bankAccountInterestNotification.get();
                for(BankReference reference : this.GetAllBankReferences(false))
                {
                    IBankAccount account = reference.get();
                    if(account != null)
                    {
                        LightmansCurrency.LogDebug("Applying interest to " + account.getName().getString());
                        account.applyInterest(interestRate,limits,forceInterest,notifyPlayers);
                    }
                }
            }
        }
    }

}
