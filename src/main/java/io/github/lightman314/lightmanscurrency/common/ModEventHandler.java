package io.github.lightman314.lightmanscurrency.common;

import io.github.lightman314.lightmanscurrency.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionDataType;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = LightmansCurrency.MODID)
public class ModEventHandler {

    @SubscribeEvent
    public static void registerRegistries(@Nonnull NewRegistryEvent event)
    {
        event.create(new RegistryBuilder<EjectionDataType>().setName(VersionUtil.lcResource("ejection_data")),r -> LCRegistries.EJECTION_DATA = r);
    }

}
