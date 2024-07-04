package io.github.lightman314.lightmanscurrency.common.advancements;

import io.github.lightman314.lightmanscurrency.common.advancements.date.DateTrigger;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;

public class LCAdvancementTriggers {
    
    public static void init() { }

    static {
        ModRegistries.CRITERION_TRIGGERS.register("date_range",() -> DateTrigger.INSTANCE);
    }
    
}
