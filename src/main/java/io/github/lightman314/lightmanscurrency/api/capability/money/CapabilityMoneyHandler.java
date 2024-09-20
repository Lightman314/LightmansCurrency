package io.github.lightman314.lightmanscurrency.api.capability.money;

import io.github.lightman314.lightmanscurrency.common.capability.CurrencyCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CapabilityMoneyHandler {

    private CapabilityMoneyHandler() {}

    @Nonnull
    public static ICapabilityProvider createProvider(@Nonnull IMoneyHandler handler) { return new Provider(handler); }

    /**
     * Important Note:<br>
     * If an item-related Money Handler requires knowledge of which logical side it's being interacted on, please implement {@link io.github.lightman314.lightmanscurrency.api.misc.ISidedObject ISidedObject} so that is can be properly flagged<br>
     * All items with a Money Handler capability will automatically function as trader payment, etc.
     */
    @Nullable
    public static IMoneyHandler getCapability(@Nonnull ItemStack stack)
    {
        LazyOptional<IMoneyHandler> cap = stack.getCapability(CurrencyCapabilities.MONEY_HANDLER);
        if(cap.isPresent())
            return cap.orElseThrow(() -> new RuntimeException("Unexpected error occurred!"));
        return null;
    }

    private static class Provider implements ICapabilityProvider
    {

        private final LazyOptional<IMoneyHandler> lazyOptional;

        private Provider(@Nonnull IMoneyHandler viewer) { this.lazyOptional = LazyOptional.of(() -> viewer); }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return CurrencyCapabilities.MONEY_HANDLER.orEmpty(cap, this.lazyOptional);
        }
    }

}
