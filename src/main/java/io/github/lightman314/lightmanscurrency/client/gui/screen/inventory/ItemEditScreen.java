package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ItemEditScreen extends ContainerScreen<ItemEditContainer>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/item_edit.png");
	
	public static final int SCREEN_EXTENSION = ItemTraderStorageUtil.SCREEN_EXTENSION;
	
	private TextFieldWidget searchField;
	
	Button buttonToggleSlot;
	
	Button buttonPageLeft;
	Button buttonPageRight;
	
	Button buttonCountUp;
	Button buttonCountDown;
	
	Button buttonChangeName;
	int setSlot = 0;
	
	boolean firstTick = false;
	
	List<Button> tradePriceButtons = new ArrayList<>();
	
	public ItemEditScreen(ItemEditContainer container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		this.xSize = 176;
		this.ySize = 156;
		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		
		if(this.container.getTrader() == null)
			return;
		
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (this.width - xSize) / 2;
		int startY = (this.height - ySize) / 2;
		
		//Render the BG
		this.blit(matrix, startX, startY, 0, 0, this.xSize, this.ySize);
		
		//Render the fake trade button
		ItemTradeButton.renderItemTradeButton(matrix, (Screen)this, font, startX, startY - ItemTradeButton.HEIGHT, this.container.tradeIndex, this.container.getTrader(), false);
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		
		this.font.drawString(matrix, new TranslationTextComponent("gui.lightmanscurrency.item_edit.title").getString(), 8.0f, 6.0f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();

		//Initialize the search field
		this.searchField = new TextFieldWidget(this.font, guiLeft + 81, guiTop + 6, 79, 9, new TranslationTextComponent("gui.lightmanscurrency.item_edit.search"));
		this.searchField.setEnableBackgroundDrawing(false);
		this.searchField.setMaxStringLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		this.children.add(this.searchField);
		
		//Initialize the buttons
		//Toggle button
		this.buttonToggleSlot = this.addButton(new Button(this.guiLeft + this.xSize - 80, this.guiTop - 20, 80, 20, new TranslationTextComponent("gui.button.lightmanscurrency.item_edit.toggle.sell"), this::PressToggleSlotButton));
		this.buttonToggleSlot.visible = this.container.getTrade().isBarter();
		
		//Page Buttons
		this.buttonPageLeft = this.addButton(IconAndButtonUtil.leftButton(this.guiLeft - 20, this.guiTop, this::PressPageButton));
		this.buttonPageRight = this.addButton(IconAndButtonUtil.rightButton(this.guiLeft + this.xSize, this.guiTop, this::PressPageButton));
		//Count Buttons
		this.buttonCountUp = this.addButton(new PlainButton(this.guiLeft + this.xSize, this.guiTop + 20, 10, 10, this::PressStackCountButton, GUI_TEXTURE, this.xSize + 32, 0));
		this.buttonCountDown = this.addButton(new PlainButton(this.guiLeft + this.xSize, this.guiTop + 30, 10, 10, this::PressStackCountButton, GUI_TEXTURE, this.xSize + 32, 20));
		
		//Close Button
		this.addButton(new Button(this.guiLeft + 7, this.guiTop + 129, 162, 20, new TranslationTextComponent("gui.button.lightmanscurrency.back"), this::PressCloseButton));
		
		
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		if(this.container.getTrader() == null)
		{
			this.minecraft.displayGuiScreen(null);
			return;
		}
		
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX,  mouseY);
		
		this.searchField.render(matrixStack, mouseX, mouseY, partialTicks);
		
		ItemTradeButton.tryRenderTooltip(matrixStack, this, this.container.tradeIndex, this.container.getTrader(), this.guiLeft, this.guiTop - ItemTradeButton.HEIGHT, false, mouseX, mouseY);
		
	}
	
	@Override
	public void tick()
	{
		
		if(this.container.getTrader() == null)
		{
			this.minecraft.displayGuiScreen(null);
			return;
		}
		
		this.searchField.tick();
		
		this.buttonToggleSlot.setMessage(new TranslationTextComponent(this.container.getEditSlot() == 1 ? "gui.button.lightmanscurrency.item_edit.toggle.barter" : "gui.button.lightmanscurrency.item_edit.toggle.sell"));
		
		this.buttonPageLeft.active = this.container.getPage() > 0;
		this.buttonPageRight.active = this.container.getPage() < this.container.maxPage();
		
		this.buttonCountUp.active = this.container.getStackCount() < 64;
		this.buttonCountDown.active = this.container.getStackCount() > 1;
		
		if(!firstTick)
		{
			firstTick = true;
			this.container.refreshPage();
		}
		
	}
	
	@Override
	public boolean charTyped(char c, int code)
	{
		String s = this.searchField.getText();
		if(this.searchField.charTyped(c, code))
		{
			if(!Objects.equals(s, this.searchField.getText()))
			{
				container.modifySearch(this.searchField.getText());
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean keyPressed(int key, int scanCode, int mods)
	{
		String s = this.searchField.getText();
		if(this.searchField.keyPressed(key, scanCode, mods))
		{
			if(!Objects.equals(s,  this.searchField.getText()))
			{
				container.modifySearch(this.searchField.getText());
			}
			return true;
		}
		return this.searchField.isFocused() && this.searchField.getVisible() && key != GLFW_KEY_ESCAPE || super.keyPressed(key, scanCode, mods);
	}
	
	private void PressToggleSlotButton(Button button)
	{
		this.container.toggleEditSlot();
	}
	
	private void PressPageButton(Button button)
	{
		int direction = 1;
		if(button == this.buttonPageLeft)
			direction = -1;
		
		container.modifyPage(direction);
		
	}
	
	private void PressStackCountButton(Button button)
	{
		int deltaCount = 1;
		if(button == this.buttonCountDown)
			deltaCount = -1;
		
		if(Screen.hasShiftDown())
			deltaCount *= 16;
		
		container.modifyStackSize(deltaCount);
		
	}
	
	private void PressCloseButton(Button button)
	{
		this.container.openTraderStorage();
	}
	
	
}
