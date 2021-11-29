package io.github.lightman314.lightmanscurrency.client;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletScreen;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.common.EventHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.containers.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageOpenWallet;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.MessageSlotShiftClick;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.MessageWalletSlotClick;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseClickedEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {

	
	public static final KeyBinding KEY_WALLET = new KeyBinding("key.wallet", GLFW.GLFW_KEY_V, "key.categories.inventory");
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.currentScreen instanceof WalletScreen)
		{
			if(event.getAction() == GLFW.GLFW_PRESS && event.getKey() == KEY_WALLET.getKey().getKeyCode())
			{
				minecraft.player.closeScreen();
			}
		}
		else if(minecraft.player != null && minecraft.currentScreen == null)
		{
			ClientPlayerEntity player = minecraft.player;
			if(KEY_WALLET.isPressed())
			{
				if(!LightmansCurrency.getWalletStack(player).isEmpty())
				{
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWallet());
					
					minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1.25f + player.world.rand.nextFloat() * 0.5f, 0.75f));
					
					ItemStack wallet = LightmansCurrency.getWalletStack(player);
					if(!WalletItem.isEmpty(wallet))
						minecraft.getSoundHandler().play(SimpleSound.master(CurrencySoundEvents.COINS_CLINKING, 1f, 0.4f));
				}
			}
		}
	}
	
	//Render the wallet slot for the creative and inventory screens
	@SuppressWarnings("resource")
	@SubscribeEvent
    public void onPlayerRenderScreen(GuiContainerEvent.DrawBackground event)
    {
    	if(LightmansCurrency.isCuriosLoaded())
    		return;
    	
    	ContainerScreen<?> screen = event.getGuiContainer();
    	
        if(screen instanceof InventoryScreen)
        {
        	InventoryScreen inventoryScreen = (InventoryScreen) screen;
            imitateWalletSlot(screen, event.getMatrixStack(), Minecraft.getInstance().player, inventoryScreen.getGuiLeft(), inventoryScreen.getGuiTop(), 152, 62, event.getMouseX(), event.getMouseY());
        }
        else if(screen instanceof CreativeScreen)
        {
            CreativeScreen creativeScreen = (CreativeScreen) screen;
            if(creativeScreen.getSelectedTabIndex() == ItemGroup.INVENTORY.getIndex())
                imitateWalletSlot(screen, event.getMatrixStack(), Minecraft.getInstance().player, creativeScreen.getGuiLeft(), creativeScreen.getGuiTop(), 153, 33, event.getMouseX(), event.getMouseY());
        }
    }
	
	@SubscribeEvent
	public void onPlayerClick(MouseClickedEvent.Pre event)
	{
		if(LightmansCurrency.isCuriosLoaded())
			return;
		if(event.isCanceled())
			return;
		
		boolean cancel = false;
		Screen screen = event.getGui();
		
		if(screen instanceof InventoryScreen)
		{
			InventoryScreen inventoryScreen = (InventoryScreen) screen;
			cancel = processScreenClick(inventoryScreen.getContainer(), inventoryScreen.getGuiLeft(), inventoryScreen.getGuiTop(), 152, 62, (int)event.getMouseX(), (int)event.getMouseY(), event.getButton());
		}
		else if(screen instanceof CreativeScreen)
		{
			CreativeScreen creativeScreen = (CreativeScreen) screen;
            if(creativeScreen.getSelectedTabIndex() == ItemGroup.INVENTORY.getIndex())
            	cancel = processScreenClick(creativeScreen.getContainer(), creativeScreen.getGuiLeft(), creativeScreen.getGuiTop(), 153, 33, (int)event.getMouseX(), (int)event.getMouseY(), event.getButton());
		}
		if(cancel)
			event.setCanceled(true);
	}
	
	private static void imitateWalletSlot(Screen screen, MatrixStack matrixStack, LivingEntity entity, int guiLeft, int guiTop, int x, int y, int mouseX, int mouseY)
	{
		WalletCapability.getWalletHandler(entity).ifPresent(walletHandler ->{
			
			Minecraft minecraft = screen.getMinecraft();
			FontRenderer font = minecraft.fontRenderer;
			minecraft.getTextureManager().bindTexture(ContainerScreen.INVENTORY_BACKGROUND);
			//Render the slot background
			Screen.blit(matrixStack, guiLeft + x - 1, guiTop + y - 1, 7, 7, 18, 18, 256, 256);
			
			ItemStack walletItem = walletHandler.getWallet();
			if(walletItem.isEmpty())
			{
				//Render the wallet slot bg
				ItemRenderUtil.drawSlotBackground(matrixStack, guiLeft + x, guiTop + y, Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, WalletSlot.EMPTY_WALLET_SLOT));
			}
			else
			{
				//Render the wallet stack
				ItemRenderUtil.drawItemStack(screen, font, walletItem, guiLeft + x, guiTop + y, true);
			}
			
			//Render the white overlay for when the mouse is hovered over the slot
			if (isMouseOverSlot(guiLeft + x, guiTop + y, mouseX, mouseY)) {
	            RenderSystem.disableDepthTest();
	            int j1 = guiLeft + x;
	            int k1 = guiTop + y;
	            RenderSystem.colorMask(true, true, true, false);
	            int slotColor = -2130706433;
	            fillGradient(matrixStack, j1, k1, j1 + 16, k1 + 16, slotColor, slotColor);
	            RenderSystem.colorMask(true, true, true, true);
	            RenderSystem.enableDepthTest();
	            
	            //Render the wallet tooltip
	            //Only render if the held item is empty
	            if(!walletItem.isEmpty() && minecraft.player.inventory.getItemStack().isEmpty())
	            {
	            	List<ITextComponent> tooltip = screen.getTooltipFromItem(walletItem);
	            	screen.renderWrappedToolTip(matrixStack, tooltip, mouseX, mouseY, font);
	            }
	            
			}
		});
	}
	
	private static boolean isMouseOverSlot(int x, int y, int mouseX, int mouseY)
	{
		int minX = x - 1;
		int maxX = x + 17;
		int minY = y - 1;
		int maxY = y + 17;
		return mouseX >= minX && mouseX < maxX && mouseY >= minY && mouseY < maxY;
	}
	
	@SuppressWarnings("deprecation")
	private static void fillGradient(MatrixStack matrixStack, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
	      RenderSystem.disableTexture();
	      RenderSystem.enableBlend();
	      RenderSystem.disableAlphaTest();
	      RenderSystem.defaultBlendFunc();
	      RenderSystem.shadeModel(7425);
	      Tessellator tessellator = Tessellator.getInstance();
	      BufferBuilder bufferbuilder = tessellator.getBuffer();
	      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
	      fillGradient2(matrixStack.getLast().getMatrix(), bufferbuilder, x1, y1, x2, y2, 100, colorFrom, colorTo);
	      tessellator.draw();
	      RenderSystem.shadeModel(7424);
	      RenderSystem.disableBlend();
	      RenderSystem.enableAlphaTest();
	      RenderSystem.enableTexture();
	}
	
	private static void fillGradient2(Matrix4f matrix, BufferBuilder builder, int x1, int y1, int x2, int y2, int z, int colorA, int colorB) {
	      float f = (float)(colorA >> 24 & 255) / 255.0F;
	      float f1 = (float)(colorA >> 16 & 255) / 255.0F;
	      float f2 = (float)(colorA >> 8 & 255) / 255.0F;
	      float f3 = (float)(colorA & 255) / 255.0F;
	      float f4 = (float)(colorB >> 24 & 255) / 255.0F;
	      float f5 = (float)(colorB >> 16 & 255) / 255.0F;
	      float f6 = (float)(colorB >> 8 & 255) / 255.0F;
	      float f7 = (float)(colorB & 255) / 255.0F;
	      builder.pos(matrix, (float)x2, (float)y1, (float)z).color(f1, f2, f3, f).endVertex();
	      builder.pos(matrix, (float)x1, (float)y1, (float)z).color(f1, f2, f3, f).endVertex();
	      builder.pos(matrix, (float)x1, (float)y2, (float)z).color(f5, f6, f7, f4).endVertex();
	      builder.pos(matrix, (float)x2, (float)y2, (float)z).color(f5, f6, f7, f4).endVertex();
	}
	
	private static boolean processScreenClick(Container container, int guiLeft, int guiTop, int x, int y, int mouseX, int mouseY, int button)
	{
		boolean hasShiftDown = Screen.hasShiftDown();
		Minecraft minecraft = Minecraft.getInstance();
		if(isMouseOverSlot(guiLeft + x, guiTop + y, mouseX, mouseY))
		{
			//Move the wallet out of the wallet slot, or place the wallet back in the wallet slot
			EventHandler.onWalletSlotClick(minecraft.player, hasShiftDown);
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageWalletSlotClick(hasShiftDown));
			return true;
		}
		else if(hasShiftDown)
		{
			//If the mouse is over another slot, and if so run the onSlotShiftClick function
			for(int i = 0; i < container.inventorySlots.size(); i++)
			{
				Slot slot = container.inventorySlots.get(i);
				//Only work on player inventory slots
				if(isMouseOverSlot(guiLeft + slot.xPos, guiTop + slot.yPos, mouseX, mouseY) && slot.inventory == minecraft.player.inventory)
				{
					//Extract player inventory index from the slot
					int inventoryIndex = slot.getSlotIndex();
					boolean result = EventHandler.onSlotShiftClick(minecraft.player, inventoryIndex);
					if(result)
						LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSlotShiftClick(inventoryIndex));
					return result;
				}
			}
		}
		return false;
		
	}
	
}
