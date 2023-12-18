package io.github.lightman314.lightmanscurrency.api.money.coins.display;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.events.RegisterValueDisplayTypes;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.builtin.CoinDisplay;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.builtin.Null;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.builtin.NumberDisplay;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ValueDisplayAPI {

    private static Map<ResourceLocation, ValueDisplaySerializer> REGISTRY = null;

    public static void Setup()
    {
        if(REGISTRY != null)
            return;
        RegisterValueDisplayTypes event = new RegisterValueDisplayTypes();
        //Register built-in
        event.register(Null.SERIALIZER);
        event.register(CoinDisplay.SERIALIZER);
        event.register(NumberDisplay.SERIALIZER);
        //Register others
        ModLoader.get().postEvent(event);
        REGISTRY = event.getResults();
    }

    @Nullable
    public static ValueDisplaySerializer get(@Nonnull ResourceLocation type) { return REGISTRY == null ? null : REGISTRY.get(type); }

}
