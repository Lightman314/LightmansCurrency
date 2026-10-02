package io.github.lightman314.lightmanscurrency.api.client.gui.screen.generic;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.GhostSlot;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.IngredientResult;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.IFancyScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.*;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerClient;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class FancyScreen extends Screen implements IFancyScreen {

    private final List<ILateRenderer> lateRenderers = new ArrayList<>();
    private final List<ITickerClient> tickers = new ArrayList<>();
    private final List<IRenderTick> renderTicks = new ArrayList<>();
    private final List<IScrollListener> scrollListeners = new ArrayList<>();
    private final List<IMouseListener> mouseListeners = new ArrayList<>();
    private final List<IRemovalListener> removalListeners = new ArrayList<>();

    private Optional<Pair<Integer,Integer>> size = Optional.empty();
    private ScreenArea area = ScreenArea.ZERO;

    public FancyScreen(Component title) { super(title); }
    public FancyScreen(Component title,int width,int height) { super(title); this.size = Optional.of(Pair.of(width,height)); }

    private void recalculateCorner() {
        if(this.size.isPresent()) {
            Pair<Integer,Integer> s = this.size.get();
            this.area = this.area.atPosition((this.width - s.getFirst()) / 2,(this.height - s.getSecond()) / 2);
        }
        else
            this.area = ScreenPosition.ZERO.asArea(this.width,this.height);
    }

    protected final ScreenArea changeSize(int width,int height) {
        this.size = Optional.of(Pair.of(width,height));
        this.recalculateCorner();
        return this.area;
    }

    @Override
    public ScreenPosition getCorner() { return this.area.pos; }
    @Override
    public ScreenArea getArea() { return this.area; }
    @Override
    public int getWidth() { return this.area.width; }
    @Override
    public int getHeight() { return this.area.height; }
    @Override
    public Optional<IngredientResult> getHoveredIngredient(ScreenPosition mousePos) { return Optional.empty(); }
    @Override
    public List<GhostSlot<?>> getGhostSlots() { return List.of(); }

    @Override
    protected final void init() {
        this.recalculateCorner();
        this.initialize(this.area);
    }

    protected abstract void initialize(ScreenArea area);

    @Override
    public <T> T addChild(T child) {
        //Builder
        if(child instanceof IWidgetBuilder<?> builder) {
            LightmansCurrency.LogWarning("Received a builder instead of the desired object!",new Throwable());
            this.addChild(builder.build());
            return child;
        }
        //Widget with child
        if(child instanceof IMultiWidget w) {
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
        if(child instanceof IRemovalListener l)
            this.removalListeners.add(l);
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
        if(child instanceof IRemovalListener l) {
            this.removalListeners.remove(l);
            l.afterWidgetRemoval();
        }
    }

    @Override
    public void removeAllChildren() { this.clearWidgets(); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float a) {
        //Call the render tick A.S.A.P
        FancyGuiExtractor gui = new FancyGuiExtractor(graphics,mouseX,mouseY,a);
        this.deployRenderTick(gui.getMousePos());
        gui.push(this.getCorner());
        this.extractBackground(gui,this.area);
        super.extractRenderState(graphics, mouseX, mouseY, a);
        //Render the late widgets
        for(ILateRenderer r : new ArrayList<>(this.lateRenderers))
            r.extractLateRender(gui);
    }

    protected abstract void extractBackground(FancyGuiExtractor gui,ScreenArea area);

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
    public final void tick() {
        for(ITickerClient ticker : new ArrayList<>(this.tickers))
            ticker.clientTick();
        this.clientTick();
    }
    protected void clientTick() {}

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
        for(IRemovalListener l : new ArrayList<>(this.removalListeners))
            l.afterWidgetRemoval();
        this.removalListeners.clear();
    }

}
