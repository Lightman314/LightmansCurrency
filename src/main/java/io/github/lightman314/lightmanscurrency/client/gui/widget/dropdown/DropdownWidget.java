package io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class DropdownWidget extends EasyWidgetWithChildren implements IMouseListener {
	
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/dropdown.png");
	
	public static final int HEIGHT = 12;
	
	boolean open = false;
	
	int currentlySelected;

	private final List<Component> options;
	private final Consumer<Integer> onSelect;
	private final Function<Integer,Boolean> optionActive;
	
	List<EasyButton> optionButtons = new ArrayList<>();
	
	public DropdownWidget(ScreenPosition pos, int width, int selected, Consumer<Integer> onSelect, Component... options) {
		this(pos.x, pos.y, width, selected, onSelect, options);
	}
	public DropdownWidget(int x, int y, int width, int selected, Consumer<Integer> onSelect, Component... options) {
		this(x, y, width, selected, onSelect, (index) -> true, options);
	}
	
	public DropdownWidget(ScreenPosition pos, int width, int selected, Consumer<Integer> onSelect, List<Component> options) {
		this(pos.x, pos.y, width, selected, onSelect, options);
	}
	public DropdownWidget(int x, int y, int width, int selected, Consumer<Integer> onSelect, List<Component> options) {
		this(x, y, width, selected, onSelect, (index) -> true, options);
	}
	
	public DropdownWidget(ScreenPosition pos, int width, int selected, Consumer<Integer> onSelect, Function<Integer,Boolean> optionActive, Component... options) {
		this(pos.x, pos.y, width, selected, onSelect, optionActive, options);
	}
	public DropdownWidget(int x, int y, int width, int selected, Consumer<Integer> onSelect, Function<Integer,Boolean> optionActive, Component... options) {
		this(x, y, width, selected, onSelect, optionActive, Lists.newArrayList(options));
	}
	
	public DropdownWidget(ScreenPosition pos, int width, int selected, Consumer<Integer> onSelect, Function<Integer,Boolean> optionActive, List<Component> options) {
		this(pos.x, pos.y, width, selected, onSelect, optionActive, options);
	}
	public DropdownWidget(int x, int y, int width, int selected, Consumer<Integer> onSelect, Function<Integer,Boolean> optionActive, List<Component> options) {
		super(x, y, width, HEIGHT);
		this.options = options;
		this.currentlySelected = MathUtil.clamp(selected, 0, this.options.size() - 1);
		this.onSelect = onSelect;
		this.optionActive = optionActive;
	}

	@Override
	public DropdownWidget withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

	//Init the buttons before this, so that they get pressed before this closes them on an offset click
	@Override
	public boolean addChildrenBeforeThis() { return true; }

	@Override
	public void addChildren() {
		this.optionButtons = new ArrayList<>();
		for(int i = 0; i < this.options.size(); ++i)
		{
			int yPos = this.getY() + HEIGHT + (i * HEIGHT);
			DropdownButton button = this.addChild(new DropdownButton(this.getX(), yPos, this.width, this.options.get(i), this::OnSelect));
			this.optionButtons.add(button);
			this.optionButtons.get(i).visible = this.open;
		}
	}

	@Override
	public void renderTick() {
		//Confirm the option buttons active state
		if(this.open)
		{
			for(int i = 0; i < this.optionButtons.size(); ++i)
				this.optionButtons.get(i).active = this.optionActive.apply(i) && i != this.currentlySelected;
		}
	}
	
	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui) {
		
		//Draw the background
        int offset = this.isHovered ? this.height : 0;
        if(!this.active)
			gui.setColor(0.5f, 0.5f, 0.5f);
		else
			gui.resetColor();
		gui.blit(GUI_TEXTURE, 0, 0, 0, offset, 2, DropdownWidget.HEIGHT);
        int xOffset = 0;
        while(xOffset < this.width - 14)
        {
        	int xPart = Math.min(this.width - 14 - xOffset, 244);
			gui.blit(GUI_TEXTURE, 2 + xOffset, 0, 2, offset, xPart, DropdownWidget.HEIGHT);
        	xOffset += xPart;
        }
		gui.blit(GUI_TEXTURE, this.width - 12, 0, 244, offset, 12, DropdownWidget.HEIGHT);
		
        //Draw the option text
		gui.drawString(this.fitString(gui, this.options.get(this.currentlySelected).getString()), 2, 2, 0x404040);

		gui.resetColor();
		
	}
	
	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int click) {
		if (this.active && this.visible) {
            if (this.clicked(mouseX, mouseY) && this.isValidClickButton(click)) {
            	this.playDownSound(Minecraft.getInstance().getSoundManager());
            	this.open = !this.open;
            	this.optionButtons.forEach(button -> button.visible = this.open);
            	return true;
            }
            else if(this.open && !this.isOverChild(mouseX, mouseY))
            {
            	this.open = false;
            	this.optionButtons.forEach(button -> button.visible = false);
            }
		}
		return false;
	}

	private boolean isOverChild(double mouseX, double mouseY)
	{
		for(EasyButton b : this.optionButtons)
		{
			if(b.isMouseOver(mouseX, mouseY))
				return true;
		}
		return false;
	}

	private void OnSelect(EasyButton button) {
		int index = this.optionButtons.indexOf(button);
		if(index < 0)
			return;
		this.currentlySelected = index;
		this.onSelect.accept(index);
		this.open = false;
		this.optionButtons.forEach(b -> b.visible = false);
	}

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrator) { }

	private String fitString(EasyGuiGraphics gui, String text) {
		if(gui.font.width(text) <= this.width - 14)
			return text;
		while(gui.font.width(text + "...") > this.width - 14 && text.length() > 0)
			text = text.substring(0, text.length() - 1);
		return text + "...";
	}

	@Override
	protected boolean isValidClickButton(int button) { return button == 0; }

	@Override
	public void playDownSound(@Nonnull SoundManager manager) { EasyButton.playClick(manager); }
}
