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
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, value = Dist.CLIENT)
public class ClientEvents {

	
	public static final KeyBinding KEY_WALLET = new KeyBinding("key.wallet", GLFW.GLFW_KEY_V, "key.categories.inventory");
	
	@SubscribeEvent
	public static void onKeyInput(InputEvent.KeyInputEvent event)
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
				
				LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWallet());
				
				if(!LightmansCurrency.getWalletStack(player).isEmpty())
				{
					minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1.25f + player.world.rand.nextFloat() * 0.5f, 0.75f));
					
					ItemStack wallet = LightmansCurrency.getWalletStack(player);
					if(!WalletItem.isEmpty(wallet))
						minecraft.getSoundHandler().play(SimpleSound.master(CurrencySoundEvents.COINS_CLINKING, 1f, 0.4f));
				}
			}
		}
	}
	
	//Add the wallet button to the gui
	@SubscribeEvent
	public static void onInventoryGuiInit(GuiScreenEvent.InitGuiEvent.Post event)
	{
		if(!Config.CLIENT.renderWalletButton.get())
			return;
		Screen screen = event.getGui();
		if(screen instanceof InventoryScreen || screen instanceof CreativeScreen)
		{
			ContainerScreen<?> gui = (ContainerScreen<?>)screen;
			boolean isCreative = screen instanceof CreativeScreen;
			int xPos = isCreative ? Config.CLIENT.walletButtonCreativeX.get() : Config.CLIENT.walletButtonX.get();
			int yPos = isCreative ? Config.CLIENT.walletButtonCreativeY.get() : Config.CLIENT.walletButtonY.get();
			
			event.addWidget(new WalletButton(gui, xPos, yPos, button -> LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWallet())));
		}
	}
	
}
