package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.misc;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientMiscTabAddonProvider;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MiscTab extends SettingsSubTab implements IScrollable {

    public MiscTab(TraderSettingsClientTab parent) { super(parent); }

    private List<MiscTabAddon> addons = new ArrayList<>();

    public List<MiscTabAddon> getAddons() {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return IClientMiscTabAddonProvider.getMiscTabAddons(trader);
        return new ArrayList<>();
    }

    private int visibleWidgets = 1;
    private int scroll = 0;
    @Override
    public int currentScroll() { return this.scroll; }
    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; this.updateScrollableWidgets(); }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.visibleWidgets,this.widgets.size()); }

    private final List<Pair<AbstractWidget,Integer>> widgets = new ArrayList<>();

    @Override
    public IconData getIcon() { return IconUtil.ICON_SETTINGS; }

    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_MISC.get(); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.EDIT_SETTINGS); }

    @Override
    public boolean shouldRenderInventoryText() { return false; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        AtomicInteger nextYLevel = new AtomicInteger(15);

        this.widgets.clear();

        this.addons = this.getAddons();
        this.addons.forEach(a -> a.setup(this));

        this.addons.forEach(a -> a.addWidgets(this, screenArea, firstOpen));

        this.updateScrollableWidgets();

    }

    public final void addWidget(Object widget) { this.addWidget(widget,5);}
    public final void addWidget(Object widget, int spacing)
    {
        super.addChild(widget);
        if(widget instanceof AbstractWidget w)
            this.widgets.add(Pair.of(w,spacing));
    }

    public final void updateScrollableWidgets() {
        this.tick();
        int remainingSpace = 100;
        this.visibleWidgets = 0;
        int yPos = 15;
        int centerX = this.screen.getArea().centerX();
        for(int i = 0; i < this.widgets.size(); ++i)
        {
            Pair<AbstractWidget,Integer> entry = this.widgets.get(i);
            AbstractWidget widget = entry.getFirst();
            //Force update visibility checks
            if(widget instanceof EasyWidget w)
                w.isVisible();
            if(i < this.scroll)
                hideWidget(widget);
            else
            {
                if(remainingSpace <= 0)
                    hideWidget(widget);
                else if(widget.visible)
                {
                    int neededSpace = widget.getHeight();
                    int totalSpace = neededSpace + entry.getSecond();
                    if(remainingSpace < neededSpace)
                    {
                        remainingSpace = 0;
                        if(this.visibleWidgets < 1)
                        {
                            this.visibleWidgets = 1;
                            widget.setPosition(centerX - (widget.getWidth() / 2),yPos);
                        }
                        else
                            hideWidget(widget);
                    }
                    else
                    {
                        remainingSpace -= totalSpace;
                        this.visibleWidgets++;
                        widget.setPosition(centerX - (widget.getWidth() / 2),yPos);
                        yPos += totalSpace;
                    }
                }
                else //Ignore invisible widgets
                    hideWidget(widget);
            }
        }
    }

    private static void hideWidget(AbstractWidget widget) {
        widget.setPosition(0,widget.getY() * -2);
    }

    @Override
    protected void onSubtabClose() { this.addons.forEach(a -> a.onClose(this)); }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        this.addons.forEach(a -> a.renderBG(this, gui));
    }

    @Override
    public void renderAfterWidgets(EasyGuiGraphics gui) {
        this.addons.forEach(a -> a.renderAfterWidgets(this, gui));
    }

    @Override
    public void tick() { this.addons.forEach(a -> a.tick(this)); }

}
