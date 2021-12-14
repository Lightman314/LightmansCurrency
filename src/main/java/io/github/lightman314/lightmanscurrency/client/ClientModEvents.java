package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.colors.TicketColor;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.TicketSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

	@SubscribeEvent
	public static void registerItemColors(ColorHandlerEvent.Item event)
	{
		//LightmansCurrency.LogInfo("Registering Item Colors for Ticket Items");
		event.getItemColors().register(new TicketColor(), ModItems.TICKET, ModItems.TICKET_MASTER);
	}
	
	@SubscribeEvent
	public static void stitchTextures(TextureStitchEvent.Pre event) {
		if(event.getMap().getTextureLocation() == PlayerContainer.LOCATION_BLOCKS_TEXTURE) {
			//Add coin/wallet slot backgrounds
			LightmansCurrency.LogInfo("Adding empty coin/ticket/wallet slot sprites to the texture atlas.");
			event.addSprite(CoinSlot.EMPTY_COIN_SLOT);
			event.addSprite(TicketSlot.EMPTY_TICKET_SLOT);
			event.addSprite(WalletSlot.EMPTY_WALLET_SLOT);
			event.addSprite(ItemTradeButton.DEFAULT_BACKGROUND);
		}
	}
	
}
