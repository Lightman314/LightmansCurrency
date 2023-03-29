package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.logs;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.notifications.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.logs.TraderLogTab;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TraderLogClientTab extends TraderStorageClientTab<TraderLogTab> {


    public TraderLogClientTab(TraderStorageScreen screen, TraderLogTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_SHOW_LOGGER; }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.trader.log"); }

    @Override
    public boolean tabButtonVisible() { return true; }

    @Override
    public boolean blockInventoryClosing() { return false; }

    private NotificationDisplayWidget logDisplay;

    @Override
    public void onOpen() {

        this.logDisplay = this.screen.addRenderableTabWidget(new NotificationDisplayWidget(this.screen.getGuiLeft() + 5, this.screen.getGuiTop() + 10, this.screen.getXSize() - 10, 5, this.font, this::getNotifications));

        this.menu.SetCoinSlotsActive(false);

    }

    @Override
    public void onClose() {
        this.menu.SetCoinSlotsActive(true);
    }

    private List<Notification> getNotifications()
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return trader.getNotifications();
        return new ArrayList<>();
    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {
        this.logDisplay.tryRenderTooltip(pose, this.screen, mouseX, mouseY);
    }
}
