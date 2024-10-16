package io.github.lightman314.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.ejection.EjectionDataType;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.IForgeRegistry;

public class LCRegistries {

    public static final ResourceKey<Registry<EjectionDataType>> EJECTION_DATA_KEY = ResourceKey.createRegistryKey(VersionUtil.lcResource("ejection_data"));
    public static IForgeRegistry<EjectionDataType> EJECTION_DATA = null;

}
