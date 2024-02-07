package io.github.lightman314.lightmanscurrency.client.gui.easy;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.*;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class EasyScreen extends Screen implements IEasyScreen {

    private final List<IPreRender> preRenders = new ArrayList<>();
    private final List<ILateRender> lateRenders = new ArrayList<>();
    private final List<IEasyTickable> guiTickers = new ArrayList<>();
    private final List<ITooltipSource> tooltipSources = new ArrayList<>();
    private final List<IScrollListener> scrollListeners = new ArrayList<>();
    private final List<IMouseListener> mouseListeners = new ArrayList<>();


    @Nonnull
    @Override
    public final Font getFont() { return this.font; }
    @Nonnull
    @Override
    public final Player getPlayer() { return this.minecraft.player; }

    private ScreenArea screenArea = ScreenArea.of(0, 0, 100, 100);

    protected EasyScreen() { this(EasyText.empty()); }
    protected EasyScreen(Component title) { super(title); }

    @Override
    public boolean isPauseScreen() { return false; }

    @Nonnull
    @Override
    public final ScreenArea getArea() { return this.screenArea; }
    public final int getGuiLeft() { return this.screenArea.x; }
    public final  int getGuiTop() { return this.screenArea.y; }
    @Nonnull
    public final  ScreenPosition getCorner() { return this.screenArea.pos; }
    public final  int getXSize() { return this.screenArea.width; }
    public final  int getYSize() { return this.screenArea.height; }

    protected final void resize(int width, int height) { this.screenArea = this.screenArea.ofSize(width, height); this.recalculateCorner(); }

    @Override
    protected final void init() {
        this.preRenders.clear();
        this.lateRenders.clear();
        this.guiTickers.clear();
        this.tooltipSources.clear();
        this.scrollListeners.clear();
        this.mouseListeners.clear();
        super.init();
        this.recalculateCorner();
        this.initialize(this.screenArea);
    }

    protected void recalculateCorner() { this.screenArea = this.screenArea.atPosition(ScreenPosition.of((this.width - this.screenArea.width) / 2,(this.height - this.screenArea.height) / 2)); }

    protected abstract void initialize(ScreenArea screenArea);

    @Override
    public final void render(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        this.renderTick();
        EasyGuiGraphics gui = EasyGuiGraphics.create(pose, this.font, mouseX, mouseY, partialTicks).pushOffset(this.getCorner());
        //Trigger Pre-Render ticks
        for(IPreRender r : ImmutableList.copyOf(this.preRenders))
            r.preRender(gui);
        //Render background tint
        this.renderBackground(pose);
        //Render background
        this.renderBG(gui);
        //Render Widgets
        super.render(pose, mouseX, mouseY, partialTicks);
        //Render Late Renders
        for(ILateRender r : ImmutableList.copyOf(this.lateRenders))
            r.lateRender(gui);
        //Render After Widgets
        this.renderAfterWidgets(gui);
        //Render Tooltips
        EasyScreenHelper.RenderTooltips(gui, ImmutableList.copyOf(this.tooltipSources));
        //Render After Tooltips
        this.renderAfterTooltips(gui);
    }

    protected void renderTick() {}

    protected abstract void renderBG(@Nonnull EasyGuiGraphics gui);

    protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {}

    protected void renderAfterTooltips(@Nonnull EasyGuiGraphics gui) {}

    @Override
    public final <T> T addChild(T child) {
        if(child instanceof EasyWidgetWithChildren w)
        {
            w.pairWithScreen(this::addChild, this::removeChild);
            if(w.addChildrenBeforeThis())
                w.addChildren();
        }
        if(child instanceof Widget r && !this.renderables.contains(child))
            this.renderables.add(r);
        if(child instanceof GuiEventListener && child instanceof NarratableEntry)
            super.addWidget((GuiEventListener & NarratableEntry)child);
        IEasyTickable ticker = EasyScreenHelper.getWidgetTicker(child);
        if(ticker != null && !this.guiTickers.contains(ticker))
            this.guiTickers.add(ticker);
        if(child instanceof ITooltipSource t && !this.tooltipSources.contains(t))
            this.tooltipSources.add(t);
        if(child instanceof IMouseListener l && !this.mouseListeners.contains(l))
            this.mouseListeners.add(l);
        if(child instanceof IScrollListener l && !this.scrollListeners.contains(l))
            this.scrollListeners.add(l);
        if(child instanceof IPreRender r && !this.preRenders.contains(r))
            this.preRenders.add(r);
        if(child instanceof ILateRender r && !this.lateRenders.contains(r))
            this.lateRenders.add(r);
        if(child instanceof EasyWidget w)
            w.addAddons(this::addChild);
        if(child instanceof EasyWidgetWithChildren w && !w.addChildrenBeforeThis())
            w.addChildren();
        return child;
    }

    @Override
    public final void removeChild(Object child) {
        if(child instanceof Widget r)
            this.renderables.remove(r);
        if(child instanceof GuiEventListener l)
            super.removeWidget(l);
        IEasyTickable ticker = EasyScreenHelper.getWidgetTicker(child);
        this.guiTickers.remove(ticker);
        if(child instanceof ITooltipSource t)
            this.tooltipSources.remove(t);
        if(child instanceof IMouseListener l)
            this.mouseListeners.remove(l);
        if(child instanceof IScrollListener l)
            this.scrollListeners.remove(l);
        if(child instanceof EasyWidget w)
            w.removeAddons(this::removeChild);
        if(child instanceof IPreRender r)
            this.preRenders.remove(r);
        if(child instanceof ILateRender r)
            this.lateRenders.remove(r);
        if(child instanceof EasyWidgetWithChildren w)
            w.removeChildren();
    }

    @Override
    public final void tick() {
        super.tick();
        for(IEasyTickable t : ImmutableList.copyOf(this.guiTickers))
            t.tick();
        this.screenTick();
    }

    protected void screenTick() {}

    @Nonnull
    @Override
    @Deprecated
    protected final <T extends GuiEventListener & NarratableEntry> T addWidget(@Nonnull T widget) { return this.addChild(widget); }

    @Nonnull
    @Override
    @Deprecated
    protected final <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(@Nonnull T widget) { return this.addChild(widget); }

    @Nonnull
    @Override
    @Deprecated
    protected final <T extends Widget> T addRenderableOnly(@Nonnull T widget) { return this.addChild(widget); }

    @Override
    @Deprecated
    protected final void removeWidget(@Nonnull GuiEventListener widget) { this.removeChild(widget); }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        for(IScrollListener l : ImmutableList.copyOf(this.scrollListeners))
        {
            if(l.mouseScrolled(mouseX, mouseY, scroll))
                return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(IMouseListener l : ImmutableList.copyOf(this.mouseListeners))
        {
            if(l.onMouseClicked(mouseX, mouseY, button))
                return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for(IMouseListener l : ImmutableList.copyOf(this.mouseListeners))
        {
            if(l.onMouseReleased(mouseX, mouseY, button))
                return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
        InputConstants.Key mouseKey = InputConstants.getKey(p_97765_, p_97766_);
        //Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.blockInventoryClosing()) {
            return true;
        }
        //Otherwise close the screen
        else if(this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey))
        {
            this.onClose();
            return true;
        }
        return super.keyPressed(p_97765_, p_97766_, p_97767_);
    }

}
