package io.github.lightman314.lightmanscurrency.api.money.bank.salary;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import java.util.List;

@MethodsReturnNonnullByDefault
public interface CustomTarget {

    List<BankReference> getTargets();
    Component getName();

    interface ForPlayers extends CustomTarget
    {
        @Override
        default List<BankReference> getTargets() { return this.getPlayers().stream().map(PlayerBankReference::of).toList(); }
        List<PlayerReference> getPlayers();
    }

}