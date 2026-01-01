package io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

/**
 * Menu Screen for menus that only have tabs on the client-side.<br>
 * @see EasyTabbedMenuScreen
 * @see io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyTabbedMenu EasyTabbedMenu
 */
public abstract class EasyClientUnenforcedTabbedMenuScreen<M extends AbstractContainerMenu,S extends EasyClientUnenforcedTabbedMenuScreen<M,S,T>,T extends EasyClientTab<M,S,T>> extends EasyClientTab.ClientMenuScreen<M,S,T> {

    private final T defaultTab;
    private T currentTab;
    public final T currentTab() { return this.currentTab; }

    private IWidgetPositioner tabButtonPositioner;

    protected EasyClientUnenforcedTabbedMenuScreen(M menu,Inventory inventory,Function<S,T> defaultTab) { this(menu,inventory,EasyText.empty(),defaultTab); }
    protected EasyClientUnenforcedTabbedMenuScreen(M menu,Inventory inventory,Component title, Function<S,T> defaultTab) {
        super(menu,inventory,title);
        this.defaultTab = this.currentTab = Objects.requireNonNull(defaultTab.apply((S)this));
    }

    @Override
    protected final void initialize(ScreenArea screenArea) {
        //Initialize screen-related widgets
        this.init(screenArea);
        //Initialize the currently opened tab
        this.currentTab().onOpen();
    }

    protected abstract void init(ScreenArea screenArea);

    public void setTab(T newTab) {
        //Close the old tab
        this.currentTab.onClose();
        //Set the new tab
        this.currentTab = newTab;
        this.currentTab.onOpen();
    }
    public void closeTab() { this.setTab(this.defaultTab); }

    @Override
    protected final void renderBG(EasyGuiGraphics gui) {
        this.renderBackground(gui);
        this.currentTab().renderBG(gui);
    }
    protected abstract void renderBackground(EasyGuiGraphics gui);

    @Override
    protected final void renderAfterWidgets(EasyGuiGraphics gui) {
        this.renderLate(gui);
        this.currentTab().renderAfterWidgets(gui);
    }
    protected void renderLate(EasyGuiGraphics gui) {}

    @Override
    public boolean blockInventoryClosing() { return this.currentTab() != null && this.currentTab().blockInventoryClosing(); }

    @Nullable
    @Override
    public Pair<ItemStack,ScreenArea> getHoveredItem(ScreenPosition mousePos) { return this.currentTab().getHoveredItem(mousePos); }
    @Nullable
    @Override
    public Pair<FluidStack,ScreenArea> getHoveredFluid(ScreenPosition mousePos) { return this.currentTab().getHoveredFluid(mousePos); }

}