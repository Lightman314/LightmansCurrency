package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconType;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin.ATMItemIcon;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin.ATMArrowIcon;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin.ATMSpriteIcon;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCATMIconTypes {

    private LCATMIconTypes() {}

    public static final DeferredRegister<ATMIconType> REGISTER = DeferredRegister.create(LCRegistries.Coins.ATM_ICON_TYPE,LCApi.MODID);

    static {
        register("item",ATMItemIcon.TYPE);
        register("small_arrow",ATMArrowIcon.TYPE);
        register("texture",ATMSpriteIcon.TYPE);
    }

    private static void register(String name,ATMIconType type) { REGISTER.register(name,() -> type); }

}