package io.github.lightman314.lightmanscurrency.client.features.player_data.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.tabs.TabButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.WidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.TextIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationStack;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.category.builtin.UnfilteredCategory;
import io.github.lightman314.lightmanscurrency.client.features.player_data.PlayerDataClientTab;
import io.github.lightman314.lightmanscurrency.client.features.player_data.PlayerDataScreen;
import io.github.lightman314.lightmanscurrency.features.player_data.PlayerDataMenu;
import io.github.lightman314.lightmanscurrency.features.player_data.PlayerDataTab;
import io.github.lightman314.lightmanscurrency.features.player_data.tabs.NotificationTab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class NotificationClientTab extends PlayerDataClientTab<NotificationTab> {

    public static final TabBuilder<PlayerDataMenu,NotificationTab,PlayerDataTab,PlayerDataScreen> BUILDER = NotificationClientTab::new;

    protected NotificationClientTab(PlayerDataMenu menu, NotificationTab commonTab, PlayerDataScreen screen) { super(menu, commonTab, screen); }

    @Override
    public IconData getIcon() { return ItemIcon.of(Items.WRITABLE_BOOK); }
    @Override
    public Component getName() { return NotificationTab.NAME.get(); }

    private NotificationCategory selectedCategory = UnfilteredCategory.INSTANCE;

    private NotificationDisplayWidget notificationDisplay = null;

    @Override
    protected void initialize(ScreenArea area,FancyPacketMap message) {

        //Widget Positioner and scroll arrows
        WidgetPositioner positioner = this.addChild(WidgetPositioner.leftEdge(this.getScreen(),20));
        this.addChild(IconButton.builder()
                .atPos(area.pos.offset(-20,-20))
                .withIcon(SpriteIcon.of(LCApi.id("icon/arrow_up")))
                .visible(() -> positioner.getMaxScroll() > 0)
                .onPress(positioner::decrementScroll)
                .build());
        this.addChild(IconButton.builder()
                .atPos(area.pos.offset(-20,-20))
                .withIcon(SpriteIcon.of(LCApi.id("icon/arrow_down")))
                .visible(() -> positioner.getMaxScroll() > 0)
                .onPress(positioner::incrementScroll)
                .build());

        //Add a Tab Button for each category
        List<NotificationCategory> categories = this.getCategories();
        if(!categories.contains(this.selectedCategory))
            this.selectedCategory = UnfilteredCategory.INSTANCE;
        for(NotificationCategory category : categories) {
            TabButton newButton = this.addChild(TabButton.builder()
                    .withIcon(category.getIcon())
                    .tooltip(TooltipSource.deferredSingle(category::getName))
                    .active(() -> category != this.selectedCategory)
                    .onPress(() -> this.selectedCategory = category)
                    .build());
            positioner.addWidgets(newButton);
        }

        //Add the Notification Display
        this.notificationDisplay = this.addChild(NotificationDisplayWidget.builder()
                .atPos(area.pos.offset(15,15))
                .ofWidth(area.width - 30)
                .withRows(8)
                .deletionHandler(this::deleteNotification)
                .showGeneral(this::isGeneralCategory)
                .withNotifications(this::getNotifications)
                .colorIfUnseen()
                .withOldWidget(this.notificationDisplay)
                .build());

        //Add the Mark As Read button
        int textWidth = this.getFont().width(NotificationTab.BUTTON_MARK_AS_READ.get());
        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(area.width - 19 - textWidth,4))
                .ofSize(4 + textWidth,11)
                .withText(NotificationTab.BUTTON_MARK_AS_READ)
                .onPress(this::markAsRead)
                .active(this::hasUnseenNotification)
                .build());

    }

    private List<NotificationCategory> getCategories() {
        List<NotificationCategory> result = new ArrayList<>();
        result.add(UnfilteredCategory.INSTANCE);
        result.addAll(this.getCommonTab().getNotificationReader().getCategories());
        return result;
    }

    private List<NotificationStack> getNotifications() { return this.getCommonTab().getNotifications(this.selectedCategory); }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        //Render the title
        gui.text(NotificationTab.NAME.get(),8,6,0xFF404040,false);
    }

    private boolean isGeneralCategory() { return this.selectedCategory == UnfilteredCategory.INSTANCE; }

    private boolean hasUnseenNotification() { return this.getCommonTab().getNotificationReader().hasUnseenNotification(this.selectedCategory); }

    private void deleteNotification(int index) {
        this.getCommonTab().deleteNotification(this.selectedCategory,index);
    }

    private void markAsRead() {
        this.getCommonTab().markAsRead(this.selectedCategory);
    }

}