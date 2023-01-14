package io.github.lightman314.lightmanscurrency.client.gui.screen.notification;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class NotificationTab extends EasyTab<EasyNotificationScreen> implements ScrollBarWidget.IScrollable, GuiEventListener, ITooltipSource {

    public static final int NOTIFICATIONS_PER_PAGE = 8;
    public static final int NOTIFICATION_HEIGHT = 22;

    public final NotificationCategory category;

    @Override
    public @NotNull IconData getTabIcon() { return this.category.getIcon(); }

    private int scroll = 0;

    public NotificationTab(@NotNull EasyNotificationScreen screen, @NotNull NotificationCategory category) { super(screen); this.category =category; }

    public final List<Notification> getNotifications() { return this.screen.getNotifications().getNotifications(this.category); }
    ScrollBarWidget notificationScroller = null;

    @Override
    protected void initializeTab() {
        this.notificationScroller = this.addRenderableWidget(new ScrollBarWidget(this.guiLeft() + this.width() - 15, this.guiTop() + 15, NOTIFICATIONS_PER_PAGE * NOTIFICATION_HEIGHT, this));
    }

    private List<Component> tooltip = new ArrayList<>();

    @Override
    public void renderTab(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        this.notificationScroller.beforeWidgetRender(mouseY);

        //Render the current notifications
        this.scroll = Math.min(this.scroll, this.getMaxScroll());
        List<Notification> notifications = this.getNotifications();
        this.tooltip = new ArrayList<>();
        int index = this.scroll;
        for(int y = 0; y < NOTIFICATIONS_PER_PAGE && index < notifications.size(); ++y)
        {
            Notification not = notifications.get(index++);
            int yPos = this.guiTop() + 15 + y * NOTIFICATION_HEIGHT;
            RenderSystem.setShaderTexture(0, this.screen.texture);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            int vPos = not.wasSeen() ? this.height() : this.height() + NOTIFICATION_HEIGHT;
            int textColor = not.wasSeen() ? 0xFFFFFF : 0x000000;

            this.screen.blit(pose, this.guiLeft() + 15, yPos, 0, vPos, 170, NOTIFICATION_HEIGHT);
            int textXPos = this.guiLeft() + 17;
            int textWidth = 166;
            if(not.getCount() > 1)
            {
                //Render quantity text
                String countText = String.valueOf(not.getCount());
                int quantityWidth = this.getFont().width(countText);
                this.screen.blit(pose, this.guiLeft() + 16 + quantityWidth, yPos, 170, vPos, 3, NOTIFICATION_HEIGHT);

                this.getFont().draw(pose, countText, textXPos, yPos + (NOTIFICATION_HEIGHT / 2f) - (this.getFont().lineHeight / 2f), textColor);

                textXPos += quantityWidth + 2;
                textWidth -= quantityWidth + 2;
            }
            Component message = this.category == NotificationCategory.GENERAL ? not.getGeneralMessage() : not.getMessage();
            List<FormattedCharSequence> lines = this.getFont().split(message, textWidth);
            if(lines.size() == 1)
            {
                this.getFont().draw(pose, lines.get(0), textXPos, yPos + (NOTIFICATION_HEIGHT / 2f) - (this.getFont().lineHeight / 2f), textColor);
            }
            else
            {
                for(int l = 0; l < lines.size() && l < 2; ++l)
                    this.getFont().draw(pose, lines.get(l), textXPos, yPos + 2 + l * 10, textColor);
                //Set the message as a tooltip if it's too large to fit and the mouse is hovering over the notification
                if(this.tooltip.size() == 0 && mouseX >= this.guiLeft() + 15 && mouseX < this.guiLeft() + 185 && mouseY >= yPos && mouseY < yPos + NOTIFICATION_HEIGHT)
                {
                    if(not.hasTimeStamp())
                        this.tooltip.add(not.getTimeStampMessage());
                    if(lines.size() > 2)
                        this.tooltip.add(message);
                }
            }
        }
    }

    @Override
    public int currentScroll() { return this.scroll;}
    @Override
    public void setScroll(int newScroll) { this.scroll = MathUtil.clamp(newScroll, 0, this.getMaxScroll()); }
    @Override
    public int getMaxScroll() { return Math.max(0, this.getNotifications().size() - NOTIFICATIONS_PER_PAGE); }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) { return this.handleScrollWheel(delta); }

    @Override
    public List<Component> getTooltip(int mouseX, int mouseY) { return this.tooltip; }

}