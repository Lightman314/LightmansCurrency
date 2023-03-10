package io.github.lightman314.lightmanscurrency.util.config;

import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.util.CoinValueParser;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Supplier;

public class CoinValueConfig implements Supplier<CoinValue> {

    private final ForgeConfigSpec.ConfigValue<String> baseConfig;
    private final Supplier<CoinValue> defaultSupplier;
    private final Supplier<ForgeConfigSpec> specSupplier;

    private CoinValue cachedValue = null;

    private CoinValueConfig(ForgeConfigSpec.ConfigValue<String> baseConfig, Supplier<CoinValue> defaultSupplier,Supplier<ForgeConfigSpec> specSupplier)
    {
        this.baseConfig = baseConfig;
        this.defaultSupplier = defaultSupplier;
        this.specSupplier = specSupplier;

        //Register to the mod event bus
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReloaded);
    }

    public void onConfigReloaded(ModConfig.ModConfigEvent event)
    {
        if(event.getConfig().getSpec() == this.specSupplier.get())
            this.cachedValue = null;
    }

    @Override
    public CoinValue get() {
        if(this.cachedValue == null)
        {
            String input = this.baseConfig.get();
            this.cachedValue = CoinValueParser.ParseConfigString(input, this.defaultSupplier);
        }
        return this.cachedValue;
    }

    public static CoinValueConfig define(ForgeConfigSpec.Builder builder, String path, String defaultInput, CoinValue defaultValue, Supplier<ForgeConfigSpec> specSupplier) { return define(builder, path, defaultInput, defaultValue::copy, specSupplier); }

    public static CoinValueConfig define(ForgeConfigSpec.Builder builder, String path, String defaultInput, Supplier<CoinValue> defaultValue, Supplier<ForgeConfigSpec> specSupplier)
    {
        ForgeConfigSpec.ConfigValue<String> baseConfig = builder.define(path, defaultInput);
        return new CoinValueConfig(baseConfig, defaultValue, specSupplier);
    }

}