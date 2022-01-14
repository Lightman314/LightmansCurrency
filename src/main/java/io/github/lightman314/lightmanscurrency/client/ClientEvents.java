package io.github.lightman314.lightmanscurrency.client;
import org.lwjgl.glfw.GLFW;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.WalletButton;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageOpenWallet;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, value = Dist.CLIENT)
public class ClientEvents {

	
	public static final KeyMapping KEY_WALLET = new KeyMapping("key.wallet", GLFW.GLFW_KEY_V, KeyMapping.CATEGORY_INVENTORY);
	public static final KeyMapping KEY_TEAM = new KeyMapping("key.team_settings", GLFW.GLFW_KEY_RIGHT_BRACKET, KeyMapping.CATEGORY_INTERFACE);
	
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
			LocalPlayer player = minecraft.player;
			if(KEY_WALLET.isDown())
			{
				
				LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWallet());
				
				if(!LightmansCurrency.getWalletStack(player).isEmpty())
				{
					
					minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ARMOR_EQUIP_LEATHER, 1.25f + player.level.random.nextFloat() * 0.5f, 0.75f));
					
					ItemStack wallet = LightmansCurrency.getWalletStack(player);
					if(!WalletItem.isEmpty(wallet))
						minecraft.getSoundManager().play(SimpleSoundInstance.forUI(CurrencySoundEvents.COINS_CLINKING, 1f, 0.4f));
				}
			}
			else if(KEY_TEAM.isDown())
			{
				LightmansCurrency.PROXY.openTeamManager();
			}
		}
	}
	
	//Add the wallet button to the gui
	@SubscribeEvent
	public static void onInventoryGuiInit(ScreenEvent.InitScreenEvent.Post event)
	{
		
		if(!Config.CLIENT.renderWalletButton.get())
			return;
		
		Screen screen = event.getScreen();
		
		if(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
		{
			AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>)screen;
			boolean isCreative = screen instanceof CreativeModeInventoryScreen;
			int xPos = isCreative ? Config.CLIENT.walletButtonCreativeX.get() : Config.CLIENT.walletButtonX.get();
			int yPos = isCreative ? Config.CLIENT.walletButtonCreativeY.get() : Config.CLIENT.walletButtonY.get();;
			
			event.addListener(new WalletButton(gui, xPos, yPos, button -> LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWallet())));
		}
	}
	
}
