package io.github.lightman314.lightmanscurrency.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class ScrollBarWidget extends AbstractWidget {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/scroll.png");
	
	public static final int WIDTH = 8;
	public static final int KNOB_HEIGHT = 29;
	public static final int SMALL_KNOB_HEIGHT = 9;
	
	private final IScrollable scrollable;
	
	public boolean smallKnob = false;
	
	public boolean isDragging = false;
	
	private int getKnobHeight() { return this.smallKnob ? SMALL_KNOB_HEIGHT : KNOB_HEIGHT; }
	
	public ScrollBarWidget(int x, int y, int height, IScrollable scrollable) {
		super(x, y, WIDTH, height, new TextComponent(""));
		this.scrollable = scrollable;
	}

	public boolean visible() { return this.visible && this.scrollable.getMaxScroll() > this.scrollable.getMinScroll(); }
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		if(!this.visible() && this.isDragging)
			this.isDragging = false;
		super.render(pose, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
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
		//if(this.isDragging)
		//	knobPosition = this.getKnobAndScrollFromMouse(mouseY).getSecond();
		//else
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
	
	/*private Pair<Integer,Integer> getKnobAndScrollFromMouse(double mouseY) {
		//Offset the mouse to emulate it being at the bottom of the knob.
		mouseY -= ((double)this.getKnobHeight() / 2d) + this.y;
		if(mouseY < 0)
			return Pair.of(this.scrollable.getMinScroll(), 0);
		else if(mouseY >= this.height - this.getKnobHeight())
			return Pair.of(this.scrollable.getMaxScroll(), this.height - this.getKnobHeight());
		int notches = this.scrollable.getMaxScroll() - this.scrollable.getMinScroll() + 1;
		if(notches <= 1)
			return Pair.of(this.scrollable.getMinScroll(), 0);
		int scroll = this.scrollable.getMinScroll();
		double spacing = (double)(this.height - this.getKnobHeight()) / (double)notches;
		double yOffset = this.y;
		while(yOffset <= this.y + this.height - this.getKnobHeight())
		{
			if(mouseY >= yOffset && mouseY < yOffset + spacing)
			{
				return Pair.of(scroll, (int)mouseY);
			}
			yOffset += spacing;
			scroll++;
		}
		return Pair.of(this.scrollable.getMaxScroll(), this.height - this.getKnobHeight());
	}*/
	
	public interface IScrollable {
		public int currentScroll();
		public void setScroll(int newScroll);
		public default int getMinScroll() { return 0; }
		public int getMaxScroll();
	}

	@Override
	public void updateNarration(NarrationElementOutput narrator) { }
	
	protected void dragKnob(double mouseY) {
		//Cannot do anything if the scrollable cannot be scrolled
		/*if(!this.visible())
		{
			this.isDragging = false;
			return;
		}
		
		Pair<Integer,Integer> positions = this.getKnobAndScrollFromMouse(mouseY);
		
		if(this.scrollable.currentScroll() != positions.getFirst())
			this.scrollable.setScroll(positions.getFirst());
		*/
	}
	
	/**
	 * Not required for new scroll bar drag system.
	 */
	@Deprecated
	public void onMouseDragged(double mouseX, double mouseY, int button) { }
	
	public void onMouseClicked(double mouseX, double mouseY, int button) {
		/*this.isDragging = false;
		if(this.isMouseOver(mouseX, mouseY) && this.visible() && button == 0)
		{
			LightmansCurrency.LogInfo("Started dragging.");
			this.isDragging = true;
			this.dragKnob(mouseY);
		}*/
	}
	
	public void onMouseReleased(double mouseX, double mouseY, int button) {
		/*if(this.isDragging && this.visible() && button == 0)
		{
			//One last drag calculation
			this.dragKnob(mouseY);
			this.isDragging = false;
			LightmansCurrency.LogInfo("Stopped dragging.");
		}*/
	}

}
