package io.github.lightman314.lightmanscurrency.api.money.capability.implementations;

import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.money.capability.MoneyHolder;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple tool for viewing a sum of multiple money holders
 */
public class MultiMoneyHolder extends MoneyHolder {

    private final List<IMoneyHolder> holdersPayFirst;
    private final List<IMoneyHolder> holdersTakeFirst;

    public MultiMoneyHolder(List<IMoneyHolder> holders)
    {
        this.holdersPayFirst = new ArrayList<>(holders);
        IMoneyHolder.sortPayFirst(this.holdersPayFirst);
        this.holdersTakeFirst = new ArrayList<>(holders);
        IMoneyHolder.sortTakeFirst(this.holdersTakeFirst);
    }

    @Override
    protected void collectStoredMoney(MoneyView.Builder builder) {
        for(IMoneyHolder holder : this.holdersPayFirst)
            builder.merge(holder.getStoredMoney());
    }

    @Override
    public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) {
        for(IMoneyHolder holder : this.holdersPayFirst)
        {
            insertAmount = holder.insertMoney(insertAmount, simulation);
            if(insertAmount.isEmpty())
                return MoneyValue.empty();
        }
        return insertAmount;
    }

    @Override
    public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) {
        for(IMoneyHolder holder : this.holdersTakeFirst)
        {
            extractAmount = holder.extractMoney(extractAmount, simulation);
            if(extractAmount.isEmpty())
                return MoneyValue.empty();
        }
        return extractAmount;
    }

    @Override
    public boolean isMoneyTypeValid(MoneyValue value) { return this.holdersPayFirst.stream().anyMatch(h -> h.isMoneyTypeValid(value)); }

    @Override
    public void formatTooltip(List<Component> tooltip) {
        for(IMoneyHolder holder : this.holdersTakeFirst)
            holder.formatTooltip(tooltip);
    }
    @Override
    public Component getTooltipTitle() { return EasyText.empty(); }

}
