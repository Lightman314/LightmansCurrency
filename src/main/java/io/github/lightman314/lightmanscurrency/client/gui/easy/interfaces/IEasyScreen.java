package io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IEasyScreen extends LazyPacketData.IBuilderProvider {

    @Nonnull
    default LazyPacketData.Builder builder() { return LazyPacketData.builder(); }

    default int getGuiLeft() { return this.getArea().x; }
    default int getGuiTop() { return this.getArea().y; }
    @Nonnull
    default ScreenPosition getCorner() { return this.getArea().pos; }
    default int getXSize() { return this.getArea().width; }
    default int getYSize() { return this.getArea().height; }
    @Nonnull
    ScreenArea getArea();

    @Nonnull
    Font getFont();
    @Nonnull
    Player getPlayer();

    <W> W addChild(W child);
    void removeChild(Object child);

    default boolean blockInventoryClosing() { return false; }

    @Nullable
    default Pair<ItemStack,ScreenArea> getHoveredItem(@Nonnull ScreenPosition mousePos) { return null; }
    @Nullable
    default Pair<FluidStack,ScreenArea> getHoveredFluid(@Nonnull ScreenPosition mousePos) { return null; }

}
