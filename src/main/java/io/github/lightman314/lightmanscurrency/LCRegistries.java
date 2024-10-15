package io.github.lightman314.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.ejection.EjectionDataType;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class LCRegistries {

    public static final ResourceKey<Registry<EjectionDataType>> EJECTION_DATA_KEY = ResourceKey.createRegistryKey(VersionUtil.lcResource("ejection_data"));
    public static final Registry<EjectionDataType> EJECTION_DATA = new RegistryBuilder<>(EJECTION_DATA_KEY).create();

}
