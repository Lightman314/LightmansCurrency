package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.stats.types.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStatTypes {

    public static final DeferredRegister<StatType<?,?>> REGISTER = DeferredRegister.create(LCRegistries.STAT_TYPES,LightmansCurrency.MODID);

    static {
        REGISTER.register("basic_int",() -> IntegerStat.TYPE);
        REGISTER.register("multi_money",() -> MultiMoneyStat.TYPE);
    }

}
