package io.github.lightman314.lightmanscurrency.client.gui.widget.notifications;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

public class NotificationDisplayWidget extends EasyWidget implements IScrollable, ITooltipWidget {

	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/notifications.png");
	
	public static final int HEIGHT_PER_ROW = 22;
	
	private final NonNullSupplier<List<Notification>> notificationSource;
	private final int rowCount;
	public boolean colorIfUnseen = false;
	public int backgroundColor = 0xFFC6C6C6;
	
	public static int CalculateHeight(int rowCount) { return rowCount * HEIGHT_PER_ROW; }
	
	private List<Notification> getNotifications() { return this.notificationSource.get(); }
	
	Component tooltip = null;
	
	public NotificationDisplayWidget(ScreenPosition pos, int width, int rowCount, NonNullSupplier<List<Notification>> notificationSource) { this(pos.x, pos.y, width, rowCount, notificationSource); }
	public NotificationDisplayWidget(int x, int y, int width, int rowCount, NonNullSupplier<List<Notification>> notificationSource) {
		super(x, y, width, CalculateHeight(rowCount));
		this.notificationSource = notificationSource;
		this.rowCount = rowCount;
	}

	@Override
	public NotificationDisplayWidget withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }



	@Override
	public void renderWidget(@NotNull EasyGuiGraphics gui) {
		this.validateScroll();
		
		this.tooltip = null;
		
		List<Notification> notifications = this.getNotifications();
		int index = this.scroll;

		gui.fill(0, 0, this.width, this.height, this.backgroundColor);
		
		for(int y = 0; y < this.rowCount && index < notifications.size(); ++y)
		{
			int yPos = y * HEIGHT_PER_ROW;
			Notification n = notifications.get(index++);
			
			//Draw the background
			RenderSystem.setShaderTexture(0, GUI_TEXTURE);
			gui.resetColor();
			int vPos = n.wasSeen() && this.colorIfUnseen ? 222 : 200;
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
			
			Component message = n.getMessage();
			List<FormattedCharSequence> lines = gui.font.split(message, textWidth);
			if(lines.size() == 1)
			{
				gui.drawString(lines.get(0), textXPos, yPos + (HEIGHT_PER_ROW / 2) - (gui.font.lineHeight / 2), textColor);
			}
			else
			{
				for(int l = 0; l < lines.size() && l < 2; ++l)
					gui.drawString(lines.get(l), textXPos, yPos + 2 + l * 10, textColor);
				if(this.tooltip == null && gui.mousePos.x >= this.getX() && gui.mousePos.x < this.getX() + this.width && gui.mousePos.y >= yPos && gui.mousePos.y < yPos + HEIGHT_PER_ROW)
				{
					if(lines.size() > 2)
					{
						if(n.hasTimeStamp())
							this.tooltip = EasyText.empty().append(n.getTimeStampMessage()).append(EasyText.literal("\n")).append(message);
						else
							this.tooltip = message;
					}
					else if(n.hasTimeStamp())
						this.tooltip = n.getTimeStampMessage();
				}
			}
			
		}
		
	}

	@Override
	public List<Component> getTooltipText()
	{
		if(this.tooltip != null)
			return ImmutableList.of(this.tooltip);
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
	
	private void validateScroll() { this.scroll = MathUtil.clamp(this.scroll, 0, this.getMaxScroll()); }

	@Override
	public int getMaxScroll() { return Math.max(0, this.getNotifications().size() - this.rowCount); }

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		this.handleScrollWheel(scroll);
		return true;
	}
	
	 public void playDownSound(@NotNull SoundManager manager) {}
}
