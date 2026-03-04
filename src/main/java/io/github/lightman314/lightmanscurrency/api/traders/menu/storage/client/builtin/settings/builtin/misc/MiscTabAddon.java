package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.misc;

import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.widgets.LabelWidget;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public abstract class MiscTabAddon implements LazyPacketData.IBuilderProvider {

    public static final int DEFAULT_WIDTH = 150;

    private MiscTab tab;
    public final void setup(MiscTab tab) { this.tab = tab; }
    @Nullable
    protected final MiscTab getTab() { return this.tab; }
    @Nullable
    protected final TraderData getTrader() { return this.tab == null ? null : this.tab.menu.getTrader(); }
    @Nullable
    protected final <T extends TraderNode> T getNode(TraderNodeType<T> type) {
        TraderData trader = this.getTrader();
        return trader == null ? null : trader.getNode(type);
    }
    protected final boolean hasPermissions(String permission)
    {
        if(this.tab == null)
            return false;
        return this.tab.menu.hasPermission(permission);
    }
    protected final int getPermissionLevel(String permission) {
        if(this.tab == null)
            return 0;
        return this.tab.menu.getPermissionLevel(permission);
    }

    @Override
    public LazyPacketData.Builder builder() { return this.tab.builder(); }

    public abstract void addWidgets(SettingsSubTab tab, ScreenArea screenArea, boolean firstOpen);

    protected final void addLabel(TextEntry label) { this.addLabel(label.get());}
    protected final void addLabel(Component label)
    {
        this.addWidget(LabelWidget.builder().width(DEFAULT_WIDTH)
                .label(label)
                .build(),2);
    }

    protected final <T> T addWidget(T widget)
    {
        if(this.tab == null)
            return widget;
        this.tab.addWidget(widget);
        return widget;
    }
    protected final <T> T addWidget(T widget, int spacing)
    {
        if(this.tab == null)
            return widget;
        this.tab.addWidget(widget,spacing);
        return widget;
    }

    public void renderBG(SettingsSubTab tab, EasyGuiGraphics gui) {}
    public void renderAfterWidgets(SettingsSubTab tab, EasyGuiGraphics gui) {}

    public void tick(SettingsSubTab tab) {}

    public void onClose(SettingsSubTab tab) {}

    protected final void sendMessage(LazyPacketData.Builder message)
    {
        if(this.tab != null)
            this.tab.sendMessage(message);
    }

}
