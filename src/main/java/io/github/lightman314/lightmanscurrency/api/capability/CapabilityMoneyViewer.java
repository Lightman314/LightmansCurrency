package io.github.lightman314.lightmanscurrency.api.capability;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.MoneyViewer;
import io.github.lightman314.lightmanscurrency.common.capability.CurrencyCapabilities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullFunction;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;

public class CapabilityMoneyViewer {

    public static LazyOptional<IMoneyViewer> getCapability(@Nonnull ItemStack stack) { return stack.getCapability(CurrencyCapabilities.MONEY_VIEWER); }

    public static MoneyView getContents(@Nonnull ItemStack stack)
    {
        LazyOptional<IMoneyViewer> optional = getCapability(stack);
        AtomicReference<MoneyView> result = new AtomicReference<>(MoneyView.empty());
        optional.ifPresent(v -> result.set(v.getStoredMoney()));
        return result.get();
    }

    public static ICapabilityProvider createProvider(@Nonnull ItemStack stack, @Nonnull NonNullFunction<ItemStack,MoneyView> getContents) {
        return new Provider(new StackView(stack, getContents));
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

    private static class StackView extends MoneyViewer
    {

        private final ItemStack stack;
        private final NonNullFunction<ItemStack,MoneyView> getContents;
        private ItemStack cachedStack = ItemStack.EMPTY;

        private StackView(@Nonnull ItemStack stack, @Nonnull NonNullFunction<ItemStack,MoneyView> getContents)
        {
            this.stack = stack;
            this.getContents = getContents;
        }

        @Override
        protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
            this.cachedStack = this.stack.copy();
            builder.merge(this.getContents.apply(this.stack));
        }

        @Override
        public boolean hasStoredMoneyChanged() { return !InventoryUtil.ItemsFullyMatch(this.stack, this.cachedStack); }
    }

}
