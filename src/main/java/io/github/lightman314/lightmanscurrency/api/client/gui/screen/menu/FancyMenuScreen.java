package io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.GhostSlot;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.IngredientResult;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.*;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerClient;
import io.github.lightman314.lightmanscurrency.api.world.menu.FancyMenu;
import io.github.lightman314.lightmanscurrency.mixin.client.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class FancyMenuScreen<M extends FancyMenu> extends AbstractContainerScreen<M> implements IFancyScreen {

    private ScreenArea area = ScreenArea.ZERO;

    private final List<ILateRenderer> lateRenderers = new ArrayList<>();
    private final List<ITickerClient> tickers = new ArrayList<>();
    private final List<IRenderTick> renderTicks = new ArrayList<>();
    private final List<IScrollListener> scrollListeners = new ArrayList<>();
    private final List<IMouseListener> mouseListeners = new ArrayList<>();
    private final List<IRecipeIngredientViewer> ingredientViewers = new ArrayList<>();
    private final List<IGhostSlotProvider> ghostSlotProviders = new ArrayList<>();
    private final List<IAreaClaim> areaClaims = new ArrayList<>();
    public List<ScreenArea> getAreaClaims() {
        List<ScreenArea> result = new ArrayList<>();
        for(IAreaClaim claim : new ArrayList<>(this.areaClaims)) {
            Optional<ScreenArea> optional = claim.getClaimedArea();
            if(optional.isPresent())
                result.add(optional.get());
        }
        return result;
    }

    public Font getFont() { return Minecraft.getInstance().font; }

    public FancyMenuScreen(M menu,Inventory inventory) { this(menu,inventory,Component.empty()); }
    public FancyMenuScreen(M menu,Inventory inventory,Component title) { super(menu,inventory,title); }
    public FancyMenuScreen(M menu,Inventory inventory,int width,int height) { this(menu,inventory,Component.empty(),width,height); }
    public FancyMenuScreen(M menu,Inventory inventory,Component title,int width,int height) {
        super(menu,inventory,title,width,height);
        this.area = this.area.ofSize(width,height);
    }

    private void recalculateCorner() {
        this.area = this.area.atPosition((this.width - this.area.width) / 2,(this.height - this.area.height) / 2);
        //Reposition vanilla corners to match
        this.leftPos = this.area.x;
        this.topPos = this.area.y;
    }

    protected final ScreenArea changeSize(int width,int height)
    {
        this.area = this.area.ofSize(width,height);
        if(this instanceof AbstractContainerScreenAccessor a)
        {
            a.setImageWidth(width);
            a.setImageHeight(height);
        }
        this.recalculateCorner();
        this.inventoryLabelY = this.area.height - 94;
        return this.area;
    }

    @Override
    public final ScreenPosition getCorner() { return this.area.pos; }
    @Override
    public final ScreenArea getArea() { return this.area; }
    @Override
    public final int getWidth() { return this.area.width; }
    @Override
    public final int getHeight() { return this.area.height; }

    @Override
    protected final void init() {
        super.init();
        this.recalculateCorner();
        this.initialize(this.area);
    }

    protected abstract void initialize(ScreenArea area);

    @Override
    public final <T> T addChild(T child)
    {
        //Builder
        if(child instanceof IWidgetBuilder<?> builder)
        {
            LightmansCurrency.LogWarning("Received a builder instead of the desired object!",new Throwable());
            this.addChild(builder.build());
            return child;
        }
        //Widget with children
        if(child instanceof IMultiWidget w)
        {
            w.defineParent(this);
            w.addEarlyChildren();
        }
        if(child instanceof Renderable r)
            super.addRenderableOnly(r);
        if(child instanceof GuiEventListener && child instanceof NarratableEntry)
            super.addWidget((GuiEventListener & NarratableEntry)child);
        if(child instanceof IRenderTick t)
            this.renderTicks.add(t);
        if(child instanceof ITickerClient t)
            this.tickers.add(t);
        if(child instanceof ILateRenderer r)
            this.lateRenderers.add(r);
        if(child instanceof IScrollListener l)
            this.scrollListeners.add(l);
        if(child instanceof IMouseListener l)
            this.mouseListeners.add(l);
        if(child instanceof IRecipeIngredientViewer v)
            this.ingredientViewers.add(v);
        if(child instanceof IGhostSlotProvider g)
            this.ghostSlotProviders.add(g);
        if(child instanceof IAreaClaim a)
            this.areaClaims.add(a);
        //Add late children afterward
        if(child instanceof IMultiWidget w)
            w.addLateChildren();
        if(child instanceof IWidgetWrapper wrapper)
            this.addChild(wrapper.getWrappedWidget());
        return child;
    }

    @Override
    public final void removeChild(Object child)
    {
        //On the removal side, we only care about the IWidgetHolder interface,
        //not the IMultiWidget interface that allows us to add widgets
        if(child instanceof IWidgetHolder w)
            w.removeAllChildren();
        if(child instanceof IWidgetWrapper w)
            this.removeChild(w.getWrappedWidget());
        if(child instanceof Renderable r)
            this.renderables.remove(r);
        if(child instanceof GuiEventListener l)
            super.removeWidget(l);//This also removes the narratable entries, it just doesn't require it on the removal side for some odd reason...
        if(child instanceof IRenderTick t)
            this.renderTicks.remove(t);
        if(child instanceof ITickerClient t)
            this.tickers.remove(t);
        if(child instanceof ILateRenderer r)
            this.lateRenderers.remove(r);
        if(child instanceof IScrollListener l)
            this.scrollListeners.remove(l);
        if(child instanceof IMouseListener l)
            this.mouseListeners.remove(l);
        if(child instanceof IGhostSlotProvider g)
            this.ghostSlotProviders.remove(g);
        if(child instanceof IRecipeIngredientViewer v)
            this.ingredientViewers.remove(v);
        if(child instanceof IAreaClaim c)
            this.areaClaims.remove(c);
    }

    @Override
    public final void removeAllChildren() {
        this.clearWidgets();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        //Call the render tick A.S.A.P.
        FancyGuiExtractor gui = new FancyGuiExtractor(graphics,mouseX,mouseY,a);
        this.deployRenderTick(gui.getMousePos());
        gui.push(this.getCorner());
        this.extractBackground(gui,this.area);
        super.extractRenderState(graphics, mouseX, mouseY, a);
        //Render the late widgets
        for(ILateRenderer r : new ArrayList<>(this.lateRenderers))
            r.extractLateRender(gui);
    }

    private void deployRenderTick(ScreenPosition mousePos) {
        //Copy the list just in case the ticker adds or removes widgets
        List<IRenderTick> copy = new ArrayList<>(this.renderTicks);
        //Deploy both the "early" and "late" phase of the render ticks
        this.deployRenderTick(false,copy,mousePos);
        this.deployRenderTick(true,copy,mousePos);
    }
    private void deployRenderTick(boolean late,List<IRenderTick> list,ScreenPosition mousePos) {
        for(IRenderTick t : list)
        {
            if(t.renderTickLate() == late)
                t.renderTick(mousePos);
        }
    }

    @Override
    protected final void containerTick() {
        for(ITickerClient ticker : new ArrayList<>(this.tickers))
            ticker.clientTick();
        this.clientTick();
    }
    protected void clientTick() {}

    protected abstract void extractBackground(FancyGuiExtractor gui, ScreenArea area);

    protected boolean blockInventoryMenuClosing() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(event)) && this.blockInventoryMenuClosing()) {
            //Only send to the relevant widget, otherwise abort as there shouldn't be anything else listening to this key
            return this.getFocused() != null && this.getFocused().keyPressed(event);
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        for(IScrollListener listener : new ArrayList<>(this.scrollListeners))
        {
            if(listener.onMouseScrolled((int)x,(int)y,scrollX,scrollY))
                return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for(IMouseListener listener : new ArrayList<>(this.mouseListeners))
        {
            if(listener.onMouseClicked(event,doubleClick))
                return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        for(IMouseListener listener : new ArrayList<>(this.mouseListeners))
        {
            if(listener.onMouseDragged(event,dx,dy))
                return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for(IMouseListener listener : new ArrayList<>(this.mouseListeners))
        {
            if(listener.onMouseReleased(event))
                return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    @Deprecated
    protected final <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) { return this.addChild(widget); }
    @Override
    @Deprecated
    protected final <T extends GuiEventListener & NarratableEntry> T addWidget(T widget) { return this.addChild(widget); }
    @Override
    @Deprecated
    protected final <T extends Renderable> T addRenderableOnly(T renderable) { return this.addChild(renderable); }
    @Override
    protected final void removeWidget(GuiEventListener widget) { this.removeChild(widget); }
    @Override
    @OverridingMethodsMustInvokeSuper
    protected void clearWidgets() {
        super.clearWidgets();
        this.lateRenderers.clear();
        this.tickers.clear();
        this.renderTicks.clear();
        this.scrollListeners.clear();
        this.mouseListeners.clear();
        this.ingredientViewers.clear();
        this.ghostSlotProviders.clear();
        this.areaClaims.clear();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public Optional<IngredientResult> getHoveredIngredient(ScreenPosition mousePos) {
        for(IRecipeIngredientViewer viewer : new ArrayList<>(this.ingredientViewers)) {
            Optional<IngredientResult> result = viewer.getHoveredIngredient(mousePos);
            if(result.isPresent())
                return result;
        }
        return Optional.empty();
    }

    @Override
    public List<GhostSlot<?>> getGhostSlots() {
        List<GhostSlot<?>> result = new ArrayList<>();
        for(IGhostSlotProvider provider : new ArrayList<>(this.ghostSlotProviders)) {
            List<GhostSlot<?>> l = provider.getGhostSlots();
            if(l != null)
                result.addAll(l);
        }
        return result;
    }
}