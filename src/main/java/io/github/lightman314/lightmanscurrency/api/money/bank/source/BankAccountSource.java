package io.github.lightman314.lightmanscurrency.api.money.bank.source;

import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class BankAccountSource {

    /**
     * When called, should return a list of all bank references that exist.<br>
     * {@link BankReference#allowedAccess(Player)} may then be used to filter what bank accounts players are allowed to access.
     */
    @Nonnull
    public abstract List<BankReference> CollectAllReferences(boolean isClient);

    /**
     * When called, returns a list of all bank accounts that exist.<br>
     * Use {@link #CollectAllReferences(boolean)} if you wish to filter the available bank accounts based on whether a given player is allowed to access it.
     */
    public List<IBankAccount> CollectAllBankAccounts(boolean isClient) {
        List<IBankAccount> list = new ArrayList<>();
        for(BankReference br : this.CollectAllReferences(isClient))
        {
            IBankAccount ba = br.get();
            if(ba != null)
                list.add(ba);
        }
        return list;
    }

}