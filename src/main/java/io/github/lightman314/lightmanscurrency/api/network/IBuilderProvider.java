package io.github.lightman314.lightmanscurrency.api.network;

import io.github.lightman314.lightmanscurrency.api.data.IRegistryAccess;

public interface IBuilderProvider extends IRegistryAccess {
    default LazyPacketData.Builder builder() { return LazyPacketData.builder(this.registryAccess()); }
}
