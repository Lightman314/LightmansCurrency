package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class ItemEditScreen extends AbstractContainerScreen<ItemEditContainer>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/item_edit.png");
	
	public static final int SCREEN_EXTENSION = ItemTraderStorageUtil.SCREEN_EXTENSION;
	
	private EditBox searchField;
	
	Button buttonPageLeft;
	Button buttonPageRight;
	
	Button buttonCountUp;
	Button buttonCountDown;
	
	Button buttonChangeName;
	
	boolean firstTick = false;
	
	List<Button> tradePriceButtons = new ArrayList<>();
	
	public ItemEditScreen(ItemEditContainer container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = 156;
		
	}
	
	@Override
	protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		
		//RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		//minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		
		int startX = (this.width - imageWidth) / 2;
		int startY = (this.height - imageHeight) / 2;
		
		//Render the BG
		this.blit(matrix, startX, startY, 0, 0, this.imageWidth, this.imageHeight);
		
		//Render the trade button
		//minecraft.getTextureManager().bindTexture(ItemTradeButton.TRADE_TEXTURES);
		RenderSystem.setShaderTexture(0, ItemTradeButton.TRADE_TEXTURES);
		int yOffset = ItemTradeButton.getRenderYOffset(menu.tradeData.getTradeDirection());
		this.blit(matrix, startX, startY - ItemTradeButton.HEIGHT, 0, yOffset, ItemTradeButton.WIDTH, ItemTradeButton.HEIGHT);
		
	}
	
	@Override
	protected void renderLabels(PoseStack matrix, int mouseX, int mouseY)
	{
		
		this.font.draw(matrix, new TranslatableComponent("gui.lightmanscurrency.item_edit.title").getString(), 8.0f, 6.0f, 0x404040);
		
		//Draw the trade price text
		this.font.draw(matrix, ItemTradeButton.getTradeText(menu.tradeData, true, true), ItemTradeButton.TEXTPOS_X, ItemTradeButton.TEXTPOS_Y - ItemTradeButton.HEIGHT, ItemTradeButton.getTradeTextColor(menu.tradeData, true, true));
		
	}
	
	@Override
	protected void init()
	{
		super.init();

		//Initialize the search field
		this.searchField = this.addRenderableWidget(new EditBox(this.font, leftPos + 81, topPos + 6, 79, 9, new TranslatableComponent("gui.lightmanscurrency.item_edit.search")));
		//this.searchField.setEnableBackgroundDrawing(false);
		this.searchField.setBordered(false);
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		
		//Initialize the buttons
		//Page Buttons
		this.buttonPageLeft = this.addRenderableWidget(new IconButton(this.leftPos - 20, this.topPos, this::PressPageButton, GUI_TEXTURE, this.imageWidth, 0));
		this.buttonPageRight = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth, this.topPos, this::PressPageButton, GUI_TEXTURE, this.imageWidth + 16, 0));
		//Count Buttons
		this.buttonCountUp = this.addRenderableWidget(new PlainButton(this.leftPos + this.imageWidth, this.topPos + 20, 10, 10, this::PressStackCountButton, GUI_TEXTURE, this.imageWidth + 32, 0));
		this.buttonCountDown = this.addRenderableWidget(new PlainButton(this.leftPos + this.imageWidth, this.topPos + 30, 10, 10, this::PressStackCountButton, GUI_TEXTURE, this.imageWidth + 32, 20));
		
		//Close Button
		this.addRenderableWidget(new Button(this.leftPos + 7, this.topPos + 129, 162, 20, new TranslatableComponent("gui.button.lightmanscurrency.back"), this::PressCloseButton));
		
		
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX,  mouseY);
		
		//this.searchField.render(matrixStack, mouseX, mouseY, partialTicks);
		
	}
	
	@Override
	public void containerTick()
	{
		
		this.searchField.tick();
		
		this.buttonPageLeft.active = this.menu.getPage() > 0;
		this.buttonPageRight.active = this.menu.getPage() < this.menu.maxPage();
		
		this.buttonCountUp.active = this.menu.getStackCount() < 64;
		this.buttonCountDown.active = this.menu.getStackCount() > 1;
		
		if(!firstTick)
		{
			firstTick = true;
			this.menu.refreshPage();
		}
		
	}
	
	@Override
	public boolean charTyped(char c, int code)
	{
		String s = this.searchField.getValue();
		if(this.searchField.charTyped(c, code))
		{
			if(!Objects.equals(s, this.searchField.getValue()))
			{
				menu.modifySearch(this.searchField.getValue());
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean keyPressed(int key, int scanCode, int mods)
	{
		String s = this.searchField.getValue();
		if(this.searchField.keyPressed(key, scanCode, mods))
		{
			if(!Objects.equals(s,  this.searchField.getValue()))
			{
				menu.modifySearch(this.searchField.getValue());
			}
			return true;
		}
		return this.searchField.isFocused() && this.searchField.visible && key != GLFW_KEY_ESCAPE || super.keyPressed(key, scanCode, mods);
	}
	
	private void PressPageButton(Button button)
	{
		int direction = 1;
		if(button == this.buttonPageLeft)
			direction = -1;
		
		menu.modifyPage(direction);
		
	}
	
	private void PressStackCountButton(Button button)
	{
		int direction = 1;
		if(button == this.buttonCountDown)
			direction = -1;
		
		menu.modifyStackSize(direction);
		
	}
	
	private void PressCloseButton(Button button)
	{
		menu.openTraderStorage();
	}
	
	
}
