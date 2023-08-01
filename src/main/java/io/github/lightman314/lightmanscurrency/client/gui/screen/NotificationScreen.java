package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientNotificationData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications.MarkAsSeenButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications.NotificationTabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.notifications.MessageFlagNotificationsSeen;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nonnull;

public class NotificationScreen extends EasyScreen implements IScrollable {

	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/notifications.png");
	
	public final NotificationData getNotifications() { return ClientNotificationData.GetNotifications(); }

	private static final int SCREEN_WIDTH = 200;
	private static final int SCREEN_HEIGHT = 200;
	
	public final int TABS_PER_PAGE = 8;
	
	public final int NOTIFICATIONS_PER_PAGE = 8;
	public final int NOTIFICATION_HEIGHT = 22;
	
	List<NotificationTabButton> tabButtons;
	int tabScroll = 0;
	NotificationCategory selectedCategory = NotificationCategory.GENERAL;
	
	ScrollBarWidget notificationScroller = null;
	
	EasyButton buttonMarkAsSeen;
	
	int notificationScroll = 0;
	
	public NotificationScreen() { super(); this.resize(SCREEN_WIDTH + TabButton.SIZE, SCREEN_HEIGHT); }
	
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
			this.tabButtons.add(this.addChild(new NotificationTabButton(this::SelectTab, this::getNotifications, cat)));
		this.positionTabButtons();
		
		this.notificationScroller = this.addChild(new ScrollBarWidget(screenArea.pos.offset(screenArea.width - 15, 15), this.NOTIFICATIONS_PER_PAGE * this.NOTIFICATION_HEIGHT, this));
		
		this.buttonMarkAsSeen = this.addChild(new MarkAsSeenButton(screenArea.x + screenArea.width - 15, screenArea.y + 4, EasyText.translatable("gui.button.notifications.mark_read"), this::markAsRead)
				.withAddons(EasyAddonHelper.activeCheck(() -> this.getNotifications().unseenNotification(this.selectedCategory))));
		
		this.tick();
		
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
	
