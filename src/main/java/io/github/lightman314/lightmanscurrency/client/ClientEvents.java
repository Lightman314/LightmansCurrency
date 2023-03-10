package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.client.gui.util.ScreenUtil;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.glfw.GLFW;

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
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, value = Dist.CLIENT)
public class ClientEvents {

	public static final ResourceLocation WALLET_SLOT_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/wallet_slot.png");
	
	public static final KeyBinding KEY_WALLET = new KeyBinding("key.wallet", GLFW.GLFW_KEY_V, "key.categories.inventory");
	public static final KeyBinding KEY_PORTABLE_TERMINAL = new KeyBinding("key.portable_terminal", GLFW.GLFW_KEY_BACKSLASH, "key.categories.inventory");
	//public static final KeyMapping KEY_TEAM = new KeyMapping("key.team_settings", GLFW.GLFW_KEY_RIGHT_BRACKET, "key.categories.ui");
	
	@SubscribeEvent
	public static void onKeyInput(InputEvent.KeyInputEvent event)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.screen instanceof WalletScreen)
		{
			if(event.getAction() == GLFW.GLFW_PRESS && event.getKey() == KEY_WALLET.getKey().getValue())
			{
				minecraft.player.clientSideCloseContainer();
			}
		}
		else if(minecraft.player != null && minecraft.screen == null)
		{
			ClientPlayerEntity player = minecraft.player;
			if(KEY_WALLET.isDown())
			{
				
				LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWallet(-1));
				
				if(!LightmansCurrency.getWalletStack(player).isEmpty())
				{
					
					minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.ARMOR_EQUIP_LEATHER, 1.25f + player.level.random.nextFloat() * 0.5f, 0.75f));
					
					ItemStack wallet = LightmansCurrency.getWalletStack(player);
					if(!WalletItem.isEmpty(wallet))
						minecraft.getSoundManager().play(SimpleSound.forUI(ModSounds.COINS_CLINKING, 1f, 0.4f));
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
	public static void onInventoryGuiInit(GuiScreenEvent.InitGuiEvent.Post event)
	{
		
		Screen screen = event.getGui();
		
		if(screen instanceof InventoryScreen || screen instanceof CreativeScreen)
		{
			
			ContainerScreen<?> gui = (ContainerScreen<?>)screen;
			
			Minecraft mc = Minecraft.getInstance();
			if(!LightmansCurrency.isCuriosValid(mc.player))
			{

				ScreenPosition slotPosition = getWalletSlotPosition(screen instanceof CreativeScreen);
				ScreenPosition buttonPosition = slotPosition.offset(Config.CLIENT.walletButtonOffset.get());
				
				event.addWidget(new WalletButton(gui, buttonPosition.x, buttonPosition.y, b -> LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWallet(-1))));
				
				event.addWidget(new VisibilityToggleButton(gui, slotPosition.x, slotPosition.y, ClientEvents::toggleVisibility));
				
			}
			
			//Add notification button
			event.addWidget(new NotificationButton(gui));
			event.addWidget(new TeamManagerButton(gui));
			event.addWidget(new TraderRecoveryButton(gui));
			
		}
	}
	
	private static void toggleVisibility(Button button) {
		Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
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
	public static void renderInventoryScreen(GuiContainerEvent.DrawBackground event)
	{
		
		Minecraft mc = Minecraft.getInstance();
		if(LightmansCurrency.isCuriosValid(mc.player))
			return;
		
		ContainerScreen<?> screen = event.getGuiContainer();
		
		if(screen instanceof InventoryScreen || screen instanceof CreativeScreen)
		{
			if(screen instanceof CreativeScreen) {
				CreativeScreen creativeScreen = (CreativeScreen)screen;
				if(creativeScreen.getSelectedTab() != ItemGroup.TAB_INVENTORY.getId())
					return;
			}
			
			IWalletHandler walletHandler = getWalletHandler(); 
			if(walletHandler == null)
				return;
			ScreenPosition slotPosition = getWalletSlotPosition(screen instanceof CreativeScreen);
			RenderUtil.bindTexture(WALLET_SLOT_TEXTURE);
			RenderUtil.color4f(1f, 1f, 1f, 1f);
			//Render slot background
			screen.blit(event.getMatrixStack(), screen.getGuiLeft() + slotPosition.x, screen.getGuiTop() + slotPosition.y, 0, 0, 18, 18);
			//Render slot item
			ItemStack wallet = walletHandler.getWallet();
			if(wallet.isEmpty())
				ItemRenderUtil.drawSlotBackground(event.getMatrixStack(), screen.getGuiLeft() + slotPosition.x + 1, screen.getGuiTop() + slotPosition.y + 1, WalletSlot.BACKGROUND);
			else
				ItemRenderUtil.drawItemStack(screen, null, wallet, screen.getGuiLeft() + slotPosition.x + 1, screen.getGuiTop() + slotPosition.y + 1);
			//Render slot highlight
			if(isMouseOverWalletSlot(screen, event.getMouseX(), event.getMouseY(), slotPosition))
				RenderUtil.renderSlotHighlight(event.getMatrixStack(), screen.getGuiLeft() + slotPosition.x + 1, screen.getGuiTop() + slotPosition.y + 1, screen.getBlitOffset());
		}
	}
	
	//Renders wallet tooltip
	@SubscribeEvent
	public static void renderInventoryTooltips(GuiScreenEvent.DrawScreenEvent.Post event)
	{
		Screen screen = event.getGui();
		
		if(screen instanceof InventoryScreen || screen instanceof CreativeScreen)
		{
			ContainerScreen<?> gui = (ContainerScreen<?>)screen;
			
			if(!getPlayer().inventory.getCarried().isEmpty()) //Don't render tooltips if the held item isn't empty
				return;
			
			if(gui instanceof CreativeScreen) {
				CreativeScreen creativeScreen = (CreativeScreen)gui;
				if(creativeScreen.getSelectedTab() != ItemGroup.TAB_INVENTORY.getId())
					return;
			}
			
			Minecraft mc = Minecraft.getInstance();
			if(!LightmansCurrency.isCuriosValid(mc.player))
			{
				ScreenPosition slotPosition = getWalletSlotPosition(screen instanceof CreativeScreen);
				
				if(isMouseOverWalletSlot(gui, event.getMouseX(), event.getMouseY(), slotPosition))
				{
					IWalletHandler walletHandler = getWalletHandler();
					ItemStack wallet = walletHandler == null ? ItemStack.EMPTY : walletHandler.getWallet();
					if(!wallet.isEmpty())
						screen.renderComponentTooltip(event.getMatrixStack(), ItemRenderUtil.getTooltipFromItem(wallet), event.getMouseX(), event.getMouseY());
				}
			}
			
			//Render notification & team manager button tooltips
			NotificationButton.tryRenderTooltip(event.getMatrixStack(), event.getMouseX(), event.getMouseY());
			TeamManagerButton.tryRenderTooltip(event.getMatrixStack(), event.getMouseX(), event.getMouseY());
			
		}
	}
	
	//Interact
	@SubscribeEvent
	public static void onInventoryClick(GuiScreenEvent.MouseClickedEvent.Pre event)
	{
		
		Minecraft mc = Minecraft.getInstance();
		if(LightmansCurrency.isCuriosValid(mc.player))
			return;
		
		Screen screen = event.getGui();
		
		if(screen instanceof InventoryScreen || screen instanceof CreativeScreen)
		{
			ContainerScreen<?> gui = (ContainerScreen<?>)screen;
			if(gui instanceof CreativeScreen) {
				CreativeScreen creativeScreen = (CreativeScreen)gui;
				if(creativeScreen.getSelectedTab() != ItemGroup.TAB_INVENTORY.getId())
					return;
			}
			ScreenPosition slotPosition = getWalletSlotPosition(screen instanceof CreativeScreen);


			//Wallet Slot click detection
			if(isMouseOverWalletSlot(gui, event.getMouseX(), event.getMouseY(), slotPosition) && !isMouseOverVisibilityButton(gui, event.getMouseX(), event.getMouseY(), slotPosition))
			{
				ItemStack heldStack = getPlayer().inventory.getCarried().copy();
				boolean shiftHeld = Screen.hasShiftDown() && !(gui instanceof CreativeScreen);
				LightmansCurrencyPacketHandler.instance.sendToServer(new CPacketWalletInteraction(-1, shiftHeld, heldStack));
				WalletCapability.WalletSlotInteraction(getPlayer(), -1, shiftHeld, heldStack);
			}
			//Normal slot click detection and validation
			else if(Screen.hasShiftDown() && !(gui instanceof CreativeScreen))
			{
				Slot hoveredSlot = gui.getSlotUnderMouse();
				if(hoveredSlot != null)
				{
					PlayerEntity player = getPlayer();
					int slotIndex = hoveredSlot.container != player.inventory ? -1 : hoveredSlot.getSlotIndex();
					if(slotIndex < 0)
						return;
					ItemStack slotItem = player.inventory.getItem(slotIndex);
					if(WalletSlot.isValidWallet(slotItem))
					{
						ItemStack heldStack = getPlayer().inventory.getCarried().copy();
						LightmansCurrencyPacketHandler.instance.sendToServer(new CPacketWalletInteraction(slotIndex, true, heldStack));
						WalletCapability.WalletSlotInteraction(getPlayer(), slotIndex, true, heldStack);
						//Cancel event
						event.setCanceled(true);
					}
				}
			}
		}
	}
	
	private static boolean isMouseOverWalletSlot(ContainerScreen<?> gui, double mouseX, double mouseY, ScreenPosition slotPosition)
	{
		return ScreenUtil.isMouseOver(mouseX, mouseY, slotPosition.offset(gui), 18, 18);
	}
	
	private static boolean isMouseOverVisibilityButton(ContainerScreen<?> gui, double mouseX, double mouseY, ScreenPosition slotPosition)
	{
		return ScreenUtil.isMouseOver(mouseX, mouseY, slotPosition.offset(gui), VisibilityToggleButton.SIZE, VisibilityToggleButton.SIZE);
	}
	
	private static IWalletHandler getWalletHandler() {
		Minecraft mc = Minecraft.getInstance();
		return WalletCapability.lazyGetWalletHandler(mc.player);
	}
	
	private static PlayerEntity getPlayer() {
		Minecraft mc = Minecraft.getInstance();
		return mc.player;
	}
	
	private static ScreenPosition getWalletSlotPosition(boolean isCreative) {
		return isCreative ? Config.CLIENT.walletSlotCreative.get() : Config.CLIENT.walletSlot.get();
	}
	
}
