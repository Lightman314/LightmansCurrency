package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.containers.PlayerInventoryWalletContainer;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.CPacketOpenVanilla;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.WalletButton;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class PlayerInventoryWalletScreen extends DisplayEffectsScreen<PlayerInventoryWalletContainer>{

	float oldMouseX;
	float oldMouseY;
	
	public PlayerInventoryWalletScreen(PlayerInventoryWalletContainer container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		//this.titleX = 97;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
		this.blit(matrix, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		//Hide the crafting table
		this.blit(matrix, this.guiLeft + 97, this.guiTop + 17, 97, 53, 74, 30);
		this.blit(matrix, this.guiLeft + 97, this.guiTop + 47, 97, 53, 74, 6);
		//Render the wallet slot
		this.blit(matrix, this.guiLeft + PlayerInventoryWalletContainer.WALLET_SLOT_X - 1, this.guiTop + PlayerInventoryWalletContainer.WALLET_SLOT_Y - 1, 7, 7, 18, 18);
		InventoryScreen.drawEntityOnScreen(this.guiLeft + 51, this.guiTop + 75, 30, (float)(this.guiLeft + 51) - this.oldMouseX, (float)(this.guiTop + 75 - 50) - this.oldMouseY, this.minecraft.player);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		//this.font.func_243248_b(matrix, this.title, (float)this.titleX, (float)this.titleY, 4210752);
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		this.addButton(new WalletButton(this, 26, 8, this::PressInventoryButton));
		
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
		
		this.oldMouseX = (float)mouseX;
		this.oldMouseY = (float)mouseY;
	}
	
	@Override
	public void tick()
	{
		super.tick();
	}
	
	private void PressInventoryButton(Button button)
	{
		InventoryScreen inventory = new InventoryScreen(this.minecraft.player);
		ItemStack stack = this.minecraft.player.inventory.getItemStack();
		this.minecraft.player.inventory.setItemStack(ItemStack.EMPTY);
		this.minecraft.displayGuiScreen(inventory);
		this.minecraft.player.inventory.setItemStack(stack);
		LightmansCurrencyPacketHandler.instance.sendToServer(new CPacketOpenVanilla());
	}
	
}