	private void positionTabButtons() {
		this.tabScroll = Math.min(this.tabScroll, this.getMaxTabScroll());
		int startIndex = this.tabScroll;
		ScreenPosition pos = this.getCorner();
		List<NotificationCategory> categories = this.getCategories();
		for(int i = 0; i < this.tabButtons.size(); ++i)
		{
			TabButton tab = this.tabButtons.get(i);
			if(i >= startIndex && i < startIndex + TABS_PER_PAGE)
			{
				tab.visible = true;
				tab.reposition(pos, 3);
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

	private Component cachedTooltip = null;

	@Override
	protected void renderBG(@Nonnull EasyGuiGraphics gui) {

		//Render the background
		int screenLeft = TabButton.SIZE;
		gui.resetColor();
		gui.blit(GUI_TEXTURE, screenLeft, 0, 0, 0, SCREEN_WIDTH, this.getYSize());

		//Render the current notifications
		this.notificationScroll = Math.min(this.notificationScroll, this.getMaxNotificationScroll());
		List<Notification> notifications = this.getNotifications().getNotifications(this.selectedCategory);
		this.cachedTooltip = null;
		int index = this.notificationScroll;
		for(int y = 0; y < NOTIFICATIONS_PER_PAGE && index < notifications.size(); ++y)
		{
			Notification not = notifications.get(index++);
			int yPos = 15 + y * NOTIFICATION_HEIGHT;
			gui.resetColor();

			int vPos = not.wasSeen() ? this.getYSize() : this.getYSize() + NOTIFICATION_HEIGHT;
			int textColor = not.wasSeen() ? 0xFFFFFF : 0x000000;

			gui.blit(GUI_TEXTURE, screenLeft + 15, yPos, 0, vPos, 170, NOTIFICATION_HEIGHT);
			int textXPos = screenLeft + 17;
			int textWidth = 166;
			if(not.getCount() > 1)
			{
				//Render quantity text
				String countText = String.valueOf(not.getCount());
				int quantityWidth = this.font.width(countText);
				gui.blit(GUI_TEXTURE, screenLeft + 16 + quantityWidth, yPos, 170, vPos, 3, NOTIFICATION_HEIGHT);

				gui.drawString(countText, textXPos, yPos + (NOTIFICATION_HEIGHT / 2) - (font.lineHeight / 2), textColor);

				textXPos += quantityWidth + 2;
				textWidth -= quantityWidth + 2;
			}
			Component message = this.selectedCategory == NotificationCategory.GENERAL ? not.getGeneralMessage() : not.getMessage();
			List<FormattedCharSequence> lines = this.font.split(message, textWidth);
			if(lines.size() == 1)
			{
				gui.drawString(lines.get(0), textXPos, yPos + (NOTIFICATION_HEIGHT / 2) - (this.font.lineHeight / 2), textColor);
			}
			else
			{
				for(int l = 0; l < lines.size() && l < 2; ++l)
					gui.drawString(lines.get(l), textXPos, yPos + 2 + l * 10, textColor);
				//Set the message as a tooltip if it's too large to fit and the mouse is hovering over the notification
				if(this.cachedTooltip == null && this.getCorner().offset(screenLeft + 15, yPos).isMouseInArea(gui.mousePos, 170, NOTIFICATION_HEIGHT))
				{
					if(lines.size() > 2)
					{
						if(not.hasTimeStamp())
							this.cachedTooltip = EasyText.empty().append(not.getTimeStampMessage()).append(EasyText.literal("\n")).append(message);
						else
							this.cachedTooltip = message;
					}
					else if(not.hasTimeStamp())
						this.cachedTooltip = not.getTimeStampMessage();
				}
			}
		}

	}

	@Override
	protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		if(this.cachedTooltip != null)
			gui.renderTooltip(gui.font.split(this.cachedTooltip, 170));
	}
	
	private void SelectTab(EasyButton button) {
		int tabIndex = -1;
		if(button instanceof NotificationTabButton)
			tabIndex = this.tabButtons.indexOf(button);
		if(tabIndex >= 0)
		{
			List<NotificationCategory> categories = this.getCategories();
			if(tabIndex < categories.size())
			{
				NotificationCategory newCategory = categories.get(tabIndex);
				if(!newCategory.matches(this.selectedCategory))
				{
					this.selectedCategory = newCategory;
					//Reset notification scroll as a new category was selected
					this.notificationScroll = 0;
					this.positionTabButtons();
				}
			}
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
	
	public boolean notificationScrolled(double delta) {
		if(delta < 0)
		{			
			if(this.notificationScroll < this.getMaxNotificationScroll())
				this.notificationScroll++;
			else
				return false;
		}
		else if(delta > 0)
		{
			if(this.notificationScroll > 0)
				this.notificationScroll--;
			else
				return false;
		}
		return true;
	}

	public void markAsRead(EasyButton button) {
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageFlagNotificationsSeen(this.selectedCategory));
	}
	
	@Override
	public int currentScroll() { return this.notificationScroll; }
	@Override
	public void setScroll(int newScroll) { this.notificationScroll = newScroll; }
	@Override
	public int getMaxScroll() { return this.getMaxNotificationScroll(); }
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		//If mouse is over the screen, scroll the notifications
		if(this.getCorner().offset(TabButton.SIZE, 0).isMouseInArea(mouseX, mouseY, this.getXSize() - TabButton.SIZE, this.getYSize()))
		{
			if(this.notificationScrolled(delta))
				return true;
			//Don't scroll the tabs while the mouse is over the center of the screen.
			return super.mouseScrolled(mouseX, mouseY, delta);
		}
		else if(this.tabScrolled(delta)) //Otherwise scroll the tabs
			return true;
		return super.mouseScrolled(mouseX, mouseY, delta);
	}
	
	@Override
	public boolean keyPressed(int key, int scanCode, int mods) {
		InputConstants.Key mouseKey = InputConstants.getKey(key, scanCode);
		//Manually close the screen when hitting the inventory key
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
			this.minecraft.setScreen(null);
			return true;
		}
		return super.keyPressed(key, scanCode, mods);
	}
	
}
