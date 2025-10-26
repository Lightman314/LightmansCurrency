package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class OptionWidget extends EasyWidgetWithChildren implements ITooltipWidget {

    public static final int WIDTH = 300;
    public static final int HEIGHT = 20;
    public static final int HALF_WIDTH = WIDTH / 2;

    protected final ConfigOption<?> option;
    private final Consumer<Object> changeConsumer;
    protected OptionWidget(OptionBuilder<?> builder) {
        super(builder);
        this.option = builder.option;
        this.changeConsumer = builder.changeConsumer;
    }

    private final List<Pair<AbstractWidget,ScreenPosition>> children = new ArrayList<>();

    protected final <T> T addChildAtRelativePosition(T child, ScreenPosition position)
    {
        if(child instanceof AbstractWidget aw)
            this.children.add(Pair.of(aw,position));
        return this.addChild(child);
    }

    @Override
    protected void renderTick() {
        for(var pair : this.children)
        {
            AbstractWidget widget = pair.getFirst();
            widget.visible = this.isVisible();
            if(this.isVisible())
            {
                ScreenPosition newPos = pair.getSecond().offset(this.getPosition());
                widget.setPosition(newPos.x,newPos.y);
                //LightmansCurrency.LogDebug("Current Position:" + this.getPosition() + " Widget Offset: " + pair.getSecond() + " New Position: " + newPos + " Actual New Position: " + widget.getX() + "," + widget.getY());
            }
        }
    }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) {
        Component name = this.option.getDisplayName();
        int width = gui.font.width(name);
        gui.drawShadowed(name,HALF_WIDTH - width - 5, 5, 0xFFFFFF);
    }

    @Override
    public List<Component> getTooltipText() { return TooltipHelper.splitTooltips(this.option.getCommentTooltips()); }

    @Override
    public List<Component> getTooltipText(int mouseX, int mouseY) {
        ScreenArea area = this.getArea();
        ScreenArea leftArea = area.ofSize(area.width / 2,area.height);
        if(leftArea.isMouseInArea(mouseX,mouseY))
            return this.getTooltipText();
        return null;
    }

    protected final void changeValue(Object newValue) { this.changeConsumer.accept(newValue); }

    protected abstract static class OptionBuilder<T extends OptionBuilder<T>> extends EasyBuilder<T>
    {
        private final ConfigOption<?> option;
        private final Consumer<Object> changeConsumer;
        protected OptionBuilder(ConfigOption<?> option, Consumer<Object> changeConsumer, Supplier<Boolean> active) {
            super(WIDTH,HEIGHT);
            this.option = option;
            this.changeConsumer = changeConsumer;
            this.addon(EasyAddonHelper.activeCheck(active));
        }
    }

}
