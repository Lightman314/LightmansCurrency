package io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.GhostSlot;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.Font;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IEasyScreen extends LazyPacketData.IBuilderProvider {

    RegistryAccess registryAccess();
    default LazyPacketData.Builder builder() { return LazyPacketData.builder(this.registryAccess()); }

    default int getGuiLeft() { return this.getArea().x; }
    default int getGuiTop() { return this.getArea().y; }
    
    default ScreenPosition getCorner() { return this.getArea().pos; }
    default int getXSize() { return this.getArea().width; }
    default int getYSize() { return this.getArea().height; }
    
    ScreenArea getArea();

    
    Font getFont();
    
    Player getPlayer();

    <W> W addChild(W child);
    void removeChild(Object child);

    default boolean blockInventoryClosing() { return false; }

    @Nullable
    default Pair<ItemStack,ScreenArea> getHoveredItem(ScreenPosition mousePos) { return null; }
    @Nullable
    default Pair<FluidStack,ScreenArea> getHoveredFluid(ScreenPosition mousePos) { return null; }
    
    List<GhostSlot<?>> getGhostSlots();

}
