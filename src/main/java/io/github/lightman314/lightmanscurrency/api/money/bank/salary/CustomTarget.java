package io.github.lightman314.lightmanscurrency.api.money.bank.salary;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import java.util.List;

@MethodsReturnNonnullByDefault
public interface CustomTarget extends IClientTracker {

    List<BankReference> getTargets();
    Component getName();

    interface ForPlayers extends CustomTarget
    {
        @Override
        default List<BankReference> getTargets() { return this.getPlayers().stream().map(pr -> PlayerBankReference.of(pr).flagAsClient(this)).toList(); }
        List<PlayerReference> getPlayers();
    }

}
