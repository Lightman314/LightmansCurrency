package io.github.lightman314.lightmanscurrency.api.stats;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.stats.types.IntegerStat;
import io.github.lightman314.lightmanscurrency.api.stats.types.MultiMoneyStat;

public class StatKeys {

    private StatKeys(){}

    public static class Generic
    {
        public static final StatKey<MoneyView,MoneyValue> MONEY_EARNED = MultiMoneyStat.INSTANCE.createKey("generic.money_earned");
        public static final StatKey<MoneyView,MoneyValue> MONEY_PAID = MultiMoneyStat.INSTANCE.createKey("generic.money_paid");
    }

    public static class Traders
    {
        public static final StatKey<MoneyView, MoneyValue> MONEY_EARNED = MultiMoneyStat.INSTANCE.createKey("traders.money_earned");
        public static final StatKey<MoneyView, MoneyValue> MONEY_PAID = MultiMoneyStat.INSTANCE.createKey("traders.money_paid");
        public static final StatKey<Integer,Integer> TRADES_EXECUTED = IntegerStat.INSTANCE.createKey("traders.trades_executed");
    }

    public static class Taxables
    {
        public static final StatKey<MoneyView, MoneyValue> TAXES_PAID = MultiMoneyStat.INSTANCE.createKey("taxables.taxes_paid");
    }

}
