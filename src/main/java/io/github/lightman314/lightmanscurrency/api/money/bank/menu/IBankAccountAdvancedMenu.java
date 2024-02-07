package io.github.lightman314.lightmanscurrency.api.money.bank.menu;

import net.minecraft.network.chat.MutableComponent;

public interface IBankAccountAdvancedMenu extends IBankAccountMenu
{
    void setTransferMessage(MutableComponent component);
}
