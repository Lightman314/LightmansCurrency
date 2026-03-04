package io.github.lightman314.lightmanscurrency.integration.computercraft.stats;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;

public class StatReaders {

    public static Object parseStat(StatType.Instance<?,?> stat) {
        Object value = stat.get();
        if(value instanceof MoneyView view)
            return LCLuaTable.fromMoney(view);
        return null;
    }

}
