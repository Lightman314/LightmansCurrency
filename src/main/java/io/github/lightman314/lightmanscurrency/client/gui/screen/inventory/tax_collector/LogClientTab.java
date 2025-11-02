package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.notifications.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs.LogTab;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class LogClientTab extends TaxCollectorClientTab<LogTab> {

    public LogClientTab(Object screen, LogTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_SHOW_LOGGER; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TAX_COLLECTOR_LOGS.get(); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        NotificationDisplayWidget display = this.addChild(NotificationDisplayWidget.builder()
                .position(screenArea.pos.offset(15,16))
                .width(screenArea.width - 30)
                .rowCount(7)
                .notificationSource(this::getNotifications)
                .build());

        this.addChild(ScrollBarWidget.builder()
                .onRight(display)
                .build());

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { gui.drawString(this.getTooltip(), 8, 6, 0x404040); }

    private List<Notification> getNotifications()
    {
        TaxEntry entry = this.getEntry();
        if(entry != null)
            return entry.getNotifications();
        return new ArrayList<>();
    }

}