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
import io.github.lightman314.lightmanscurrency.menus.ItemEditMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class ItemEditScreen extends AbstractContainerScreen<ItemEditMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/item_edit.png");
	
	public static final int SCREEN_EXTENSION = ItemTraderStorageUtil.SCREEN_EXTENSION;
	
	private EditBox searchField;
	
	Button buttonToggleSlot;
	
	Button buttonPageLeft;
	Button buttonPageRight;
	
	Button buttonCountUp;
	Button buttonCountDown;
	
	Button buttonChangeName;
	int setSlot = 0;
	
	boolean firstTick = false;
	
	List<Button> tradePriceButtons = new ArrayList<>();
	
	public ItemEditScreen(ItemEditMenu container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = 156;
		
	}
	
	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
	{
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		//Render the BG
		this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		//Render the fake trade button
		ItemTradeButton.renderItemTradeButton(poseStack, (Screen)this, font, this.leftPos, this.topPos - ItemTradeButton.HEIGHT, this.menu.tradeIndex, this.menu.traderSource.get(), null, false, true, false);
		
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		
		this.font.draw(poseStack, new TranslatableComponent("gui.lightmanscurrency.item_edit.title"), 8.0f, 6.0f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();

		//Initialize the search field
		this.searchField = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 81, this.topPos + 6, 79, 9, new TranslatableComponent("gui.lightmanscurrency.item_edit.search")));
		this.searchField.setBordered(false);
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		
		//Initialize the buttons
		//Toggle button
		this.buttonToggleSlot = this.addRenderableWidget(new Button(this.leftPos + this.imageWidth - 80, this.topPos - 20, 80, 20, new TranslatableComponent("gui.button.lightmanscurrency.item_edit.toggle.sell"), this::PressToggleSlotButton));
		this.buttonToggleSlot.visible = this.menu.tradeData.isBarter();
		
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
		
		ItemTradeButton.tryRenderTooltip(matrixStack, this, this.menu.tradeIndex, this.menu.traderSource.get(), this.leftPos, this.topPos - ItemTradeButton.HEIGHT, false, mouseX, mouseY, null);
		
	}
	
	@Override
	public void containerTick()
	{
		
		this.searchField.tick();
		
		this.buttonToggleSlot.setMessage(new TranslatableComponent(this.menu.getEditSlot() == 1 ? "gui.button.lightmanscurrency.item_edit.toggle.barter" : "gui.button.lightmanscurrency.item_edit.toggle.sell"));
		
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
		return this.searchField.isFocused() && this.searchField.isVisible() && key != GLFW_KEY_ESCAPE || super.keyPressed(key, scanCode, mods);
	}
	
	private void PressToggleSlotButton(Button button)
	{
		this.menu.toggleEditSlot();
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
		this.menu.openTraderStorage();
	}
	
	
}
