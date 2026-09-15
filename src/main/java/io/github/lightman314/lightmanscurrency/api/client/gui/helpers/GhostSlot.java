package io.github.lightman314.lightmanscurrency.api.client.gui.helpers;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IGhostSlotProvider;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public record GhostSlot<T>(ScreenArea area, Consumer<T> handler,Class<T> clazz) {

    public static GhostSlot<ItemStack> simpleItem(ScreenPosition pos,Consumer<ItemStack> handler) { return new GhostSlot<>(pos.asArea(16,16),handler,ItemStack.class); }
    public static GhostSlot<FluidStack> simpleFluid(ScreenPosition pos,Consumer<FluidStack> handler) { return new GhostSlot<>(pos.asArea(16,16),handler,FluidStack.class); }

    public void tryAccept(Object object) throws ClassCastException { this.handler.accept((T)object); }

    public IGhostSlotProvider asProvider() { return this.asProvider(() -> true); }
    public IGhostSlotProvider asProvider(BooleanSupplier valid) { return new LazyProvider(this,valid); }

    private record LazyProvider(GhostSlot<?> slot,BooleanSupplier valid) implements IGhostSlotProvider {
        @Nullable
        @Override
        public List<GhostSlot<?>> getGhostSlots() { return this.valid.getAsBoolean() ? List.of(this.slot) : null; }
    }

}
