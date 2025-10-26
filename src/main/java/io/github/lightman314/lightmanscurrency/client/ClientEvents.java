package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.SyncedConfigFile;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ChestCoinCollectButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.enchantments.MoneyMendingEnchantment;
import io.github.lightman314.lightmanscurrency.common.items.PortableATMItem;
import io.github.lightman314.lightmanscurrency.common.items.PortableTerminalItem;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.network.message.bank.CPacketOpenATM;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketOpenNetworkTerminal;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.wallet.VisibilityToggleButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.NotificationButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.TeamManagerButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.EjectionMenuButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.wallet.WalletButton;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketOpenWallet;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.CPacketSetVisible;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

@EventBusSubscriber(modid = LightmansCurrency.MODID, value = Dist.CLIENT)
public class ClientEvents {

	public static final KeyMapping KEY_WALLET = new KeyMapping(LCText.KEY_WALLET.getKey(), GLFW.GLFW_KEY_V, KeyMapping.CATEGORY_INVENTORY);
	public static final KeyMapping KEY_PORTABLE_TERMINAL = new KeyMapping(LCText.KEY_PORTABLE_TERMINAL.getKey(), GLFW.GLFW_KEY_BACKSLASH, KeyMapping.CATEGORY_INVENTORY);
	public static final KeyMapping KEY_PORTABLE_ATM = new KeyMapping(LCText.KEY_PORTABLE_ATM.getKey(), GLFW.GLFW_KEY_EQUAL, KeyMapping.CATEGORY_INVENTORY);
	
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
				
