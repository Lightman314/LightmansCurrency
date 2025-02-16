package io.github.lightman314.lightmanscurrency.client.gui.widget.easy;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class EasyWidget extends AbstractWidget {

    private ScreenArea area;

    private final List<WidgetAddon> addons = new ArrayList<>();
    private boolean lockAddons = false;

    public final ScreenArea getArea() { return this.area; }

    @Override
    public final int getX() { return this.area.x; }
    @Override
    public final void setX(int x) { this.area = this.area.atPosition(ScreenPosition.of(x, this.area.y)); }
    @Override
    public final int getY() { return this.area.y; }
    @Override
    public final void setY(int y) { this.area = this.area.atPosition(ScreenPosition.of(this.area.x, y)); }
    public final ScreenPosition getPosition() { return this.area.pos; }
    @Override
    public final void setPosition(int x, int y) { this.area = this.area.atPosition(x, y); }
    public final void setPosition(@Nonnull ScreenPosition pos) { this.area = this.area.atPosition(pos); }
    @Override
    public final int getWidth() { return this.area.width; }
    @Override
    public final int getHeight() { return this.area.height; }
    @Override
    public final void setWidth(int width) { this.area = this.area.ofSize(width, this.area.height); super.setWidth(width); }
    @Override
    public final void setHeight(int height) { this.area = this.area.ofSize(this.area.width, height); super.setHeight(height); }
    public final void setSize(int width, int height) { this.area = this.area.ofSize(width, height); super.setWidth(width); super.setHeight(height); }

    public final boolean isVisible() { this.visibleTickInternal(); return this.visible; }
    public final void setVisible(boolean visible) { this.visible = visible; }

    public final Font getFont() { return Minecraft.getInstance().font; }

    public boolean hideFromMouse() { return false; }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if(this.hideFromMouse())
            return false;
        this.activeTickInternal();
        return super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public final boolean isActive() { this.activeTickInternal(); return super.isActive(); }
    public final void setActive(boolean active) { this.active = active; }

    public final boolean isMouseOver(ScreenPosition mousePos) { return this.isMouseOver(mousePos.x, mousePos.y); }

    protected EasyWidget(@Nonnull EasyBuilder<?> builder) {
        super(builder.area.x, builder.area.y, builder.area.width, builder.area.height, EasyText.empty());
        this.area = builder.area;
        this.withAddonsInternal(builder.addons);
    }

    protected final void withAddonsInternal(@Nonnull List<WidgetAddon> addons)
    {
        if(this.lockAddons)
            return;
        for(WidgetAddon a : addons)
        {
            if(a != null && !this.addons.contains(a))
            {
                this.addons.add(a);
                a.attach(this);
            }
        }
    }

    public final void addAddons(Consumer<WidgetAddon> consumer) {
        this.lockAddons = true;
        for(WidgetAddon addon : this.addons)
            consumer.accept(addon);
    }

    public void removeAddons(Consumer<WidgetAddon> consumer) {
        for(WidgetAddon addon : this.addons)
            consumer.accept(addon);
    }

    private void visibleTickInternal() {
        this.addons.forEach(WidgetAddon::visibleTick);
    }

    private void activeTickInternal() {
        this.visibleTickInternal();
        this.addons.forEach(WidgetAddon::activeTick);
    }

    public void renderTickInternal() {
        this.activeTickInternal();
        this.addons.forEach(WidgetAddon::renderTick);
        this.renderTick();
    }

    protected void renderTick() { }

    @Override
    public final void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderTickInternal();
        super.render(gui, mouseX, mouseY, partialTicks);
    }

    @Override
    protected final void renderWidget(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        try {
            this.renderWidget(EasyGuiGraphics.create(gui, mouseX, mouseY, partialTicks).pushOffset(this.getPosition()));
        } catch (Throwable t) {
            LightmansCurrency.LogError("Error occurred while rendering " + this.getClass().getName(),t);
        }
    }

    protected abstract void renderWidget(@Nonnull EasyGuiGraphics gui);

    @Override
    protected boolean isValidClickButton(int button) { return false; }

    @Override
    public void playDownSound(@Nonnull SoundManager manager) { }

    @Override
    protected void updateWidgetNarration(@Nonnull NarrationElementOutput narrator) { }

    @FieldsAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static abstract class EasyBuilder<T extends EasyBuilder<T>>
    {

        private ScreenArea area;
        private final List<WidgetAddon> addons = new ArrayList<>();

        protected EasyBuilder() { this(20,20); }
        protected EasyBuilder(int defaultWidth,int defaultHeight) { this.area = ScreenArea.of(0,0,defaultWidth,defaultHeight); }

        protected abstract T getSelf();

        public final T position(int x, int y) { this.area = this.area.atPosition(x,y); return this.getSelf(); }
        public final T position(ScreenPosition position) { this.area = this.area.atPosition(position); return this.getSelf(); }

        protected final void changeWidth(int width) { this.area = this.area.ofSize(width,this.area.height); }
        protected final void changeHeight(int height) { this.area = this.area.ofSize(this.area.width,height); }
        protected final void changeSize(int width, int height) { this.area = this.area.ofSize(width,height); }

        public final T addon(WidgetAddon addon) { this.addons.add(addon); return this.getSelf(); }

        public final T copyFrom(EasyBuilder<?> other) {
            this.area = other.area;
            this.addons.clear();
            this.addons.addAll(other.addons);
            return this.getSelf();
        }

    }

    public static abstract class EasySizableBuilder<T extends EasySizableBuilder<T>> extends EasyBuilder<T>
    {
        protected EasySizableBuilder() { }
        protected EasySizableBuilder(int defaultWidth, int defaultHeight) { super(defaultWidth,defaultHeight); }

        public final T area(ScreenArea area) { this.changeSize(area.width,area.height); this.position(area.pos); return this.getSelf(); }
        public final T width(int width) { this.changeWidth(width); return this.getSelf(); }
        public final T height(int height) { this.changeHeight(height); return this.getSelf(); }
        public final T size(int width, int height) { this.changeSize(width,height); return this.getSelf(); }
    }

    public static void drawScrollingString(GuiGraphics gui, Font font, Component text, int startX, int startY, int stopX, int stopY, int color) {
        AbstractWidget.renderScrollingString(gui,font,text,startX,startY,stopX,stopY,color);
    }

}