package io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Tab for {@link EasyClientTabbedMenuScreen}
 */
public abstract class EasyClientTab<M extends AbstractContainerMenu,S extends EasyClientTabbedMenuScreen<M,S,T>,T extends EasyClientTab<M,S,T>> extends EasyTab {

    public final S screen;
    public final M menu;

    public EasyClientTab(@Nonnull S screen)
    {
        super(screen);
        this.screen = screen;
        this.menu = this.screen.getMenu();
    }

    public boolean tabVisible() { return true; }

    @Nullable
    public Pair<ItemStack,ScreenArea> getHoveredItem(@Nonnull ScreenPosition mousePos) { return null; }
    @Nullable
    public Pair<FluidStack,ScreenArea> getHoveredFluid(@Nonnull ScreenPosition mousePos) { return null; }

}