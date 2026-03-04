package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconType;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModIcons {

    public static final DeferredRegister<IconType<?>> REGISTER = DeferredRegister.create(LCRegistries.ICON_TYPE,LightmansCurrency.MODID);

    static {
        REGISTER.register("null",() -> IconData.NULL_TYPE);
        REGISTER.register("item",() -> ItemIcon.TYPE);
        REGISTER.register("texture",() -> ImageIcon.TYPE);
        REGISTER.register("icon",() -> IconIcon.TYPE);
        REGISTER.register("text",() -> TextIcon.TYPE);
        REGISTER.register("number_icon",() -> NumberIcon.TYPE);
        REGISTER.register("multi_icon",() -> MultiIcon.TYPE);
    }

}
