package io.github.lightman314.lightmanscurrency.client.gui.widget.notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NotificationDisplayWidget extends EasyWidgetWithChildren implements IScrollable, ITooltipWidget {

	public static final ResourceLocation GUI_TEXTURE =  VersionUtil.lcResource("textures/gui/notifications.png");

	public static final int HEIGHT_PER_ROW = 22;

	private final Supplier<Boolean> showGeneralMessage;
	private final Supplier<List<Notification>> notificationSource;
	private final int rowCount;
	public boolean colorIfUnseen = false;
	public int backgroundColor = 0xFFC6C6C6;

	public static int CalculateHeight(int rowCount) { return rowCount * HEIGHT_PER_ROW; }

	private List<Notification> getNotifications() { return this.notificationSource.get(); }

	List<Component> tooltip = null;
	List<EasyButton> deleteButtons = new ArrayList<>();

	private Consumer<Integer> deletionHandler = null;
	private Supplier<Boolean> canDelete = () -> false;
	public void setDeletionHandler(@Nullable Consumer<Integer> deletionHandler, @Nullable Supplier<Boolean> canDelete) {
		this.deletionHandler = deletionHandler;
		if(canDelete == null)
			this.canDelete = () -> false;
		else
			this.canDelete = canDelete;
	}

	public NotificationDisplayWidget(@Nonnull ScreenPosition pos, int width, int rowCount, @Nonnull Supplier<List<Notification>> notificationSource) { this(pos.x, pos.y, width, rowCount, notificationSource); }
	public NotificationDisplayWidget(@Nonnull ScreenPosition pos, int width, int rowCount, @Nonnull Supplier<List<Notification>> notificationSource, @Nonnull Supplier<Boolean> showGeneralMessage) { this(pos.x, pos.y, width, rowCount, notificationSource, showGeneralMessage); }
	public NotificationDisplayWidget(int x, int y, int width, int rowCount, @Nonnull Supplier<List<Notification>> notificationSource) { this(x,y,width,rowCount,notificationSource,() -> false); }
	public NotificationDisplayWidget(int x, int y, int width, int rowCount, @Nonnull Supplier<List<Notification>> notificationSource, @Nonnull Supplier<Boolean> showGeneralMessage) {
		super(x, y, width, CalculateHeight(rowCount));
		this.notificationSource = notificationSource;
		this.showGeneralMessage = showGeneralMessage;
		this.rowCount = rowCount;
	}

	@Override
	public NotificationDisplayWidget withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

	@Override
	public void addChildren() {
		this.deleteButtons = null;
		for(int i = 0; i < this.rowCount; ++i)
		{
			final int row = i;
			this.addChild(new IconButton(this.getPosition().offset(this.width - 21,1 + (i * HEIGHT_PER_ROW)),b -> this.deleteNotification(row), IconUtil.ICON_X)
					.withAddons(EasyAddonHelper.visibleCheck(deleteButtonVisible(row)),
							EasyAddonHelper.tooltip(LCText.TOOLTIP_NOTIFICATION_DELETE)));
		}
	}

	private Supplier<Boolean> deleteButtonVisible(int row)
	{
		return () -> {
			if(this.canDelete.get())
				return this.getNotifications().size() > row;
			return false;
		};
	}

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui) {
		this.validateScroll();

		this.tooltip = null;
		boolean showGeneral = this.showGeneralMessage.get();

		List<Notification> notifications = this.getNotifications();
		int index = this.scroll;

		boolean deletingEnabled = this.canDelete.get();

		gui.fill(0, 0, this.width, this.height, this.backgroundColor);

		for(int y = 0; y < this.rowCount && index < notifications.size(); ++y)
		{
			int yPos = y * HEIGHT_PER_ROW;
			Notification n = notifications.get(index++);

			//Draw the background
			RenderSystem.setShaderTexture(0, GUI_TEXTURE);
			gui.resetColor();
			int vPos = !n.wasSeen() && this.colorIfUnseen ? 222 : 200;
			gui.blit(GUI_TEXTURE, 0, yPos, 0, vPos, 2, HEIGHT_PER_ROW);
			int xPos = 2;
			while(xPos < this.width - 2)
			{
				int thisWidth = Math.min(166, this.width - 2 - xPos);
				gui.blit(GUI_TEXTURE, xPos, yPos, 2, vPos, thisWidth, HEIGHT_PER_ROW);
				xPos += thisWidth;
			}
			gui.blit(GUI_TEXTURE, this.width - 2, yPos, 168, 200, 2, HEIGHT_PER_ROW);

			//Draw the text
			int textXPos = 2;
			int textWidth = this.width - 4;
			if(deletingEnabled) //Shrink the text width by 20 if the delete button will be visible
				textWidth -= 20;
			int textColor = n.wasSeen() ? 0xFFFFFF : 0x000000;
			if(n.getCount() > 1)
			{
				//Render quantity text
				String countText = String.valueOf(n.getCount());
				int quantityWidth = gui.font.width(countText);
				gui.blit(GUI_TEXTURE, 1 + quantityWidth, yPos, 170, vPos, 3, HEIGHT_PER_ROW);

				gui.drawString(countText, textXPos, yPos + (HEIGHT_PER_ROW / 2) - (gui.font.lineHeight / 2), textColor);

				textXPos += quantityWidth + 2;
				textWidth -= quantityWidth + 2;
			}

			Component message = showGeneral ? n.getGeneralMessage() : n.getMessage();
			//Draw the lines
			List<FormattedCharSequence> lines = gui.font.split(message, textWidth);
			if(lines.size() == 1)
				gui.drawString(lines.get(0), textXPos, yPos + (HEIGHT_PER_ROW / 2) - (gui.font.lineHeight / 2), textColor);
			else
			{
				for(int l = 0; l < lines.size() && l < 2; ++l)
					gui.drawString(lines.get(l), textXPos, yPos + 2 + l * 10, textColor);
			}
			//Collect the tooltips
			if(this.tooltip == null && gui.mousePos.x >= this.getX() && gui.mousePos.x < this.getX() + this.width && gui.mousePos.y >= this.getY() + yPos && gui.mousePos.y < this.getY() + yPos + HEIGHT_PER_ROW)
			{
				this.tooltip = new ArrayList<>();
				if(n.hasTimeStamp())
					this.tooltip.add(n.getTimeStampMessage());
				if(lines.size() > 2)
					this.tooltip.addAll(TooltipHelper.splitTooltips(message));
			}
		}

	}

	@Override
	public List<Component> getTooltipText()
	{
		if(this.tooltip != null && !this.tooltip.isEmpty())
			return this.tooltip;
		return null;
	}

	private int scroll = 0;

	@Override
	public int currentScroll() { return this.scroll; }

	@Override
	public void setScroll(int newScroll) {
		this.scroll = newScroll;
		this.validateScroll();
	}

	@Override
	public int getMaxScroll() { return Math.max(0, this.getNotifications().size() - this.rowCount); }

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		this.handleScrollWheel(scroll);
		return true;
	}

	private void deleteNotification(int buttonRow)
	{
		int notificationRow = buttonRow + this.scroll;
		if(this.deletionHandler != null)
			this.deletionHandler.accept(notificationRow);
	}

}