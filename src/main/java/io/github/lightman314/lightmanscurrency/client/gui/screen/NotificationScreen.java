package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications.MarkAsSeenButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications.NotificationTabButton;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification.Category;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.notifications.MessageFlagNotificationsSeen;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class NotificationScreen extends Screen implements IScrollable{

	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/notifications.png");
	
	public final NotificationData getNotifications() { return ClientTradingOffice.getNotifications(); }
	
	public final int guiLeft() { return (this.width - this.xSize - TabButton.SIZE) / 2; }
	public final int guiTop() { return (this.height - this.ySize) / 2; }
	public final int xSize = 200;
	public final int ySize = 200;
	
	public final int TABS_PER_PAGE = 8;
	
	public final int NOTIFICATIONS_PER_PAGE = 8;
	public final int NOTIFICATION_HEIGHT = 22;
	
	List<NotificationTabButton> tabButtons;
	int tabScroll = 0;
	Category selectedCategory = Category.GENERAL;
	
	ScrollBarWidget notificationScroller = null;
	
	Button buttonMarkAsSeen;
	
	int notificationScroll = 0;
	
	public NotificationScreen() {
		super(new TextComponent(""));
	}
	
	@Override
	public boolean isPauseScreen() { return false; }
	
	public List<Category> getCategories() {
		List<Category> categories = Lists.newArrayList(Category.GENERAL);
		categories.addAll(this.getNotifications().getCategories().stream().filter(cat -> cat != Category.GENERAL).collect(Collectors.toList()));
		return categories;
	}
	
	public void reinit() {
		this.clearWidgets();
		this.validateSelectedCategory();
		this.init();
	}
	
	@Override
	public void init() {
		
		this.tabButtons = new ArrayList<>();
		for(Category cat : this.getCategories())
		{
			this.tabButtons.add(this.addRenderableWidget(new NotificationTabButton(this::SelectTab, this.font, this::getNotifications, cat)));
		}
		this.positionTabButtons();
		
		this.notificationScroller = this.addRenderableOnly(new ScrollBarWidget(this.guiLeft() + TabButton.SIZE + this.xSize - 15, this.guiTop() + 15, this.NOTIFICATIONS_PER_PAGE * this.NOTIFICATION_HEIGHT, this));
		
		this.buttonMarkAsSeen = this.addRenderableWidget(new MarkAsSeenButton(this.guiLeft() + this.xSize  + TabButton.SIZE - 15, this.guiTop() + 4, new TranslatableComponent("gui.button.notifications.mark_read"), this::markAsRead));
		
		this.tick();
		
	}
	
	private void validateSelectedCategory() {
		List<Category> categories = this.getCategories();
		boolean categoryFound = false;
		for(int i = 0; i < categories.size() && !categoryFound; ++i)
		{
			if(categories.get(i).matches(this.selectedCategory))
				categoryFound = true;
		}
		if(!categoryFound || this.selectedCategory == null)
			this.selectedCategory = Category.GENERAL;
	}
	
	private void positionTabButtons() {
		this.tabScroll = Math.min(this.tabScroll, this.getMaxTabScroll());
		int startIndex = this.tabScroll;
		int xPos = this.guiLeft();
		int yPos = this.guiTop();
		List<Category> categories = this.getCategories();
		for(int i = 0; i < this.tabButtons.size(); ++i)
		{
			TabButton tab = this.tabButtons.get(i);
			if(i >= startIndex && i < startIndex + TABS_PER_PAGE)
			{
				tab.visible = true;
				tab.reposition(xPos, yPos, 3);
				if(i < categories.size())
					tab.active = categories.get(i) != this.selectedCategory;
				else
					tab.active = true;
				yPos += TabButton.SIZE;
			}
			else
				tab.visible = false;
		}
	}
	
	@Override
	public void tick() {
		this.buttonMarkAsSeen.active = this.getNotifications().unseenNotification(this.selectedCategory);
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.renderBackground(pose);
		
		//Render the background
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		int screenLeft = this.guiLeft() + TabButton.SIZE;
		this.blit(pose, screenLeft, this.guiTop(), 0, 0, this.xSize, this.ySize);
		
		this.notificationScroller.beforeWidgetRender(mouseY);
		
		//Render the current notifications
		this.notificationScroll = Math.min(this.notificationScroll, this.getMaxNotificationScroll());
		List<Notification> notifications = this.getNotifications().getNotifications(this.selectedCategory);
		Component tooltip = null;
		int index = this.notificationScroll;
		for(int y = 0; y < NOTIFICATIONS_PER_PAGE && index < notifications.size(); ++y)
		{
			Notification not = notifications.get(index++);
			int yPos = this.guiTop() + 15 + y * NOTIFICATION_HEIGHT;
			RenderSystem.setShaderTexture(0, GUI_TEXTURE);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			
			int vPos = not.wasSeen() ? this.ySize : this.ySize + NOTIFICATION_HEIGHT;
			int textColor = not.wasSeen() ? 0xFFFFFF : 0x000000;
			
			this.blit(pose, screenLeft + 15, yPos, 0, vPos, 170, NOTIFICATION_HEIGHT);
			int textXPos = screenLeft + 17;
			int textWidth = 166;
			if(not.getCount() > 1)
			{
				//Render quantity text
				String countText = String.valueOf(not.getCount());
				int quantityWidth = this.font.width(countText);
				this.blit(pose, screenLeft + 16 + quantityWidth, yPos, 170, vPos, 3, NOTIFICATION_HEIGHT);
				
				this.font.draw(pose, countText, textXPos, yPos + (NOTIFICATION_HEIGHT / 2) - (font.lineHeight / 2), textColor);
				
				textXPos += quantityWidth + 2;
				textWidth -= quantityWidth + 2;
			}
			Component message = this.selectedCategory == Category.GENERAL ? not.getGeneralMessage() : not.getMessage();
			List<FormattedCharSequence> lines = this.font.split(message, textWidth);
			if(lines.size() == 1)
			{
				this.font.draw(pose, lines.get(0), textXPos, yPos + (NOTIFICATION_HEIGHT / 2) - (this.font.lineHeight / 2), textColor);
			}
			else
			{
				for(int l = 0; l < lines.size() && l < 2; ++l)
					this.font.draw(pose, lines.get(l), textXPos, yPos + 2 + l * 10, textColor);
				//Set the message as a tooltip if it's too large to fit and the mouse is hovering over the notification
				if(lines.size() > 2 && tooltip == null && mouseX >= screenLeft + 15 && mouseX < screenLeft + 185 && mouseY >= yPos && mouseY < yPos + NOTIFICATION_HEIGHT)
					tooltip = message;
			}
		}
		
		//Render widgets
		super.render(pose, mouseX, mouseY, partialTicks);
		
		//Render tooltips
		for(NotificationTabButton tab : this.tabButtons)
			tab.renderTooltip(pose, mouseX, mouseY, this);
		
		if(tooltip != null)
			this.renderTooltip(pose, this.font.split(tooltip, 170), mouseX, mouseY);
		
	}
	
	private void SelectTab(Button button) {
		int tabIndex = this.tabButtons.indexOf(button);
		if(tabIndex >= 0)
		{
			List<Category> categories = this.getCategories();
			if(tabIndex < categories.size())
			{
				Category newCategory = categories.get(tabIndex);
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

	public void markAsRead(Button button) {
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
		if(mouseX >= this.guiLeft() && mouseX < this.guiLeft() + this.xSize && mouseY  >= this.guiTop() && mouseY < this.guiTop() + this.ySize && this.notificationScrolled(delta))
		{
			if(this.notificationScrolled(delta))
				return true;
		}
		else if(this.tabScrolled(delta)) //Otherwise scroll the tabs
			return true;
		return super.mouseScrolled(mouseX, mouseY, delta);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.notificationScroller.onMouseClicked(mouseX, mouseY, button);
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.notificationScroller.onMouseReleased(mouseX, mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
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
