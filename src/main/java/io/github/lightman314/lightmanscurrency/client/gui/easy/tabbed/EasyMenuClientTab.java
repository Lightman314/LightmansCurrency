package io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyMenuTab;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.IEasyTabbedMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

public abstract class EasyMenuClientTab<X extends T,M extends IEasyTabbedMenu<T>,T extends EasyMenuTab<M,T>,S extends IEasyTabbedMenuScreen<M,T,S>,C extends EasyMenuClientTab<X,M,T,S,C>> extends EasyTab {

    public final S screen;
    public final M menu;
    public final X commonTab;

    public EasyMenuClientTab(Object screen, X commonTab) {
        super((S)screen);
        this.screen = (S)screen;
        this.menu = this.screen.getMenuInterface();
        this.commonTab = commonTab;
    }

    public boolean tabVisible() { return this.commonTab.canOpen(this.screen.getPlayer()); }

    protected void OpenMessage(LazyPacketData clientData) {}

    @Nullable
    public Pair<ItemStack,ScreenArea> getHoveredItem(ScreenPosition mousePos) { return null; }
    @Nullable
    public Pair<FluidStack,ScreenArea> getHoveredFluid(ScreenPosition mousePos) { return null; }

}
