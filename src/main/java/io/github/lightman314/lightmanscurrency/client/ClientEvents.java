package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.client.gui.util.ScreenUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VisibilityToggleButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.NotificationButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.TeamManagerButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.TraderRecoveryButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.WalletButton;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageOpenWallet;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.CPacketSetVisible;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.CPacketWalletInteraction;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, value = Dist.CLIENT)
public class ClientEvents {

	public static final ResourceLocation WALLET_SLOT_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/wallet_slot.png");
	
	public static final KeyMapping KEY_WALLET = new KeyMapping("key.wallet", GLFW.GLFW_KEY_V, KeyMapping.CATEGORY_INVENTORY);
	public static final KeyMapping KEY_PORTABLE_TERMINAL = new KeyMapping("key.portable_terminal", GLFW.GLFW_KEY_BACKSLASH, KeyMapping.CATEGORY_INVENTORY);
	//public static final KeyMapping KEY_TEAM = new KeyMapping("key.team_settings", GLFW.GLFW_KEY_RIGHT_BRACKET, KeyMapping.CATEGORY_INTERFACE);
	
	@SubscribeEvent
	public static void onKeyInput(InputEvent.Key event)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.screen instanceof WalletScreen && minecraft.player != null)
		{
			if(event.getAction() == GLFW.GLFW_PRESS && event.getKey() == KEY_WALLET.getKey().getValue())
			{
				minecraft.player.clientSideCloseContainer();
			}
		}
		else if(minecraft.player != null && minecraft.screen == null)
		{
			LocalPlayer player = minecraft.player;
			if(KEY_WALLET.isDown())
			{
				
				LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWallet(-1));
				
				if(!LightmansCurrency.getWalletStack(player).isEmpty())
				{
					
					minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ARMOR_EQUIP_LEATHER, 1.25f + player.level.random.nextFloat() * 0.5f, 0.75f));
					
					ItemStack wallet = LightmansCurrency.getWalletStack(player);
					if(!WalletItem.isEmpty(wallet))
						minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.COINS_CLINKING.get(), 1f, 0.4f));
				}
			}
		}
		//Open portable terminal from curios slot
		if(LightmansCurrency.isCuriosLoaded() && event.getAction() == GLFW.GLFW_PRESS && event.getKey() == KEY_PORTABLE_TERMINAL.getKey().getValue() && LCCurios.hasPortableTerminal(minecraft.player))
		{
			LightmansCurrency.PROXY.openTerminalScreen();
		}
		
	}
	
	//Add the wallet button to the gui
	@SubscribeEvent
	public static void onInventoryGuiInit(ScreenEvent.Init.Post event)
	{
		
		Screen screen = event.getScreen();
		
		if(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
		{
			
			AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>)screen;
			
			Minecraft mc = Minecraft.getInstance();
			if(!LightmansCurrency.isCuriosValid(mc.player))
			{
				
				ScreenPosition slotPosition = getWalletSlotPosition(screen instanceof CreativeModeInventoryScreen);
				ScreenPosition buttonPosition = slotPosition.offset(Config.CLIENT.walletButtonOffset.get());
				
				event.addListener(new WalletButton(gui, buttonPosition.x, buttonPosition.y, b -> LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWallet(-1))));
				
				event.addListener(new VisibilityToggleButton(gui, slotPosition.x, slotPosition.y, ClientEvents::toggleVisibility));
				
			}
			
			//Add notification button
			event.addListener(new NotificationButton(gui));
			event.addListener(new TeamManagerButton(gui));
			event.addListener(new TraderRecoveryButton(gui));
			
		}
	}
	
	private static void toggleVisibility(Button button) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		IWalletHandler handler = WalletCapability.lazyGetWalletHandler(player);
		if(handler != null)
		{
			boolean nowVisible = !handler.visible();
			handler.setVisible(nowVisible);
			LightmansCurrencyPacketHandler.instance.sendToServer(new CPacketSetVisible(player.getId(), nowVisible));
		}
	}
	
	//Renders empty gui slot & wallet item
	@SubscribeEvent
	public static void renderInventoryScreen(ContainerScreenEvent.Render.Background event)
	{
		
		Minecraft mc = Minecraft.getInstance();
		if(LightmansCurrency.isCuriosValid(mc.player))
			return;
		
		AbstractContainerScreen<?> screen = event.getContainerScreen();
		
		if(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
		{
			if(screen instanceof CreativeModeInventoryScreen creativeScreen && !creativeScreen.isInventoryOpen())
				return;
			
			IWalletHandler walletHandler = getWalletHandler(); 
			if(walletHandler == null)
				return;
			ScreenPosition slotPosition = getWalletSlotPosition(screen instanceof CreativeModeInventoryScreen);
			RenderSystem.setShaderTexture(0, WALLET_SLOT_TEXTURE);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			//Render slot background
			screen.blit(event.getPoseStack(), screen.getGuiLeft() + slotPosition.x, screen.getGuiTop() + slotPosition.y, 0, 0, 18, 18);
			//Render slot item
			ItemStack wallet = walletHandler.getWallet();
			if(wallet.isEmpty())
				ItemRenderUtil.drawSlotBackground(event.getPoseStack(), screen.getGuiLeft() + slotPosition.x + 1, screen.getGuiTop() + slotPosition.y + 1, WalletSlot.BACKGROUND);
			else
				ItemRenderUtil.drawItemStack(screen, null, wallet, screen.getGuiLeft() + slotPosition.x + 1, screen.getGuiTop() + slotPosition.y + 1);
			//Render slot highlight
			if(isMouseOverWalletSlot(screen, event.getMouseX(), event.getMouseY(), slotPosition))
				AbstractContainerScreen.renderSlotHighlight(event.getPoseStack(), screen.getGuiLeft() + slotPosition.x + 1, screen.getGuiTop() + slotPosition.y + 1, screen.getBlitOffset());
		}
	}
	
	//Renders wallet tooltip
	@SubscribeEvent
	public static void renderInventoryTooltips(ScreenEvent.Render.Post event)
	{
		Screen screen = event.getScreen();
		
		if(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
		{
			AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>)screen;
			
			if(!gui.getMenu().getCarried().isEmpty()) //Don't render tooltips if the held item isn't empty
				return;
			
			if(gui instanceof CreativeModeInventoryScreen creativeScreen && !creativeScreen.isInventoryOpen())
				return;
			
			Minecraft mc = Minecraft.getInstance();
			if(!LightmansCurrency.isCuriosValid(mc.player))
			{
				ScreenPosition slotPosition = getWalletSlotPosition(screen instanceof CreativeModeInventoryScreen);
				
				if(isMouseOverWalletSlot(gui, event.getMouseX(), event.getMouseY(), slotPosition))
				{
					IWalletHandler walletHandler = getWalletHandler();
					ItemStack wallet = walletHandler == null ? ItemStack.EMPTY : walletHandler.getWallet();
					if(!wallet.isEmpty())
						screen.renderComponentTooltip(event.getPoseStack(), ItemRenderUtil.getTooltipFromItem(wallet), event.getMouseX(), event.getMouseY());
				}
			}
			
			//Render notification & team manager button tooltips
			NotificationButton.tryRenderTooltip(event.getPoseStack(), event.getMouseX(), event.getMouseY());
			TeamManagerButton.tryRenderTooltip(event.getPoseStack(), event.getMouseX(), event.getMouseY());
			
		}
	}
	
	//Interact
	@SubscribeEvent
	public static void onInventoryClick(ScreenEvent.MouseButtonPressed.Post event)
	{
		
		Minecraft mc = Minecraft.getInstance();
		if(LightmansCurrency.isCuriosValid(mc.player))
			return;
		
		Screen screen = event.getScreen();
		
		if(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
		{
			AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>)screen;
			if(gui instanceof CreativeModeInventoryScreen creativeScreen && !creativeScreen.isInventoryOpen())
				return;
			ScreenPosition slotPosition = getWalletSlotPosition(screen instanceof CreativeModeInventoryScreen);
			
			
			//Wallet Slot click detection
			if(isMouseOverWalletSlot(gui, event.getMouseX(), event.getMouseY(), slotPosition) && !isMouseOverVisibilityButton(gui, event.getMouseX(), event.getMouseY(), slotPosition))
			{
				ItemStack heldStack = gui.getMenu().getCarried().copy();
				boolean shiftHeld = Screen.hasShiftDown() && !(gui instanceof CreativeModeInventoryScreen);
				LightmansCurrencyPacketHandler.instance.sendToServer(new CPacketWalletInteraction(-1, shiftHeld, heldStack));
				WalletCapability.WalletSlotInteraction(getPlayer(), -1, shiftHeld, heldStack);
			}
			//Normal slot click detection and validation
			else if(Screen.hasShiftDown() && !(gui instanceof CreativeModeInventoryScreen))
			{
				Slot hoveredSlot = gui.getSlotUnderMouse();
				if(hoveredSlot != null)
				{
					Player player = getPlayer();
					int slotIndex = hoveredSlot.container != player.getInventory() ? -1 : hoveredSlot.getContainerSlot();
					if(slotIndex < 0)
						return;
					ItemStack slotItem = player.getInventory().getItem(slotIndex);
					if(WalletSlot.isValidWallet(slotItem))
					{
						ItemStack heldStack = gui.getMenu().getCarried().copy();
						LightmansCurrencyPacketHandler.instance.sendToServer(new CPacketWalletInteraction(slotIndex, true, heldStack));
						WalletCapability.WalletSlotInteraction(getPlayer(), slotIndex, true, heldStack);
						//Cancel event
						event.setCanceled(true);
					}
				}
			}
		}
	}
	
	private static boolean isMouseOverWalletSlot(AbstractContainerScreen<?> gui, double mouseX, double mouseY, ScreenPosition slotPosition)
	{
		return ScreenUtil.isMouseOver(mouseX, mouseY, slotPosition.offset(gui), 18, 18);
	}
	
	private static boolean isMouseOverVisibilityButton(AbstractContainerScreen<?> gui, double mouseX, double mouseY, ScreenPosition slotPosition)
	{
		return ScreenUtil.isMouseOver(mouseX, mouseY, slotPosition.offset(gui), VisibilityToggleButton.SIZE, VisibilityToggleButton.SIZE);
	}
	
	private static IWalletHandler getWalletHandler() {
		Minecraft mc = Minecraft.getInstance();
		assert mc.player != null;
		return WalletCapability.lazyGetWalletHandler(mc.player);
	}
	
	private static Player getPlayer() {
		Minecraft mc = Minecraft.getInstance();
		return mc.player;
	}
	
	private static ScreenPosition getWalletSlotPosition(boolean isCreative) {
		return isCreative ? Config.CLIENT.walletSlotCreative.get() : Config.CLIENT.walletSlot.get();
	}
	
}
