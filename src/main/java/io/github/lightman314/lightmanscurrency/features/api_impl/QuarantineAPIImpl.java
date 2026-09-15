package io.github.lightman314.lightmanscurrency.features.api_impl;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.quarantine.QuarantineAPI;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class QuarantineAPIImpl implements QuarantineAPI {

    public static final QuarantineAPIImpl INSTANCE = new QuarantineAPIImpl();

    private QuarantineAPIImpl() {}

    @Override
    public boolean isQuarantined(ResourceKey<Level> level) { return LCConfig.SERVER.quarantinedDimensions.contains(level.identifier()); }

}
