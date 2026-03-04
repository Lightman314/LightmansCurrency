package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionDataType;
import io.github.lightman314.lightmanscurrency.api.ejection.builtin.BasicEjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.TraderEjectionData;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEjectionDataTypes {

    public static final DeferredRegister<EjectionDataType<?>> REGISTER = DeferredRegister.create(LCRegistries.EJECTION_DATA,LightmansCurrency.MODID);

    static {
        REGISTER.register("basic",() -> BasicEjectionData.TYPE);
        REGISTER.register("trader",() -> TraderEjectionData.TYPE);
    }

}
