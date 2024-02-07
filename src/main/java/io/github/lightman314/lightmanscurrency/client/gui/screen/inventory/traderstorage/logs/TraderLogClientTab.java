package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.logs;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.notifications.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.logs.TraderLogTab;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TraderLogClientTab extends TraderStorageClientTab<TraderLogTab> {


    public TraderLogClientTab(Object screen, TraderLogTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_SHOW_LOGGER; }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.trader.log"); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.addChild(new NotificationDisplayWidget(screenArea.pos.offset(5, 10), screenArea.width - 10, 5, this::getNotifications));

        this.menu.SetCoinSlotsActive(false);

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    @Override
    public void closeAction() { this.menu.SetCoinSlotsActive(true); }

    private List<Notification> getNotifications()
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return trader.getNotifications();
        return new ArrayList<>();
    }

}
