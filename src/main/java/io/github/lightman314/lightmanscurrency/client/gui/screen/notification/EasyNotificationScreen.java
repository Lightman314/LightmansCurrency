package io.github.lightman314.lightmanscurrency.client.gui.screen.notification;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientNotificationData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.EasyScreenTabbed;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.options.EasyScreenTabbedOptions;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs.EasyTabRotation;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs.TabOverflowHandler;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications.MarkAsSeenButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tabs.EasyTabButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.notifications.MessageFlagNotificationsSeen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EasyNotificationScreen extends EasyScreenTabbed<EasyNotificationScreen,NotificationTab> {

    public EasyNotificationScreen() {
        super(EasyScreenTabbedOptions.create()
                .withTabOverflowHandler(TabOverflowHandler.CreateScrolling(-20, 0, 20, 200))
                .withTexture(new ResourceLocation(LightmansCurrency.MODID, "textures/gui/notifications.png"))
                .withSize(200,200)
        );
    }

    public final NotificationData getNotifications() { return ClientNotificationData.GetNotifications(); }

    public final List<NotificationCategory> getCategories() {
        List<NotificationCategory> categories = Lists.newArrayList(NotificationCategory.GENERAL);
        categories.addAll(this.getNotifications().getCategories().stream().filter(NotificationCategory::notGeneral).toList());
        return categories;
    }

    Button buttonMarkAsSeen;

    @Override
    protected void initialize() {
        super.initialize();
        LightmansCurrency.LogInfo("Initializing notification screen.");
        this.buttonMarkAsSeen = this.addChild(new MarkAsSeenButton(this.guiLeft() + this.width() - 15, this.guiTop() + 4, Component.translatable("gui.button.notifications.mark_read"), this::markAsRead));
        LightmansCurrency.LogInfo("Finished initializing notification screen.");
    }

    @Override
    protected void onTick() {
        NotificationTab tab = this.getOpenTab();
        if(tab != null)
            this.buttonMarkAsSeen.active = this.getNotifications().unseenNotification(tab.category);
        else
            this.buttonMarkAsSeen.active = false;
    }

    @Override
    protected int getTabButtonLimit() {
        return 10;
    }

    @Override
    protected @NotNull ScreenPosition getTabButtonPosition(int displayIndex) { return ScreenPosition.of(-EasyTabButton.SIZE, displayIndex * EasyTabButton.SIZE).offset(this); }

    @Override
    protected @NotNull EasyTabRotation getTabButtonRotation(int displayIndex) { return EasyTabRotation.LEFT; }

    @Override
    protected @NotNull List<NotificationTab> createTabs() {
        List<NotificationCategory> categories = this.getCategories();
        ImmutableList<NotificationTab> oldTabs = this.currentTabs();
        NotificationTab tab = this.getOpenTab();
        NotificationCategory currentCategory = tab == null ? NotificationCategory.GENERAL : tab.category;
        List<NotificationTab> result = new ArrayList<>();
        for(NotificationCategory cat : categories)
        {
            NotificationTab oldTab = getTabOfCategory(oldTabs, cat);
            result.add(Objects.requireNonNullElseGet(oldTab, () -> new NotificationTab(this, cat)));
        }
        int categoryIndex = result.indexOf(getTabOfCategory(result, currentCategory));
        if(categoryIndex != this.getOpenTabIndex())
            this.changeTab(Math.max(0,categoryIndex));
        LightmansCurrency.LogInfo("EasyNotificationScreen generated " + result.size() + " tabs");
        return result;
    }

    @Nullable
    protected static NotificationTab getTabOfCategory(List<NotificationTab> tabs, NotificationCategory category) {
        for(NotificationTab tab : tabs)
        {
            if(tab.category.matches(category))
                return tab;
        }
        return null;
    }

    @Override
    protected void renderBackground(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTick) {
        this.drawSimpleBackground(pose);
    }

    private void markAsRead(Button button) {
        LightmansCurrencyPacketHandler.instance.sendToServer(new MessageFlagNotificationsSeen(this.getOpenTab().category));
    }

}
