package io.github.lightman314.lightmanscurrency.client.gui.screen.easy;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.options.IEasyScreenOptions;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class EasyScreen extends Screen implements IEasyScreen {

    private int width;
    private int height;
    public final int width() { return this.width; }
    public final int height() { return this.height; }
    private int guiLeft;
    private int guiTop;
    public final int guiLeft() { return this.guiLeft; }
    public final int guiTop() {return this.guiTop; }
    public final void changeSize(int width, int height) {
        this.width = width;
        this.height = height;
        this.calculateEdges();
    }

    private void calculateEdges() {
        this.guiLeft = (super.width - this.width) / 2;
        this.guiTop = (super.height - this.height) / 2;
    }

    @Override
    public final Font getFont() { return this.font; }

    public final ResourceLocation texture;

    protected EasyScreen(IEasyScreenOptions options) {
        super(options.getTitle());
        this.texture = options.getTexture();
        this.changeSize(options.getWidth(), options.getHeight());
    }

    private final List<Widget> renderables = new ArrayList<>();
    private final List<GuiEventListener> children = new ArrayList<>();
    private final List<IMouseListener> mouseListeners = new ArrayList<>();
    private final List<ITooltipSource> tooltipSources = new ArrayList<>();
    private final List<Runnable> tickers = new ArrayList<>();


    @Override
    protected final void init() {
        super.init();
        this.calculateEdges();
        this.renderables.clear();
        this.children.clear();
        this.mouseListeners.clear();
        this.tooltipSources.clear();
        this.tickers.clear();
        LightmansCurrency.LogInfo("Initializing screen.");
        this.initialize();
        LightmansCurrency.LogInfo("Finished initializing screen.");
    }

    @Override //Assume no pausing by default
    public boolean isPauseScreen() { return false; }

    protected abstract void initialize();

    public final <T extends Widget> @NotNull T addRenderableOnly(@NotNull T renderable) {
        if(!this.renderables.contains(renderable))
            this.renderables.add(renderable);
        this.widgetAddedCheck(renderable);
        return renderable;
    }

    public final <T extends Widget & GuiEventListener> @NotNull T addRenderableWidget(@NotNull T widget) {
        if(!this.renderables.contains(widget))
            this.renderables.add(widget);
        if(!this.children.contains(widget))
            this.children.add(widget);
        this.widgetAddedCheck(widget);
        return widget;
    }

    public final <T extends GuiEventListener> @NotNull T addGuiListener(@NotNull T listener) {
        if(!this.children.contains(listener))
            this.children.add(listener);
        this.widgetAddedCheck(listener);
        return listener;
    }

    public final void addMouseListener(@NotNull IMouseListener listener) { if(!this.mouseListeners.contains(listener)) this.mouseListeners.add(listener); }

    public final void addTooltipSource(@NotNull ITooltipSource tooltipSource) { if(!this.tooltipSources.contains(tooltipSource)) this.tooltipSources.add(tooltipSource); }

    public final void addTicker(@NotNull Runnable ticker) { if(!this.tickers.contains(ticker)) this.tickers.add(ticker); }

    protected final void widgetAddedCheck(@NotNull Object widget) {
        Runnable ticker = IEasyScreen.CollectTicker(widget);
        if(ticker != null)
            this.addTicker(ticker);
        if(widget instanceof ITooltipSource tooltipSource)
            this.addTooltipSource(tooltipSource);
        if(widget instanceof IMouseListener listener)
            this.addMouseListener(listener);
    }

    public final void removeChild(@NotNull Object widget) {
        if(widget instanceof Widget r)
            this.renderables.remove(r);
        if(widget instanceof GuiEventListener l)
            this.children.remove(l);
        if(widget instanceof IMouseListener l)
            this.mouseListeners.remove(l);
        if(widget instanceof ITooltipSource t)
            this.tooltipSources.remove(t);
        Runnable ticker = IEasyScreen.CollectTicker(widget);
        if(ticker != null)
            this.tickers.remove(ticker);
    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTick) {
        //Draw background
        this.drawBackground(pose, mouseX, mouseY, partialTick);
        //Draw renderables
        this.drawRenderables(pose, mouseX, mouseY, partialTick);
        //Draw anything that needs to be drawn above the widgets, but before the tooltips
        this.renderAfterWidgets(pose, mouseX, mouseY, partialTick);
        //Draw tooltips
        this.drawTooltips(pose, mouseX, mouseY);
        //Draw anything that needs to be drawn after the tooltips
        this.renderAfterTooltips(pose, mouseX, mouseY, partialTick);
    }

    protected final void drawBackground(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        //Draw gray background
        this.renderBackground(pose);
        //Draw screen-specific background
        RenderSystem.setShaderTexture(0, this.texture);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        this.renderBackground(pose, mouseX, mouseY, partialTick);
    }

    protected final void drawRenderables(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        for(Widget r : this.renderables)
            r.render(pose, mouseX, mouseY, partialTick);
    }

    protected final void drawTooltips(PoseStack pose, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        for(ITooltipSource t : this.tooltipSources)
        {
            List<Component> tooltips = t.getTooltip(mouseX, mouseY);
            if(tooltips != null && tooltips.size() > 0)
                tooltip.addAll(tooltips.stream().filter(Objects::nonNull).toList());
        }
        if(tooltip.size() > 0)
        {
            List<FormattedCharSequence> text = new ArrayList<>();
            for(Component t : tooltip)
                text.addAll(this.font.split(t, 166));
            this.renderTooltip(pose, text, mouseX, mouseY);
        }
    }

    protected final void drawSimpleBackground(@NotNull PoseStack pose) {
        this.blit(pose, this.guiLeft, this.guiTop, 0, 0, this.width, this.height);
    }

    protected abstract void renderBackground(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTick);

    protected void renderAfterWidgets(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) { }
    protected void renderAfterTooltips(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public final void tick() {
        for(int i = 0; i < this.tickers.size(); ++i)
        {
            Runnable t = this.tickers.get(i);
            t.run();
            if(t != this.tickers.get(i))
                i--;
        }
    }

    @Override
    public final @NotNull List<GuiEventListener> children() { return this.children; }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(IMouseListener ml : this.mouseListeners)
        {
            if(ml.onMouseClicked(mouseX, mouseY, button))
                return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for(IMouseListener ml : this.mouseListeners)
        {
            if(ml.onMouseReleased(mouseX, mouseY, button))
                return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

}