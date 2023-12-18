package io.github.lightman314.lightmanscurrency.util.config;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Supplier;

public class MoneyValueConfig implements Supplier<MoneyValue> {

    private final ForgeConfigSpec.ConfigValue<String> baseConfig;
    private final Supplier<MoneyValue> defaultSupplier;
    private final Supplier<ForgeConfigSpec> specSupplier;

    private MoneyValue cachedValue = null;

    private MoneyValueConfig(ForgeConfigSpec.ConfigValue<String> baseConfig, Supplier<MoneyValue> defaultSupplier, Supplier<ForgeConfigSpec> specSupplier)
    {
        this.baseConfig = baseConfig;
        this.defaultSupplier = defaultSupplier;
        this.specSupplier = specSupplier;

        //Register to the mod event bus
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReloaded);
    }

    public void onConfigReloaded(ModConfigEvent event)
    {
        if(event.getConfig().getSpec() == this.specSupplier.get())
            this.cachedValue = null;
    }

    @Override
    public MoneyValue get() {
        if(this.cachedValue == null)
        {
            String input = this.baseConfig.get();
            this.cachedValue = MoneyValueParser.ParseConfigString(input, this.defaultSupplier);
        }
        return this.cachedValue;
    }

    public static MoneyValueConfig define(ForgeConfigSpec.Builder builder, String path, String defaultInput, MoneyValue defaultValue, Supplier<ForgeConfigSpec> specSupplier) { return define(builder, path, defaultInput, () -> defaultValue, specSupplier); }

    public static MoneyValueConfig define(ForgeConfigSpec.Builder builder, String path, String defaultInput, Supplier<MoneyValue> defaultValue, Supplier<ForgeConfigSpec> specSupplier)
    {
        ForgeConfigSpec.ConfigValue<String> baseConfig = builder.define(path, defaultInput);
        return new MoneyValueConfig(baseConfig, defaultValue, specSupplier);
    }

}
