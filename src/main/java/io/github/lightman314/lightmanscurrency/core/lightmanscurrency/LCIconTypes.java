package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.icon.IconType;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.EmptyIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCIconTypes {
    private LCIconTypes() {}

    public static final DeferredRegister<IconType<?>> REGISTER = DeferredRegister.create(LCRegistries.Misc.ICON_TYPE,LCApi.MODID);

    static {
        register("null",EmptyIcon.TYPE);
        register("item",ItemIcon.TYPE);
        register("sprite",SpriteIcon.TYPE);
    }

    private static void register(String name,IconType<?> type) {
        REGISTER.register(name,() -> type);
    }

}