package io.github.lightman314.lightmanscurrency.api.stats.builtin;

import io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper;
import io.github.lightman314.lightmanscurrency.api.money.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;

public class MoneyStatType extends StatType<MoneyView,MoneyValue> {

    public static final StatType<MoneyView,MoneyValue> TYPE = new MoneyStatType();

    protected MoneyStatType() { super(MoneyView.CODEC,MoneyView.STREAM_CODEC); }

    @Override
    protected Class<MoneyView> getValueClass() { return MoneyView.class; }

    @Override
    public MoneyView addToValue(MoneyView currentValue,MoneyValue addition) { return currentValue.makeMutable().add(addition).build(); }

    @Override
    public MoneyView getEmptyValue() { return MoneyView.EMPTY; }
    @Override
    public boolean isEmptyValue(MoneyView value) { return value.isEmpty(); }
    @Override
    public Component getValueText(MoneyView value) { return MoneyDisplayHelper.getCyclingValueText(value); }
    @Nullable
    @Override
    public List<Component> getValueTooltip(MoneyView value) {
        //Create a tooltip of all money values
        if(value.getAllResources().size() > 1)
            return MoneyDisplayHelper.contentsAsMultiLineText(value);
        return null;
    }

}
