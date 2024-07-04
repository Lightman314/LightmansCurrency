package io.github.lightman314.lightmanscurrency.api.money.bank.menu;

import net.minecraft.network.chat.Component;

public interface IBankAccountAdvancedMenu extends IBankAccountMenu
{
    void setTransferMessage(Component component);
}
