package io.github.lightman314.lightmanscurrency.util.config;

import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Supplier;

public class ScreenPositionConfig implements Supplier<ScreenPosition> {


    private final ForgeConfigSpec.IntValue baseXConfig;
    private final ForgeConfigSpec.IntValue baseYConfig;
    private final Supplier<ForgeConfigSpec> specSupplier;

    private ScreenPosition cachedValue = null;

    private ScreenPositionConfig(ForgeConfigSpec.IntValue baseXConfig, ForgeConfigSpec.IntValue baseYConfig, Supplier<ForgeConfigSpec> specSupplier)
    {
        this.baseXConfig = baseXConfig;
        this.baseYConfig = baseYConfig;
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
    public ScreenPosition get() {
        if(this.cachedValue == null)
            this.cachedValue = ScreenPosition.of(this.baseXConfig.get(), this.baseYConfig.get());
        return this.cachedValue;
    }

    public ScreenPosition getDefault() { return ScreenPosition.of(this.baseXConfig.getDefault(), this.baseYConfig.getDefault()); }

    public static ScreenPositionConfig define(ForgeConfigSpec.Builder builder, String path, ScreenPosition defaultValue, Supplier<ForgeConfigSpec> specSupplier) {
        ForgeConfigSpec.IntValue x = builder.defineInRange(path + "X", defaultValue.x, -1000, 1000);
        ForgeConfigSpec.IntValue y = builder.defineInRange(path + "Y", defaultValue.y, -1000, 1000);
        return new ScreenPositionConfig(x, y, specSupplier);
    }

}
