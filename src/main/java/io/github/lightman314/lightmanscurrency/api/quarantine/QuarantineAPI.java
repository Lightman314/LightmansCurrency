package io.github.lightman314.lightmanscurrency.api.quarantine;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface QuarantineAPI {

    default boolean isQuarantined(Level level) { return this.isQuarantined(level.dimension()); }
    boolean isQuarantined(ResourceKey<Level> level);

}