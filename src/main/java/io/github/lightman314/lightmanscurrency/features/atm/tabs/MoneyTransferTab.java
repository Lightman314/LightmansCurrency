package io.github.lightman314.lightmanscurrency.features.atm.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.BankTransferNotification;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class MoneyTransferTab extends ATMTab {

    public static final Identifier CLIENT_KEY = LCApi.id("account_transfer");

    public static final TextEntry TOOLTIP = TextEntry.tooltip(LCApi.MODID,"atm.transfer");
    public static final TextEntry TOOLTIP_TRANSFER_MODE_LIST = TextEntry.tooltip(LCApi.MODID,"atm.transfer.mode.list");
    public static final TextEntry TOOLTIP_TRANSFER_MODE_PLAYER = TextEntry.tooltip(LCApi.MODID,"atm.transfer.mode.player");
    public static final TextEntry TOOLTIP_TRANSFER_TRIGGER = TextEntry.tooltip(LCApi.MODID,"atm.transfer.trigger");

    public static final TextEntry GUI_TRANSFER_ERROR_NULL_SENDER = TextEntry.gui(LCApi.MODID,"atm.transfer.error.null.sender");
    public static final TextEntry GUI_TRANSFER_ERROR_NULL_TARGET = TextEntry.gui(LCApi.MODID,"atm.transfer.error.null.target");
    public static final TextEntry GUI_TRANSFER_ERROR_ACCESS = TextEntry.gui(LCApi.MODID,"atm.transfer.error.access");
    public static final TextEntry GUI_TRANSFER_ERROR_AMOUNT = TextEntry.gui(LCApi.MODID,"atm.transfer.error.amount");
    public static final TextEntry GUI_TRANSFER_ERROR_SAME = TextEntry.gui(LCApi.MODID,"atm.transfer.error.same");
    public static final TextEntry GUI_TRANSFER_ERROR_NO_BALANCE = TextEntry.gui(LCApi.MODID,"atm.transfer.error.no_balance");
    public static final TextEntry GUI_TRANSFER_SUCCESS = TextEntry.gui(LCApi.MODID,"atm.transfer.success");

    public MoneyTransferTab(ATMMenu menu) {
        super(menu);
    }

    @Override
    public boolean usesMoneySlots() { return false; }
    @Override
    public Identifier getClientTabKey() { return CLIENT_KEY; }

    public void transferToAccount(MoneyValue amount,BankReference target) {
        if(this.isClient()) {
            this.send(FancyPacketMap.map().setMap("transferMoney",FancyPacketMap.map()
                    .set("amount",LCFancyPacketTypes.MONEY,amount)
                    .set("target",LCFancyPacketTypes.BANK_REFERENCE,target)));
            return;
        }
        BankReference selected = this.getSelectedAccount();
        if(selected.equals(target)) {
            this.sendTransferResponse(GUI_TRANSFER_ERROR_SAME.get());
            return;
        }
        if(!selected.allowedAccess(this.getPlayer())) {
            this.sendTransferResponse(GUI_TRANSFER_ERROR_ACCESS.get());
            return;
        }
        BankAccount originalAccount = selected.get();
        if(originalAccount == null) {
            this.sendTransferResponse(GUI_TRANSFER_ERROR_NULL_SENDER.get());
            return;
        }
        BankAccount targetAccount = target.get();
        if(targetAccount == null) {
            this.sendTransferResponse(GUI_TRANSFER_ERROR_NULL_TARGET.get());
            return;
        }
        if(targetAccount == originalAccount) {
            this.sendTransferResponse(GUI_TRANSFER_ERROR_SAME.get());
            return;
        }
        if(amount.isEmpty()) {
            this.sendTransferResponse(GUI_TRANSFER_ERROR_AMOUNT.get());
            return;
        }

        //Attempt the transfer
        try(Transaction transaction = Transaction.openRoot()) {
            MoneyValue taken = originalAccount.withdrawMoney(amount,transaction);
            if(!taken.isEmpty()) {
                MoneyValue given = targetAccount.insert(taken,transaction);
                if(given.equals(taken)) {
                    //Commit the Transaction
                    transaction.commit();
                    //Create Transfer Notifications
                    PlayerReference pr = PlayerReference.of(this.getPlayer());
                    Component sendingName = originalAccount.getName();
                    Component receivingName = targetAccount.getName();
                    originalAccount.pushLocalNotification(BankTransferNotification.sentNotification(pr,given,sendingName,receivingName));
                    targetAccount.pushLocalNotification(BankTransferNotification.receivedNotification(pr,given,sendingName,receivingName));
                    this.sendTransferResponse(GUI_TRANSFER_SUCCESS.get(given.getText(),targetAccount.getName()));
                }
            }
            else
                this.sendTransferResponse(GUI_TRANSFER_ERROR_NO_BALANCE.get(amount.getText()));
        }
    }

    public void transferToPlayer(MoneyValue amount,String playerName) {
        if(this.isClient()) {
            this.send(FancyPacketMap.map().setMap("transferMoneyToPlayer",FancyPacketMap.map()
                    .set("amount",LCFancyPacketTypes.MONEY,amount)
                    .setString("target",playerName)));
            return;
        }
        PlayerReference pr = PlayerReference.of(this,playerName);
        if(pr != null) {
            BankReference target = PlayerBankReference.of(pr).setSidedContext(this);
            this.transferToAccount(amount,target);
        }
    }

    private void sendTransferResponse(Component text) {
        this.sendToClient(FancyPacketMap.map().setText("transferResponse",text));
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("transferMoney")) {
            FancyPacketMap entry = message.getMap("transferMoney");
            MoneyValue amount = entry.get("amount",LCFancyPacketTypes.MONEY);
            BankReference target = entry.get("target",LCFancyPacketTypes.BANK_REFERENCE).setSidedContext(this);
            this.transferToAccount(amount,target);
        }
        if(message.contains("transferMoneyToPlayer")) {
            FancyPacketMap entry = message.getMap("transferMoney");
            MoneyValue amount = entry.get("amount",LCFancyPacketTypes.MONEY);
            String playerName = entry.getString("target");
            this.transferToPlayer(amount,playerName);
        }
    }
}
