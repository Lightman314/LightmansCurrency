package io.github.lightman314.lightmanscurrency.api.data;

import net.minecraft.core.HolderLookup;

import java.util.Objects;

public interface IRegistryAccess {

    HolderLookup.Provider registryAccess();

    class Holder implements IRegistryAccess
    {
        private HolderLookup.Provider registryAccess;
        @Override
        public final HolderLookup.Provider registryAccess() { return Objects.requireNonNull(this.registryAccess,"Registry Access not defined on " + this.getClass().getName()); }
        public final void setRegistryAccess(HolderLookup.Provider registryAccess) { this.registryAccess = registryAccess; }
        public final void setRegistryAccess(IRegistryAccess parent) { this.registryAccess = parent.registryAccess(); }
    }

}
