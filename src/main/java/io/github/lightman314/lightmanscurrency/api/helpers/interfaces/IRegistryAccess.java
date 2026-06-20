package io.github.lightman314.lightmanscurrency.api.helpers.interfaces;

import net.minecraft.core.HolderLookup;

import java.util.Objects;
import java.util.function.Supplier;

public interface IRegistryAccess {

    HolderLookup.Provider registryAccess();

    class Holder implements IRegistryAccess
    {
        private Supplier<HolderLookup.Provider> registryAccess = () -> null;

        @Override
        public HolderLookup.Provider registryAccess() { return Objects.requireNonNull(this.registryAccess.get(),"Registry Access is not yet defined!"); }
        public final void setRegistryAccess(HolderLookup.Provider registryAccess) { this.registryAccess = () -> registryAccess; }
        public final void setRegistryAccess(IRegistryAccess registryAccess) { this.registryAccess = registryAccess::registryAccess; }

    }

}