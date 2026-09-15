package io.github.lightman314.lightmanscurrency.api.client.gui.widget;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IRenderTick;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public abstract class FancyWidget extends AbstractWidget implements IRenderTick {

    /**
     * Used to manually trigger child widgets within a specified scroll area<br>
     * Will use {@link #alreadyRendered()} to flag the widget as having been rendered already so that the widget doesn't render twice.<br>
     * It's expected that the widget/screen calling this method has set up some form of scissor that matches the given screen area
     * @param gui
     * @param area
     * @param widgets
     * @param modifyVisibility Whether we should override the widgets visibility status
     */
    public static void renderWidgetsInArea(FancyGuiExtractor gui,ScreenArea area,List<? extends FancyWidget> widgets,boolean modifyVisibility) {
        for(FancyWidget w : widgets)
        {
            if(area.overlaps(w.getArea()) && (modifyVisibility || w.isVisible()))
            {
                gui.push(w.getPosition());
                if(modifyVisibility)
                    w.visible = true;
                w.extractRenderState(gui,w.getArea());
                gui.pop();
            }
            else if(modifyVisibility)
                w.visible = false;
            //Trigger "already rendered" regardless of whether it's visible this frame or not
            w.alreadyRendered();
        }
    }

    //Position Data
    private ScreenArea area;

    public final ScreenArea getArea() { return this.area; }
    public final ScreenPosition getPosition() { return this.area.pos; }
    public final void setPosition(ScreenPosition position) { this.area = this.area.atPosition(position); }


    private Optional<ScreenArea> scissorArea = Optional.empty();
    public final void setScissorArea(@Nullable ScreenArea area) {
        this.scissorArea = Optional.ofNullable(area);
    }

    @Override
    public final int getX() { return this.area.x; }
    @Override
    public final void setX(int x) { this.area = this.area.atPosition(x,this.area.y); }
    @Override
    public final int getY() { return this.area.y; }
    @Override
    public final void setY(int y) { this.area = this.area.atPosition(this.area.x,y); }
    @Override
    public final void setPosition(int x, int y) { this.area = this.area.atPosition(x,y); }
    @Override
    public final int getWidth() { return this.area.width; }
    @Override
    public final void setWidth(int width) { this.setSize(width,this.getHeight()); }
    @Override
    public final int getHeight() { return this.area.height; }
    @Override
    public final void setHeight(int height) { this.setSize(this.getWidth(),height); }
    @Override
    public final int getRight() { return this.area.right(); }
    @Override
    public final int getBottom() { return this.area.bottom(); }
    @Override
    public final void setSize(int width, int height) {
        this.area = this.area.ofSize(width,height);
        this.width = width;
        this.height = height;
    }
    @Override
    public final ScreenRectangle getRectangle() { return new ScreenRectangle(this.area.x,this.area.y,this.area.width,this.area.height); }
    @Override
    public final void setRectangle(int width, int height, int x, int y) { this.area = ScreenArea.of(x,y,width,height); }

    private Optional<Boolean> oldVisibleState = Optional.empty();
    private boolean alreadyRendered = false;
    public final boolean isVisible() { return this.visible; }
    public final void hideThisFrame() {
        this.oldVisibleState = Optional.of(this.visible);
        this.visible = false;
    }
    public final void alreadyRendered() { this.alreadyRendered = true; }

    //Easy Widget data
    private final Function<FancyWidget,Boolean> activeCheck;
    private final Function<FancyWidget,Boolean> visibleCheck;
    private final TooltipSource tooltip;

    protected FancyWidget(AbstractBuilder<?,?> builder)
    {
        super(builder.area.x,builder.area.y,builder.area.width,builder.area.height,builder.message);
        this.area = builder.area;
        this.activeCheck = builder.activeCheck;
        this.visibleCheck = builder.visibleCheck;
        this.tooltip = builder.tooltip;
    }


    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if(this.scissorArea.isPresent() && !this.scissorArea.get().isInArea(mouseX,mouseY))
            return false;
        return super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public final boolean renderTickLate() { return false; }

    @Override
    public final void renderTick(ScreenPosition mousePos) {
        //Update active and visible status
        this.active = this.activeCheck.apply(this);
        //Restore visibility from single-frame hiding
        if(this.oldVisibleState.isPresent())
        {
            this.visible = this.oldVisibleState.get();
            this.oldVisibleState = Optional.empty();
        }
        this.visible = this.visibleCheck.apply(this);
        this.renderTickInternal(mousePos);
    }

    /**
     * Called every time the {@link #extractRenderState(GuiGraphicsExtractor, int, int, float)} is called
     * immediately after the active and visible states are checked.<br>
     * Called regardles of whether this widget is actually visible
     */
    protected void renderTickInternal(ScreenPosition mousePos) {}

    //Only non-final so that EasyButton can add the cursor handling
    @Override
    @ApiStatus.Internal
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        //Don't render again if it's already been rendered this frame
        if(this.alreadyRendered)
        {
            this.alreadyRendered = false;
            return;
        }
        FancyGuiExtractor gui = new FancyGuiExtractor(graphics,mouseX,mouseY,a);
        this.extractRenderState(gui.push(this.getPosition()),this.area);
        gui.pop();
        //Try and add the tooltip
        if(this.isHovered())
        {
            List<Component> tooltips = this.collectTooltips(gui.getMousePos());
            if(tooltips != null && !tooltips.isEmpty())
                gui.renderPositionedTooltip(tooltips,this.getTooltipPositioner());
        }
    }
    protected final ClientTooltipPositioner getTooltipPositioner()
    {
        //Copied from WidgetTooltipHolder#createTooltipPositioner, so tooltips
        return !this.isHovered && this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()
                ? new BelowOrAboveWidgetTooltipPositioner(this.getRectangle())
                : new MenuTooltipPositioner(this.getRectangle());
    }

    @Nullable
    protected List<Component> collectTooltips(ScreenPosition mousePos) {
        return this.tooltip.collectToolips(this,mousePos);
    }

    protected abstract void extractRenderState(FancyGuiExtractor gui,ScreenArea area);

    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) { return false; }

    @Override
    public void playDownSound(SoundManager soundManager) { }

    //Just leave the default implementation empty since I rarely mess with narration
    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) { }

    public static abstract class AbstractBuilder<T extends AbstractBuilder<T,X>,X extends FancyWidget> implements IWidgetBuilder<X>
    {
        private ScreenArea area;
        protected final ScreenArea getArea() { return this.area; }
        private Component message = Component.empty();
        private Function<FancyWidget,Boolean> activeCheck = w -> w.active;
        private Function<FancyWidget,Boolean> visibleCheck = w -> w.visible;
        private TooltipSource tooltip = TooltipSource.EMPTY;

        protected AbstractBuilder() { this(100,20); }
        protected AbstractBuilder(int width, int height) { this.area = ScreenArea.of(0,0,width,height); }

        protected final void setSize(int width,int height) { this.area = this.area.ofSize(width,height); }
        protected final void setWidth(int width) { this.area = this.area.ofSize(width,this.area.height); }
        protected final void setHeight(int height) { this.area = this.area.ofSize(this.area.width,height); }

        protected abstract T getSelf();

        public final T atPos(int x, int y) { return this.atPos(ScreenPosition.of(x,y)); }
        public final T atPos(ScreenPosition position) { this.area = this.area.atPosition(position); return this.getSelf(); }

        public final T alwaysActive() { return this.active(() -> true); }
        public final T active(BooleanSupplier active) { return this.active1(w -> active.getAsBoolean()); }
        public final T active(UnaryOperator<Boolean> active) { return this.active1(w -> active.apply(w.active)); }
        public final T active1(Function<FancyWidget,Boolean> active) { this.activeCheck = active; return this.getSelf(); }

        public final T visible(BooleanSupplier visible) { return this.visible1(w -> visible.getAsBoolean()); }
        public final T visible(UnaryOperator<Boolean> visible) { return this.visible1(w -> visible.apply(w.visible)); }
        public final T visible1(Function<FancyWidget,Boolean> visible) { this.visibleCheck = visible; return this.getSelf(); }

        public final T matchWith(FancyWidget parent) { return this.active(parent::isActive).visible(parent::isVisible); }

        public final T tooltip(TooltipSource tooltip) { this.tooltip = tooltip; return this.getSelf(); }

        public final T withNarrationMessage(Component message) { this.message = message; return this.getSelf(); }

    }

    public static abstract class FlexibleSizeBuilder<T extends FlexibleSizeBuilder<T,X>,X extends FancyWidget> extends AbstractBuilder<T,X>
    {
        protected FlexibleSizeBuilder() { }
        protected FlexibleSizeBuilder(int width, int height) { super(width, height); }

        public final T ofSize(int width,int height) { this.setSize(width,height); return this.getSelf(); }
        public final T ofWidth(int width) { this.setWidth(width); return this.getSelf(); }
        public final T ofHeight(int height) { this.setHeight(height); return this.getSelf(); }

    }

}
