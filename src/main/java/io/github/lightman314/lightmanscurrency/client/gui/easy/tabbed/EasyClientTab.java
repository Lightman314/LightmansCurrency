package io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

/**
 * Tab for {@link EasyClientTabbedMenuScreen}
 */
public abstract class EasyClientTab<M extends AbstractContainerMenu,S extends EasyClientTab.ClientMenuScreen<M,S,T>,T extends EasyClientTab<M,S,T>> extends EasyTab {

    public final S screen;
    public final M menu;

    public EasyClientTab(S screen) {
        super(screen);
        this.screen = screen;
        this.menu = this.screen.getMenu();
    }

    public boolean tabVisible() { return true; }

    @Nullable
    public Pair<ItemStack, ScreenArea> getHoveredItem(ScreenPosition mousePos) {
        return null;
    }

    @Nullable
    public Pair<FluidStack, ScreenArea> getHoveredFluid(ScreenPosition mousePos) {
        return null;
    }

    public static abstract class Unenforced<M extends AbstractContainerMenu,S extends EasyClientTab.ClientMenuScreen<M,S,T>,T extends EasyClientTab.Unenforced<M,S,T>> extends EasyClientTab<M,S,T>
    {
        public Unenforced(S screen) { super(screen); }
        @Override
        public IconData getIcon() { return IconData.Null(); }
        @Nullable
        @Override
        public Component getTooltip() { return EasyText.empty(); }
    }

    public static abstract class ClientMenuScreen<M extends AbstractContainerMenu, S extends ClientMenuScreen<M, S, T>, T extends EasyClientTab<M, S, T>> extends EasyMenuScreen<M>
    {

        protected ClientMenuScreen(M menu, Inventory inventory) { super(menu, inventory); }
        protected ClientMenuScreen(M menu, Inventory inventory, Component title) { super(menu, inventory, title); }

    }


}
