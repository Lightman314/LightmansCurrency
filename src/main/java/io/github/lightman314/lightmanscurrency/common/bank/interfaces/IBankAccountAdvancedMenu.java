package io.github.lightman314.lightmanscurrency.common.bank.interfaces;

import net.minecraft.network.chat.MutableComponent;

public interface IBankAccountAdvancedMenu extends IBankAccountMenu {
    void setTransferMessage(MutableComponent component);
}
