package io.github.lightman314.lightmanscurrency.client.gui.easy;

import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IGhostSlotProvider;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record GhostSlot<T>(ScreenArea area, Consumer<T> handler, Class<T> clazz) {

    public static GhostSlot<ItemStack> simpleItem(ScreenPosition pos,Consumer<ItemStack> handler) { return new GhostSlot<>(pos.asArea(16,16),handler,ItemStack.class); }
    public static GhostSlot<FluidStack> simpleFluid(ScreenPosition pos, Consumer<FluidStack> handler) { return new GhostSlot<>(pos.asArea(16,16),handler,FluidStack.class); }

    public void tryAccept(Object object) throws ClassCastException { this.handler.accept((T)object); }

    public IGhostSlotProvider asProvider() { return this.asProvider(() -> true); }
    public IGhostSlotProvider asProvider(Supplier<Boolean> valid) { return new LazyProvider(this,valid); }

    private static class LazyProvider implements IGhostSlotProvider
    {
        private final GhostSlot<?> slot;
        private final Supplier<Boolean> valid;
        LazyProvider(GhostSlot<?> slot,Supplier<Boolean> valid) { this.slot = slot; this.valid = valid;}
        @Nullable
        @Override
        public List<GhostSlot<?>> getGhostSlots() { return this.valid.get() ? List.of(this.slot) : null; }
    }

}
