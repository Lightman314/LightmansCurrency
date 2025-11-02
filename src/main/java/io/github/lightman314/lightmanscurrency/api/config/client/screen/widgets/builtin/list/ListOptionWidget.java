package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.ListScreenSettings;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.OptionWidget;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
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

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ListOptionWidget extends EasyWidgetWithChildren implements ITooltipWidget {

    public static final int WIDTH = OptionWidget.WIDTH;
    public static final int HEIGHT = OptionWidget.HEIGHT;
    public static final int HALF_WIDTH = OptionWidget.HALF_WIDTH;

    protected final ConfigOption<?> option;
    protected final int index;
    private final ListScreenSettings listSettings;
    protected ListOptionWidget(ListOptionBuilder<?> builder) {
        super(builder);
        this.option = builder.option;
        this.index = builder.index;
        this.listSettings = builder.settings;
    }

    private final List<Pair<AbstractWidget,ScreenPosition>> children = new ArrayList<>();

    @Override
    public final void addChildren(ScreenArea area) {
        this.addChildAtRelativePosition(IconButton.builder()
                        .icon(IconUtil.ICON_X)
                        .pressAction(this::removeValue)
                        .addon(EasyAddonHelper.tooltip(LCText.CONFIG_OPTION_LIST_REMOVE))
                        .addon(EasyAddonHelper.visibleCheck(this.listSettings::canRemoveEntry))
                        .build(),
                ScreenPosition.of(WIDTH - 20,0));
        this.addMoreChildren(area);
    }

    protected abstract void addMoreChildren(ScreenArea area);


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
        Component name = LCText.CONFIG_OPTION_LIST_ENTRY.get(this.index + 1);
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

    protected final void changeValue(Object newValue) {
        //LightmansCurrency.LogDebug("Changing list option " + this.index + " to " + newValue);
        this.listSettings.setEntry(this.index,newValue);
    }

    protected final void removeValue() {
        if(this.listSettings.canRemoveEntry())
        {
            //LightmansCurrency.LogDebug("Removing list option at " + this.index);
            this.listSettings.removeEntry(this.index);
        }
    }

    protected abstract static class ListOptionBuilder<T extends ListOptionBuilder<T>> extends EasyBuilder<T>
    {
        private final ConfigOption<?> option;
        protected final int index;
        private final ListScreenSettings settings;
        protected ListOptionBuilder(ConfigOption<?> option, int index, ListScreenSettings settings) {
            super(WIDTH,HEIGHT);
            this.option = option;
            this.index = index;
            this.settings = settings;
        }
    }

}