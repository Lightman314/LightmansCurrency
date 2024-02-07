package io.github.lightman314.lightmanscurrency.common.advancements;

import io.github.lightman314.lightmanscurrency.common.advancements.date.DateTrigger;
import net.minecraft.advancements.CriteriaTriggers;

public class LCAdvancementTriggers {
    
    public static void setup() {
        CriteriaTriggers.register(DateTrigger.INSTANCE);
    }
    
}
