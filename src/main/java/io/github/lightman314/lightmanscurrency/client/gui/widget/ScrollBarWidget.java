package io.github.lightman314.lightmanscurrency.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ScrollBarWidget extends AbstractWidget implements IMouseListener {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/scroll.png");
	
	public static final int WIDTH = 8;
	public static final int KNOB_HEIGHT = 29;
	public static final int SMALL_KNOB_HEIGHT = 9;
	
	private final IScrollable scrollable;
	
	public boolean smallKnob = false;
	
	public boolean isDragging = false;
	
	private int getKnobHeight() { return this.smallKnob ? SMALL_KNOB_HEIGHT : KNOB_HEIGHT; }
	
	public ScrollBarWidget(int x, int y, int height, IScrollable scrollable) {
		super(x, y, WIDTH, height, Component.empty());
		this.scrollable = scrollable;
	}

	public boolean visible() { return this.visible && this.scrollable.getMaxScroll() > this.scrollable.getMinScroll(); }
	
	@Override
	public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		if(!this.visible() && this.isDragging)
			this.isDragging = false;
		super.render(pose, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void renderButton(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(!this.visible())
			return;
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		//Render the top
		this.blit(pose, this.x, this.y, 0, 0, WIDTH, 8);
		//Render the middle
		int yOffset = 8;
		while(yOffset < this.height - 8)
		{
			int yPart = Math.min(this.height - 8 - yOffset, 240);
			this.blit(pose, this.x, this.y + yOffset, 0, 8, WIDTH, yPart);
			yOffset += yPart;
		}
		//Render the bottom
		this.blit(pose, this.x, this.y + this.height - 8, 0, 248, WIDTH, 8);
		
		int knobPosition;
		if(this.isDragging)
			knobPosition = MathUtil.clamp(mouseY - this.y - (this.getKnobHeight() / 2), 0, this.height - this.getKnobHeight());
		else
			knobPosition = this.getNaturalKnobPosition();
		
		//Render the knob
		this.blit(pose, this.x, this.y + knobPosition, this.smallKnob ? WIDTH * 2 : WIDTH, 0, WIDTH, this.getKnobHeight());
		
	}
	
	public void beforeWidgetRender(double mouseY) {
		if(this.isDragging)
		{
			this.dragKnob(mouseY);
		}
	}
	
	private int getNaturalKnobPosition() {
		int notches = this.scrollable.getMaxScroll() - this.scrollable.getMinScroll();
		if(notches <= 0)
			return 0;
		double spacing = (double)(this.height - this.getKnobHeight()) / (double)notches;
		int scroll = this.scrollable.currentScroll() - this.scrollable.getMinScroll();
		return (int)Math.round(scroll * spacing);
	}
	
	public interface IScrollable {
		int currentScroll();
		void setScroll(int newScroll);
		default int getMinScroll() { return 0; }
		int getMaxScroll();
		default boolean handleScrollWheel(double delta) {
			int scroll = this.currentScroll();
			if(delta < 0)
			{			
				if(scroll < this.getMaxScroll())
				{
					this.setScroll(scroll + 1);
					return true;
				}
			}
			else if(delta > 0)
			{
				if(scroll > 0)
				{
					this.setScroll(scroll - 1);
					return true;
				}
			}
			return false;
		}
		static int calculateMaxScroll(int visibleCount, int totalCount) { return Math.max(0, totalCount - visibleCount); }
	}

	@Override
	public void updateNarration(@NotNull NarrationElementOutput narrator) { }

	protected void dragKnob(double mouseY) {
		//Cannot do anything if the scrollable cannot be scrolled
		if(!this.visible())
		{
			this.isDragging = false;
			return;
		}
		
		//Calculate the y offset
		int scroll = this.getScrollFromMouse(mouseY);
		
		if(this.scrollable.currentScroll() != scroll)
			this.scrollable.setScroll(scroll);
		
	}
	
	private int getScrollFromMouse(double mouseY) {
		
		mouseY -= (double)this.getKnobHeight() / 2d;
		//Check if the mouse is out of bounds, upon which return the max/min scroll respectively
		if(mouseY <= this.y)
			return this.scrollable.getMinScroll();
		if(mouseY >= this.y + this.height - this.getKnobHeight())
			return this.scrollable.getMaxScroll();
		
		//Calculate the scroll based on the mouse position
		int deltaScroll = this.scrollable.getMaxScroll() - this.scrollable.getMinScroll();
		if(deltaScroll <= 0)
			return Integer.MIN_VALUE;
		
		double sectionHeight = (double)(this.height - this.getKnobHeight()) / (double)deltaScroll;
		double yPos = (double)this.y - (sectionHeight / 2d);
		
		for(int i = this.scrollable.getMinScroll(); i <= this.scrollable.getMaxScroll(); ++i)
		{
			if(mouseY >= yPos && mouseY < yPos + sectionHeight)
				return i;
			yPos += sectionHeight;
		}
		//Somehow didn't find the scroll from the scroll bar.
		LightmansCurrency.LogWarning("Error getting scroll from mouse position.");
		return this.scrollable.getMinScroll();
	}

	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		this.isDragging = false;
		if(this.isMouseOver(mouseX, mouseY) && this.visible() && button == 0)
		{
			LightmansCurrency.LogInfo("Started dragging.");
			this.isDragging = true;
			this.dragKnob(mouseY);
		}
		return false;
	}

	@Override
	public boolean onMouseReleased(double mouseX, double mouseY, int button) {
		if(this.isDragging && this.visible() && button == 0)
		{
			//One last drag calculation
			this.dragKnob(mouseY);
			this.isDragging = false;
			LightmansCurrency.LogInfo("Stopped dragging.");
		}
		return false;
	}
	
	public void playDownSound(@NotNull SoundManager soundManager) { }
	
}
