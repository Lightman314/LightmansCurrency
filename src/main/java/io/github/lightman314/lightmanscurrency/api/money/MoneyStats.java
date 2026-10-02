package io.github.lightman314.lightmanscurrency.api.money;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;

public final class MoneyStats {
    private MoneyStats() {}

    public static final StatKey<MoneyView, MoneyValue> MONEY_EARNED = StatKey.createMoney(LCApi.id("money_earned"),0);
    public static final StatKey<MoneyView, MoneyValue> MONEY_PAID = StatKey.createMoney(LCApi.id("money_paid"),1);
    public static final StatKey<MoneyView,MoneyValue> TAXES_PAID = StatKey.createMoney(LCApi.id("taxes_paid"),5);

}
