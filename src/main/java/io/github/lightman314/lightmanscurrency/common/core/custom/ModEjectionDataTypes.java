package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.ejection.builtin.BasicEjectionData;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.TraderEjectionData;

public class ModEjectionDataTypes {

    public static void init() {}

    static {
        ModRegistries.EJECTION_DATA.register("basic", () -> BasicEjectionData.TYPE);
        ModRegistries.EJECTION_DATA.register("trader",() -> TraderEjectionData.TYPE);
    }

}
