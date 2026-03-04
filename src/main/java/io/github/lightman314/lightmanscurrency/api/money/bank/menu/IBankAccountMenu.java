package io.github.lightman314.lightmanscurrency.api.money.bank.menu;

import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.data.types.BankDataCache;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.IItemHandler;

public interface IBankAccountMenu extends IClientTracker
{
    Player getPlayer();
    IItemHandler getCoinInput();
    default void onDepositOrWithdraw() {}
    default BankReference getBankAccountReference() { return BankDataCache.TYPE.get(this).getSelectedAccount(this.getPlayer()); }
    default IBankAccount getBankAccount() {
        BankReference reference = this.getBankAccountReference();
        return reference == null ? null : reference.get();
    }
}
