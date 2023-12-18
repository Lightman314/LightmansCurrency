package io.github.lightman314.lightmanscurrency.api.money.value.holder;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple tool for viewing a sum of multiple money holders
 */
public class MultiMoneyHolder extends MoneyHolder {

    private final List<IMoneyHolder> holdersPayFirst;
    private final List<IMoneyHolder> holdersTakeFirst;

    public MultiMoneyHolder(@Nonnull List<IMoneyHolder> holders)
    {
        this.holdersPayFirst = new ArrayList<>(holders);
        IMoneyHolder.sortPayFirst(this.holdersPayFirst);
        this.holdersTakeFirst = new ArrayList<>(holders);
        IMoneyHolder.sortTakeFirst(this.holdersTakeFirst);
    }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        for(IMoneyHolder holder : this.holdersPayFirst)
        {
            builder.merge(holder.getStoredMoney());
            holder.flagAsKnown(this);
        }
    }

    @Override
    public boolean hasStoredMoneyChanged() {
        for(IMoneyHolder holder : this.holdersPayFirst)
        {
            if(holder.hasStoredMoneyChanged(this))
                return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public MoneyValue tryAddMoney(@Nonnull MoneyValue valueToAdd) {
        for(IMoneyHolder holder : this.holdersPayFirst)
        {
            valueToAdd = holder.tryAddMoney(valueToAdd);
            if(valueToAdd.isEmpty())
                return MoneyValue.empty();
        }
        return valueToAdd;
    }

    @Nonnull
    @Override
    public MoneyValue tryRemoveMoney(@Nonnull MoneyValue valueToRemove) {
        for(IMoneyHolder holder : this.holdersTakeFirst)
        {
            valueToRemove = holder.tryRemoveMoney(valueToRemove);
            if(valueToRemove.isEmpty())
                return MoneyValue.empty();
        }
        return valueToRemove;
    }

    @Override
    public void formatTooltip(@Nonnull List<Component> tooltip) {
        for(IMoneyHolder holder : this.holdersTakeFirst)
            holder.formatTooltip(tooltip);
    }
    @Override
    public Component getTooltipTitle() { return EasyText.empty(); }

    public void clearCache(@Nullable Object context) {
        if(context != null)
            this.forgetContext(context);
        for(IMoneyHolder holder : this.holdersTakeFirst)
            holder.forgetContext(this);
    }

}
