package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsItemTransformer;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.transformers.BookSettingTransformer;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCSettingItemTransformers {
    private LCSettingItemTransformers() {}

    public static final DeferredRegister<SettingsItemTransformer> REGISTER = DeferredRegister.create(LCRegistries.Trader.SETTINGS_ITEM_TRANSFORMER,LCApi.MODID);

    static {
        register("books",BookSettingTransformer.INSTANCE);
    }

    public static void register(String name,SettingsItemTransformer transformer) {
        REGISTER.register(name,() -> transformer);
    }

}
