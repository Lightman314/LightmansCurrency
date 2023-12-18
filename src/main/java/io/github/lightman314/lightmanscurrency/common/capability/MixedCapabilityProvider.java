package io.github.lightman314.lightmanscurrency.common.capability;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class MixedCapabilityProvider implements ICapabilityProvider {

    private final List<ICapabilityProvider> providers;
    public MixedCapabilityProvider(@Nonnull List<ICapabilityProvider> providers) { this.providers = ImmutableList.copyOf(providers); }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        for(ICapabilityProvider p : this.providers)
        {
            LazyOptional<T> result = p.getCapability(cap, side);
            if(result.isPresent())
                return result;
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        for(ICapabilityProvider p : this.providers)
        {
            LazyOptional<T> result = p.getCapability(cap);
            if(result.isPresent())
                return result;
        }
        return LazyOptional.empty();
    }
}
