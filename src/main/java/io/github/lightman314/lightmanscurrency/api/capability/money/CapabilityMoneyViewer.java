package io.github.lightman314.lightmanscurrency.api.capability.money;

import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.common.capability.CurrencyCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CapabilityMoneyViewer {

    private CapabilityMoneyViewer() {}

    @Nonnull
    public static ICapabilityProvider createProvider(@Nonnull IMoneyViewer handler) { return new Provider(handler); }

    public static IMoneyViewer getCapability(@Nonnull ItemStack stack)
    {
        LazyOptional<IMoneyViewer> cap = stack.getCapability(CurrencyCapabilities.MONEY_VIEWER);
        if(cap.isPresent())
            return cap.orElseThrow(() -> new RuntimeException("Unexpected error occurred!"));
        return null;
    }

    private static class Provider implements ICapabilityProvider
    {

        private final LazyOptional<IMoneyViewer> lazyOptional;

        private Provider(@Nonnull IMoneyViewer viewer) { this.lazyOptional = LazyOptional.of(() -> viewer); }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return CurrencyCapabilities.MONEY_VIEWER.orEmpty(cap, this.lazyOptional);
        }
    }

}
