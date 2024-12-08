package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.data.ClientNotificationData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.gui.widget.notifications.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications.NotificationTabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.menus.NotificationMenu;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class NotificationScreen extends EasyMenuScreen<NotificationMenu> {

	public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/notifications.png");
	
	public final NotificationData getNotifications() { return ClientNotificationData.GetNotifications(); }

	private static final int SCREEN_WIDTH = 200;
	private static final int SCREEN_HEIGHT = 200;
	
	public final int TABS_PER_PAGE = 8;
	
	public final int NOTIFICATIONS_PER_PAGE = 8;
	
	List<NotificationTabButton> tabButtons;
	int tabScroll = 0;
	NotificationCategory selectedCategory = NotificationCategory.GENERAL;

	NotificationDisplayWidget notificationDisplay;
	ScrollBarWidget notificationScroller;
	
	EasyButton buttonMarkAsSeen;
	
	public NotificationScreen(@Nonnull NotificationMenu menu, @Nonnull Inventory inventory, @Nonnull Component title) { super(menu,inventory,title); this.resize(SCREEN_WIDTH, SCREEN_HEIGHT); }
	
	public List<NotificationCategory> getCategories() {
		List<NotificationCategory> categories = Lists.newArrayList(NotificationCategory.GENERAL);
		categories.addAll(this.getNotifications().getCategories().stream().filter(cat -> cat != NotificationCategory.GENERAL).toList());
		return categories;
	}
	
	public void reinit() {
		this.clearWidgets();
		this.validateSelectedCategory();
		this.init();
	}
	
	@Override
	protected void initialize(ScreenArea screenArea) {
		
		this.tabButtons = new ArrayList<>();
		for(NotificationCategory cat : this.getCategories())
			this.tabButtons.add(this.addChild(NotificationTabButton.nBuilder()
					.pressAction(() -> this.SelectTab(cat))
					.data(this::getNotifications)
					.category(cat)
					.build()));
		this.positionTabButtons();

		this.notificationDisplay = this.addChild(NotificationDisplayWidget.builder()
				.position(screenArea.pos.offset(15,15))
				.width(screenArea.width - 30)
				.rowCount(NOTIFICATIONS_PER_PAGE)
				.notificationSource(this::getVisibleNotifications)
				.showGeneral(this::isGeneralSelected)
				.colorIfUnseen()
				.build());
		this.notificationDisplay.setDeletionHandler(this::deleteNotification,() -> !this.isGeneralSelected());

		this.notificationScroller = this.addChild(ScrollBarWidget.builder()
						.onRight(this.notificationDisplay)
						.build());

		int textWidth = this.font.width(LCText.BUTTON_NOTIFICATIONS_MARK_AS_READ.get());
		this.buttonMarkAsSeen = this.addChild(EasyTextButton.builder()
						.position(screenArea.pos.offset(screenArea.width - 19 - textWidth,4))
						.size(4 + textWidth,11)
						.text(LCText.BUTTON_NOTIFICATIONS_MARK_AS_READ)
						.pressAction(this::markAsRead)
						.addon(EasyAddonHelper.activeCheck(() -> this.getNotifications().unseenNotification(this.selectedCategory)))
						.build());
		
	}
	
	private void validateSelectedCategory() {
		List<NotificationCategory> categories = this.getCategories();
		boolean categoryFound = false;
		for(int i = 0; i < categories.size() && !categoryFound; ++i)
		{
			if(categories.get(i).matches(this.selectedCategory))
				categoryFound = true;
		}
		if(!categoryFound || this.selectedCategory == null)
			this.selectedCategory = NotificationCategory.GENERAL;
	}

	private boolean isGeneralSelected() { return this.selectedCategory == NotificationCategory.GENERAL; }
	
	private void positionTabButtons() {
		this.tabScroll = Math.min(this.tabScroll, this.getMaxTabScroll());
		int startIndex = this.tabScroll;
		ScreenPosition pos = this.getCorner().offset(-TabButton.SIZE,0);
		List<NotificationCategory> categories = this.getCategories();
		for(int i = 0; i < this.tabButtons.size(); ++i)
		{
			TabButton tab = this.tabButtons.get(i);
			if(i >= startIndex && i < startIndex + TABS_PER_PAGE)
			{
				tab.visible = true;
				tab.setPosition(pos);
				tab.setRotation(WidgetRotation.LEFT);
				if(i < categories.size()) //Use match code, as some categories are generated on get, and a new instance may have been generated due to reloading, etc.
					tab.active = !categories.get(i).matches(this.selectedCategory);
				else
					tab.active = true;
				pos = pos.offset(0, TabButton.SIZE);
			}
			else
				tab.visible = false;
		}
	}

	@Override
	protected void renderBG(@Nonnull EasyGuiGraphics gui) {

		//Render the background
		gui.resetColor();
		gui.renderNormalBackground(GUI_TEXTURE,this);

	}

	private List<Notification> getVisibleNotifications()
	{
		this.validateSelectedCategory();
		return this.getNotifications().getNotifications(this.selectedCategory);
	}
	
	private void SelectTab(NotificationCategory newCategory) {
		if(!newCategory.matches(this.selectedCategory))
		{
			this.selectedCategory = newCategory;
			//Reset notification scroll as a new category was selected
			this.notificationDisplay.setScroll(0);
			this.positionTabButtons();
		}
	}

	public int getMaxTabScroll() {
		return Math.max(0, this.tabButtons.size() - TABS_PER_PAGE);
	}
	
	public boolean tabScrolled(double delta) {
		if(delta < 0)
		{			
			if(this.tabScroll < this.getMaxTabScroll())
			{
				this.tabScroll++;
				this.positionTabButtons();
			}
			else
				return false;
		}
		else if(delta > 0)
		{
			if(this.tabScroll > 0)
			{
				this.tabScroll--;
				this.positionTabButtons();
			}
			else
				return false;
		}
		return true;
	}
	
	public int getMaxNotificationScroll() {
		return Math.max(0, this.getNotifications().getNotifications(this.selectedCategory).size() - NOTIFICATIONS_PER_PAGE);
	}

	public void markAsRead(EasyButton button) {
		this.menu.SendMessage(this.builder().setCompound("MarkAsRead", this.selectedCategory.save(this.registryAccess())));
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
		//If mouse is over the screen, scroll the notifications
		if(this.getCorner().offset(TabButton.SIZE, 0).isMouseInArea(mouseX, mouseY, this.getXSize() - TabButton.SIZE, this.getYSize()))
		{
			if(this.notificationDisplay.handleScrollWheel(deltaY))
				return true;
			//Don't scroll the tabs while the mouse is over the center of the screen.
			return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
		}
		else if(this.tabScrolled(deltaY)) //Otherwise scroll the tabs
			return true;
		return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
	}

	private void deleteNotification(int notificationIndex) {
		this.menu.SendMessage(this.builder().setInt("DeleteNotification",notificationIndex).setCompound("Category",this.selectedCategory.save(this.registryAccess())));

	}
	
}
