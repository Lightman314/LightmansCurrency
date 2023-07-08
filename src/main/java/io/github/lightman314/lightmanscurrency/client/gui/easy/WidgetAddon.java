package io.github.lightman314.lightmanscurrency.client.gui.easy;

import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;

public abstract class WidgetAddon {

    private EasyWidget widget;
    public final void attach(EasyWidget widget) { this.widget = widget; }
    protected final EasyWidget getWidget() { return this.widget; }

    public void renderTick() {}

    public void visibleTick() {}

    public void activeTick() {}

}