				new CPacketOpenWallet(-1).send();
				ItemStack wallet = CoinAPI.getApi().getEquippedWallet(player);
				if(!wallet.isEmpty())
				{
					minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.25f + player.level().random.nextFloat() * 0.5f, 0.75f));

					if(!WalletItem.isEmpty(wallet))
						minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.COINS_CLINKING.get(), 1f, 0.4f));
				}
			}
			//Open portable terminal/atm from curios slot
			if(LCCurios.isLoaded() && event.getAction() == GLFW.GLFW_PRESS)
			{
				if(event.getKey() == KEY_PORTABLE_TERMINAL.getKey().getValue() && LCCurios.hasPortableTerminal(minecraft.player))
					new CPacketOpenNetworkTerminal(true).send();
				else if(event.getKey() == KEY_PORTABLE_ATM.getKey().getValue() && LCCurios.hasPortableATM(minecraft.player))
					CPacketOpenATM.sendToServer();
			}
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

			//Add notification button
			event.addListener(new NotificationButton(gui));
			event.addListener(new TeamManagerButton(gui));
			event.addListener(new EjectionMenuButton(gui));

			Minecraft mc = Minecraft.getInstance();

			if(LCCurios.isLoaded())
				return;

			//Add Wallet-Related buttons if Curios doesn't exist
			event.addListener(new WalletButton(gui,CPacketOpenWallet::sendEquippedPacket));

			event.addListener(new VisibilityToggleButton(gui, ClientEvents::toggleVisibility));

		}
		else if(screen instanceof ContainerScreen chestScreen)
		{
			//Add Chest Quick-Collect Button
			event.addListener(ChestCoinCollectButton.chestBuilder().screen(chestScreen).build());
		}

	}
	
	private static void toggleVisibility() {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		WalletHandler handler = WalletHandler.get(player);
		boolean nowVisible = !handler.visible();
		handler.setVisible(nowVisible);
		new CPacketSetVisible(nowVisible).send();
	}
	
	//Renders empty gui slot
	@SubscribeEvent
	public static void renderInventoryScreen(ContainerScreenEvent.Render.Background event)
	{

		if(LCCurios.isLoaded())
			return;

		Minecraft mc = Minecraft.getInstance();
		
		AbstractContainerScreen<?> screen = event.getContainerScreen();
		
		if(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
		{
			if(screen instanceof CreativeModeInventoryScreen creativeScreen && !creativeScreen.isInventoryOpen())
				return;

			EasyGuiGraphics gui = EasyGuiGraphics.create(event);
			ScreenPosition slotPosition = getWalletSlotPosition(screen instanceof CreativeModeInventoryScreen).offsetScreen(screen);
			gui.resetColor();
			//Render slot background
            SpriteUtil.EMPTY_SLOT_NORMAL.render(gui,slotPosition.x,slotPosition.y);
		}
	}
	
	//Renders button tooltips
	@SubscribeEvent
	public static void renderInventoryTooltips(ScreenEvent.Render.Post event)
	{
		
		if(event.getScreen() instanceof InventoryScreen || event.getScreen() instanceof CreativeModeInventoryScreen)
		{
			AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>)event.getScreen();
			
			if(!screen.getMenu().getCarried().isEmpty()) //Don't renderBG tooltips if the held item isn't empty
				return;
			
			if(screen instanceof CreativeModeInventoryScreen creativeScreen && !creativeScreen.isInventoryOpen())
				return;

			EasyGuiGraphics gui = EasyGuiGraphics.create(event);

			//Render notification & team manager button tooltips
			NotificationButton.tryRenderTooltip(gui);
			TeamManagerButton.tryRenderTooltip(gui);
			EjectionMenuButton.tryRenderTooltip(gui);
			
		}
		else if(event.getScreen() instanceof ContainerScreen)
		{
			ChestCoinCollectButton.tryRenderTooltip(EasyGuiGraphics.create(event), event.getMouseX(), event.getMouseY());
		}

	}
	
	public static ScreenPosition getWalletSlotPosition(boolean isCreative) { return isCreative ? LCConfig.CLIENT.walletSlotCreative.get() : LCConfig.CLIENT.walletSlot.get(); }

	@SubscribeEvent
	public static void playerJoinsServer(ClientPlayerNetworkEvent.LoggingIn event) { ConfigFile.loadClientFiles(ConfigFile.LoadPhase.GAME_START); }

	@SubscribeEvent
	public static void playerLeavesServer(ClientPlayerNetworkEvent.LoggingOut event) {
		SyncedConfigFile.onClientLeavesServer();
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	//Add coin value tooltips to non CoinItem coins.
	public static void onItemTooltip(ItemTooltipEvent event) {
		if(event.getEntity() == null || CoinAPI.getApi().NoDataAvailable() || event.getContext().registries() == null)
			return;
		ItemStack stack = event.getItemStack();
		if(CoinAPI.getApi().IsCoin(stack, true))
			ChainData.addCoinTooltips(event.getItemStack(), event.getToolTip(), event.getFlags(), event.getEntity());

		//If item has money mending, display money mending tooltip
		MoneyMendingEnchantment.addEnchantmentTooltips(stack,event.getToolTip(),event.getContext());

		if(LCConfig.SERVER.isLoaded() && LCConfig.SERVER.anarchyMode.get() && stack.getItem() instanceof BlockItem bi)
		{
			Block b = bi.getBlock();
			if(b instanceof IOwnableBlock)
				TooltipItem.insertTooltip(event.getToolTip(),LCText.TOOLTIP_ANARCHY_WARNING.get().withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED));
		}

		//Add Trader Item Data to the tooltip
		TooltipItem.insertTooltip(stack,ModDataComponents.TRADER_ITEM_DATA,event.getContext(),event.getFlags(),event.getToolTip());

		//Key-bind Tooltips
		//Added here because it requires client-side data, so I don't really want to put it in a common class
		if(stack.getItem() instanceof WalletItem) //Put in 2nd line so that it appears just below the name
			appendKeyBindTooltip(event,LCText.TOOLTIP_WALLET_KEY_BIND,ClientEvents.KEY_WALLET);
		if(LCCurios.isLoaded())
		{
			if(stack.getItem() instanceof PortableTerminalItem)
				appendKeyBindTooltip(event, LCText.TOOLTIP_TERMINAL_KEY_BIND,ClientEvents.KEY_PORTABLE_TERMINAL);
			if(stack.getItem() instanceof PortableATMItem)
				appendKeyBindTooltip(event, LCText.TOOLTIP_ATM_KEY_BIND,ClientEvents.KEY_PORTABLE_ATM);
		}

		//Variant Wand tooltip
		if(InventoryUtil.ItemHasTag(stack,LCTags.Items.VARIANT_WANDS))
			TooltipItem.insertTooltip(event.getToolTip(), LCText.TOOLTIP_VARIANT_WAND);

	}

	private static void appendKeyBindTooltip(@Nonnull ItemTooltipEvent event, @Nonnull TextEntry tooltip, @Nonnull KeyMapping key)
	{
		event.getToolTip().add(1,tooltip.get(EasyText.makeMutable(key.getTranslatedKeyMessage()).withStyle(ChatFormatting.YELLOW)));
	}

}
