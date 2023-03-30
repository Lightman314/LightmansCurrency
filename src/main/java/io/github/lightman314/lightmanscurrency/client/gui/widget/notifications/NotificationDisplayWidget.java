package io.github.lightman314.lightmanscurrency.client.gui.widget.notifications;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

public class NotificationDisplayWidget extends AbstractWidget implements IScrollable {

	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/notifications.png");
	
	public static final int HEIGHT_PER_ROW = 22;
	
	private final NonNullSupplier<List<Notification>> notificationSource;
	private final Font font;
	private final int rowCount;
	public boolean colorIfUnseen = false;
	public int backgroundColor = 0xFFC6C6C6;
	
	public static int CalculateHeight(int rowCount) { return rowCount * HEIGHT_PER_ROW; }
	
	private List<Notification> getNotifications() { return this.notificationSource.get(); }
	
	Component tooltip = null;
	
	public NotificationDisplayWidget(int x, int y, int width, int rowCount, Font font, NonNullSupplier<List<Notification>> notificationSource) {
		super(x, y, width, CalculateHeight(rowCount), Component.empty());
		this.notificationSource = notificationSource;
		this.font = font;
		this.rowCount = rowCount;
	}

	@Override
	public void renderWidget(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		this.validateScroll();
		
		this.tooltip = null;
		
		List<Notification> notifications = this.getNotifications();
		int index = this.scroll;
		
		Screen.fill(pose, this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.backgroundColor);
		
		for(int y = 0; y < this.rowCount && index < notifications.size(); ++y)
		{
			int yPos = this.getY() + y * HEIGHT_PER_ROW;
			Notification n = notifications.get(index++);
			
			//Draw the background
			RenderSystem.setShaderTexture(0, GUI_TEXTURE);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			int vPos = n.wasSeen() && this.colorIfUnseen ? 222 : 200;
			blit(pose, this.getX(), yPos, 0, vPos, 2, HEIGHT_PER_ROW);
			int xPos = this.getX() + 2;
			while(xPos < this.getX() + this.width - 2)
			{
				int thisWidth = Math.min(166, this.getX() + this.width - 2 - xPos);
				blit(pose, xPos, yPos, 2, vPos, thisWidth, HEIGHT_PER_ROW);
				xPos += thisWidth;
			}
			blit(pose, this.getX() + this.width - 2, yPos, 168, 200, 2, HEIGHT_PER_ROW);
			
			//Draw the text
			int textXPos = this.getX() + 2;
			int textWidth = this.width - 4;
			int textColor = n.wasSeen() ? 0xFFFFFF : 0x000000;
			if(n.getCount() > 1)
			{
				//Render quantity text
				String countText = String.valueOf(n.getCount());
				int quantityWidth = this.font.width(countText);
				blit(pose, this.getX() + 1 + quantityWidth, yPos, 170, vPos, 3, HEIGHT_PER_ROW);
				
				this.font.draw(pose, countText, textXPos, yPos + (HEIGHT_PER_ROW / 2) - (this.font.lineHeight / 2), textColor);
				
				textXPos += quantityWidth + 2;
				textWidth -= quantityWidth + 2;
			}
			
			Component message = n.getMessage();
			List<FormattedCharSequence> lines = this.font.split(message, textWidth);
			if(lines.size() == 1)
			{
				this.font.draw(pose, lines.get(0), textXPos, yPos + (HEIGHT_PER_ROW / 2) - (this.font.lineHeight / 2), textColor);
			}
			else
			{
				for(int l = 0; l < lines.size() && l < 2; ++l)
					this.font.draw(pose, lines.get(l), textXPos, yPos + 2 + l * 10, textColor);
				if(this.tooltip == null && mouseX >= this.getX() && mouseX < this.getX() + this.width && mouseY >= yPos && mouseY < yPos + HEIGHT_PER_ROW)
				{
					if(lines.size() > 2)
					{
						if(n.hasTimeStamp())
							this.tooltip = Component.empty().append(n.getTimeStampMessage()).append(Component.literal("\n")).append(message);
						else
							this.tooltip = message;
					}
					else if(n.hasTimeStamp())
						this.tooltip = n.getTimeStampMessage();
				}
			}
			
		}
		
	}
	
	public void tryRenderTooltip(PoseStack pose, Screen screen, int mouseX, int mouseY)
	{
		if(this.tooltip != null)
		{
			screen.renderTooltip(pose, this.font.split(this.tooltip, this.width), mouseX, mouseY);
			this.tooltip = null;
		}
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
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrator) { }

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		if(scroll < 0)
			this.setScroll(this.scroll + 1);
		else if(scroll > 0)
			this.setScroll(this.scroll - 1);
		return true;
	}
	
	 public void playDownSound(@NotNull SoundManager manager) {}
}
