package io.github.lightman314.lightmanscurrency.features.atm.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.BankInteractionNotification;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

public class AccountInteractionTab extends ATMTab {

    public static final Identifier CLIENT_KEY = LCApi.id("account_interaction");

    public static final TextEntry TOOLTIP = TextEntry.tooltip(LCApi.MODID,"atm.interact");
    public static final TextEntry GUI_NO_SELECTED_ACCOUNT = TextEntry.button(LCApi.MODID,"atm.interaction.no_selection");
    public static final TextEntry BUTTON_DEPOSIT = TextEntry.button(LCApi.MODID,"atm.interaction.deposit");
    public static final TextEntry BUTTON_WITHDRAW = TextEntry.button(LCApi.MODID,"atm.interaction.withdraw");

    public AccountInteractionTab(ATMMenu menu) { super(menu); }

    @Override
    public boolean usesMoneySlots() { return true; }

    @Override
    public Identifier getClientTabKey() { return CLIENT_KEY; }

    public void depositMoney(MoneyValue amount) {
        if(amount == null)
            return;
        if(this.isClient()) {
            this.send(FancyPacketMap.map().set("depositMoney",LCFancyPacketTypes.MONEY,amount));
            return;
        }
        BankAccount account = this.getAccount();
        if(account == null)
            return;
        //Collect the data on what we should attempt to deposit, and what money we have available
        MoneyResourceHandler depositResources;
        List<MoneyValue> toDeposit;
        //If no deposit amount defined, deposit all money currently contained within
        if(amount.isEmpty()) {
            depositResources = this.getMoneyResource();
            toDeposit = depositResources.getAllResources();
        }
        else {
            depositResources = this.getMoneyAndPlayerResources();
            toDeposit = List.of(amount);
        }
        for(MoneyValue deposit : toDeposit) {
            try(Transaction transaction = Transaction.openRoot()) {
                MoneyValue taken = depositResources.extract(deposit,transaction);
                if(!taken.isEmpty()) {
                    MoneyValue given = account.insert(taken,transaction);
                    if(given.equals(taken)) {
                        //Commit the transaction
                        transaction.commit();
                        //Log the interaction
                        account.pushLocalNotification(BankInteractionNotification.forPlayer(account.getName(),true,taken,PlayerReference.of(this.getPlayer())));
                    }
                }
            }
        }

    }

    public void withdrawMoney(MoneyValue amount) {
        if(amount == null)
            return;
        if(this.isClient()) {
            this.send(FancyPacketMap.map().set("withdrawMoney",LCFancyPacketTypes.MONEY,amount));
            return;
        }
        BankAccount account = this.getAccount();
        if(account == null || amount.isEmpty())
            return;
        try(Transaction transaction = Transaction.openRoot()) {
            MoneyValue taken = account.withdrawMoney(amount,transaction);
            if(!taken.isEmpty()) {
                MoneyValue given = this.getPlayerAndMoneyResources().insert(taken,transaction);
                if(given.equals(taken)) {
                    //Commit the transaction
                    transaction.commit();
                    //Log the interaction
                    account.pushLocalNotification(BankInteractionNotification.forPlayer(account.getName(),false,taken,PlayerReference.of(this.getPlayer())));
                }
            }
        }
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("depositMoney"))
            this.depositMoney(message.get("depositMoney",LCFancyPacketTypes.MONEY));
        if(message.contains("withdrawMoney"))
            this.withdrawMoney(message.get("withdrawMoney",LCFancyPacketTypes.MONEY));

    }
}
