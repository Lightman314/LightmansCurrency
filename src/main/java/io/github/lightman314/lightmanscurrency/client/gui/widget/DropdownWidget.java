package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.DropdownButton;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class DropdownWidget extends AbstractWidget {
	
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/dropdown.png");
	
	public static final int HEIGHT = 12;
	
	boolean open = false;
	
	int currentlySelected;
	
	private final Font font;
	private final List<Component> options;
	private final Consumer<Integer> onSelect;
	private final Function<Integer,Boolean> optionActive;
	
	List<Button> optionButtons = new ArrayList<>();
	
	public DropdownWidget(int x, int y, int width, Font font, int selected, Consumer<Integer> onSelect, Function<Button,Button> addButton, Component... options) {
		this(x, y, width, font, selected, onSelect, (index) -> true, addButton, options);
	}
	
	public DropdownWidget(int x, int y, int width, Font font, int selected, Consumer<Integer> onSelect, Function<Button,Button> addButton, List<Component> options) {
		this(x, y, width, font, selected, onSelect, (index) -> true, addButton, options);
	}
	
	public DropdownWidget(int x, int y, int width, Font font, int selected, Consumer<Integer> onSelect, Function<Integer,Boolean> optionActive, Function<Button,Button> addButton, Component... options) {
		this(x, y, width, font, selected, onSelect, optionActive, addButton, Lists.newArrayList(options));
	}
	
	public DropdownWidget(int x, int y, int width, Font font, int selected, Consumer<Integer> onSelect, Function<Integer,Boolean> optionActive, Function<Button,Button> addButton, List<Component> options) {
		super(x, y, width, HEIGHT, new TextComponent(""));
		this.font = font;
		this.options = options;
		this.currentlySelected = MathUtil.clamp(selected, 0, this.options.size() - 1);
		this.onSelect = onSelect;
		this.optionActive = optionActive;
		//Init the buttons before this, so that they get pressed before this closes them on a offset click
		this.init(addButton);
	}
	
	private void init(Function<Button,Button> addButton) {
		this.optionButtons = new ArrayList<>();
		
		for(int i = 0; i < this.options.size(); ++i)
		{
			int yPos = this.y + HEIGHT + (i * HEIGHT);
			this.optionButtons.add(addButton.apply(new DropdownButton(this.x, yPos, this.width, this.font, this.options.get(i), this::OnSelect)));
			this.optionButtons.get(i).visible = this.open;
		}
		
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		//Draw the background
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int offset = this.isHovered ? this.height : 0;
        if(!this.active)
        	RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        this.blit(pose, this.x, this.y, 0, offset, 2, DropdownWidget.HEIGHT);
        int xOffset = 0;
        while(xOffset < this.width - 14)
        {
        	int xPart = Math.min(this.width - 14 - xOffset, 244);
        	this.blit(pose, this.x + 2 + xOffset, this.y, 2, offset, xPart, DropdownWidget.HEIGHT);
        	xOffset += xPart;
        }
        this.blit(pose, this.x + this.width - 12, this.y, 244, offset, 12, DropdownWidget.HEIGHT);
		
        //Draw the option text
        this.font.draw(pose, this.fitString(this.options.get(this.currentlySelected).getString()), this.x + 2, this.y + 2, 0x404040);
        
		//Confirm the option buttons active state
		if(this.open)
		{
			for(int i = 0; i < this.optionButtons.size(); ++i)
				this.optionButtons.get(i).active = this.optionActive.apply(i) && i != this.currentlySelected;
		}
		
		
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int click) {
		if (this.active && this.visible) {
            if (this.clicked(mouseX, mouseY) && this.isValidClickButton(click)) {
            	this.playDownSound(Minecraft.getInstance().getSoundManager());
            	this.open = !this.open;
            	this.optionButtons.forEach(button -> button.visible = this.open);
            	return true;
            }
            else if(this.open)
            {
            	this.open = false;
            	this.optionButtons.forEach(button -> button.visible = false);
            }
			return false;
		} else {
			return false;
		}
	}
	
	private void OnSelect(Button button) {
		int index = this.optionButtons.indexOf(button);
		if(index < 0)
			return;
		this.currentlySelected = index;
		this.onSelect.accept(index);
		this.open = false;
		this.optionButtons.forEach(b -> b.visible = false);
	}

	@Override
	public void updateNarration(NarrationElementOutput narrator) { }
	
	private String fitString(String text) {
		if(this.font.width(text) <= this.width - 14)
			return text;
		while(this.font.width(text + "...") > this.width - 14 && text.length() > 0)
		{
			text = text.substring(0, text.length() - 1);
		}
		return text + "...";
	}
	
	
}
