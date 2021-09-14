package io.github.lightman314.lightmanscurrency.client;

import org.lwjgl.glfw.GLFW;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletScreen;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageOpenWallet;
import io.github.lightman314.lightmanscurrency.util.WalletUtil;
import io.github.lightman314.lightmanscurrency.util.WalletUtil.PlayerWallets;
import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {

	
	public static final KeyMapping KEY_WALLET = new KeyMapping("key.wallet", GLFW.GLFW_KEY_V, "key.categories.inventory");
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event)
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
				if(WalletUtil.getWallets(player).hasEquippedWallet())
				{
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWallet());
					
					minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ARMOR_EQUIP_LEATHER, 1.25f + player.level.random.nextFloat() * 0.5f, 0.75f));
					
					PlayerWallets wallet = WalletUtil.getWallets(player);
					if(wallet.getStoredMoney() > 0)
						minecraft.getSoundManager().play(SimpleSoundInstance.forUI(CurrencySoundEvents.COINS_CLINKING, 1f, 0.4f));
				}
			}
		}
	}
	
}
