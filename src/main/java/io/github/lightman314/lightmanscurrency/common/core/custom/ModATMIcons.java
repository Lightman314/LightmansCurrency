package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconType;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModATMIcons {

    public static final DeferredRegister<ATMIconType> REGISTER = DeferredRegister.create(LCRegistries.ATM_ICON_TYPE,LightmansCurrency.MODID);

    static {
        REGISTER.register("item",() -> ATMItemIcon.TYPE);
        REGISTER.register("small_arrow",() -> SimpleArrowIcon.TYPE);
        REGISTER.register("texture",() -> SpriteIcon.TYPE);
    }

}
