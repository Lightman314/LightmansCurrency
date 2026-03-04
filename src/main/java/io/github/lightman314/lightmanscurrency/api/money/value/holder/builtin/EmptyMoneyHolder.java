package io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHolder;
import net.minecraft.network.chat.Component;

public class EmptyMoneyHolder implements IMoneyHolder {

    @Override
    public Component getTooltipTitle() { return EasyText.empty(); }

    @Override
    public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) { return insertAmount; }

    @Override
    public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) { return extractAmount; }

    @Override
    public boolean isMoneyTypeValid(MoneyValue value) { return false; }

    @Override
    public MoneyView getStoredMoney() { return MoneyView.empty(); }
}
