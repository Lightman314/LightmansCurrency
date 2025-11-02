package io.github.lightman314.lightmanscurrency.client.gui.widget.notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import io.github.lightman314.lightmanscurrency.util.ListUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NotificationDisplayWidget extends EasyWidgetWithChildren implements IScrollable, ITooltipWidget {

	public static final ResourceLocation GUI_TEXTURE =  VersionUtil.lcResource("textures/gui/notifications.png");

	public static final int HEIGHT_PER_ROW = 22;

	private final Supplier<Boolean> showGeneralMessage;
	private final Supplier<List<Notification>> notificationSource;
	private final int rowCount;
	private final boolean colorIfUnseen;

	public static int CalculateHeight(int rowCount) { return rowCount * HEIGHT_PER_ROW; }

	private List<Notification> getNotifications() { return this.notificationSource.get(); }

	List<Component> tooltip = null;

	private Consumer<Integer> deletionHandler = null;
	private Supplier<Boolean> canDelete = () -> false;
	public void setDeletionHandler(@Nullable Consumer<Integer> deletionHandler, @Nullable Supplier<Boolean> canDelete) {
		this.deletionHandler = deletionHandler;
		if(canDelete == null)
			this.canDelete = () -> false;
		else
			this.canDelete = canDelete;
	}

	private NotificationDisplayWidget(Builder builder)
	{
		super(builder);
		this.notificationSource = builder.source;
		this.showGeneralMessage = builder.showGeneral;
		this.rowCount = builder.rowCount;
		this.colorIfUnseen = builder.colorIfUnseen;
	}

	@Override
	public void addChildren(ScreenArea area) {
		for(int i = 0; i < this.rowCount; ++i)
		{
			final int row = i;
			this.addChild(IconButton.builder()
					.position(area.pos.offset(this.width - 21, 1 + (i * HEIGHT_PER_ROW)))
					.pressAction(() -> this.deleteNotification(row))
					.icon(IconUtil.ICON_X)
					.addon(EasyAddonHelper.visibleCheck(deleteButtonVisible(row)))
					.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_NOTIFICATION_DELETE))
					.build());
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
	public void renderWidget(EasyGuiGraphics gui) {
		this.validateScroll();

		this.tooltip = null;
		boolean showGeneral = this.showGeneralMessage.get();

		List<Notification> notifications = this.getNotifications();
		int index = this.scroll;

		boolean deletingEnabled = this.canDelete.get();

		for(int y = 0; y < this.rowCount && index < notifications.size(); ++y)
		{
			int yPos = y * HEIGHT_PER_ROW;
			Notification n = notifications.get(index++);

			//Draw the background
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
			//Leaving old code here just in case I want to make the text color change again later
			//int textColor = n.wasSeen()  && this.colorIfUnseen ? 0xFFFFFF : 0x000000;
			//Text is now black in all states cause that pops out a bit more
			int textColor = 0;
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

			List<Component> message = showGeneral ? n.getGeneralMessage() : n.getMessageLines();
			//Draw the lines
			List<FormattedCharSequence> lines = new ArrayList<>();
			for(Component line : message)
				lines.addAll(gui.font.split(line, textWidth));
			if(lines.size() == 1)
				gui.drawString(lines.get(0), textXPos, yPos + (HEIGHT_PER_ROW / 2) - (gui.font.lineHeight / 2), textColor);
			else
			{
				for(int l = 0; l < lines.size() && l < 2; ++l)
					gui.drawString(lines.get(l), textXPos, yPos + 2 + l * 10, textColor);
			}
			int maxX = this.getX() + this.width;
			if(this.canDelete.get())
				maxX -= 22;
			//Collect the tooltips
			if(this.tooltip == null && gui.mousePos.x >= this.getX() && gui.mousePos.x < maxX && gui.mousePos.y >= this.getY() + yPos && gui.mousePos.y < this.getY() + yPos + HEIGHT_PER_ROW)
			{
				this.tooltip = new ArrayList<>();
				if(n.hasTimeStamp())
					this.tooltip.add(n.getTimeStampMessage());
				if(lines.size() > 2)
					this.tooltip.addAll(TooltipHelper.splitTooltips(ListUtil.convertList(message)));
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

	public static Builder builder() { return new Builder(); }

	@FieldsAreNonnullByDefault
	public static class Builder extends EasyBuilder<Builder>
	{
		private Builder() { super(100,CalculateHeight(1)); }
		@Override
		protected Builder getSelf() { return this; }

		private int rowCount = 1;
		private Supplier<List<Notification>> source = ImmutableList::of;
		private Supplier<Boolean> showGeneral = () -> false;
		private boolean colorIfUnseen = false;

		public Builder width(int width) { this.changeWidth(width); return this; }
		public Builder rowCount(int rowCount) { this.rowCount = rowCount; this.changeHeight(CalculateHeight(this.rowCount)); return this; }

		public Builder notificationSource(Supplier<List<Notification>> source) { this.source = source; return this; }

		public Builder showGeneral(Supplier<Boolean> showGeneral) { this.showGeneral = showGeneral; return this; }

		public Builder colorIfUnseen() { this.colorIfUnseen = true; return this; }

		public NotificationDisplayWidget build() { return new NotificationDisplayWidget(this); }

	}

}