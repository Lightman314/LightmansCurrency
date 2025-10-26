package io.github.lightman314.lightmanscurrency.api.money.bank.menu;

import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.data.types.BankDataCache;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public interface IBankAccountMenu extends IClientTracker
{
    Player getPlayer();
    Container getCoinInput();
    default void onDepositOrWithdraw() {}
    default BankReference getBankAccountReference() { return BankDataCache.TYPE.get(this).getSelectedAccount(this.getPlayer()); }
    default IBankAccount getBankAccount() {
        BankReference reference = this.getBankAccountReference();
        return reference == null ? null : reference.get();
    }
}
