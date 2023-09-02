package io.github.lightman314.lightmanscurrency.common.bank.interfaces;

import io.github.lightman314.lightmanscurrency.client.data.ClientBankData;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public interface IBankAccountMenu extends IClientTracker
{
    Player getPlayer();
    Container getCoinInput();
    default void onDepositOrWithdraw() {}
    default BankReference getBankAccountReference() {
        return this.isClient() ? ClientBankData.GetLastSelectedAccount() : BankSaveData.GetSelectedBankAccount(this.getPlayer());
    }
    default BankAccount getBankAccount() {
        BankReference reference = this.getBankAccountReference();
        return reference == null ? null : reference.get();
    }
}
