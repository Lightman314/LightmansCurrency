package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.FreezerBlock;
import io.github.lightman314.lightmanscurrency.client.colors.TicketColor;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.TicketModifierSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.TicketSlot;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.client.ItemTradeButtonRenderer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

	@SubscribeEvent
	public static void registerItemColors(ColorHandlerEvent.Item event)
	{
		//LightmansCurrency.LogInfo("Registering Item Colors for Ticket Items");
		event.getItemColors().register(new TicketColor(), ModItems.TICKET.get(), ModItems.TICKET_MASTER.get());
	}
	
	@SubscribeEvent
	public static void stitchTextures(TextureStitchEvent.Pre event) {
		if(event.getMap().location() == PlayerContainer.BLOCK_ATLAS) {
			//Add coin/wallet slot backgrounds
			//LightmansCurrency.LogInfo("Adding empty slot sprites to the texture atlas.");
			event.addSprite(CoinSlot.EMPTY_COIN_SLOT);
			event.addSprite(TicketSlot.EMPTY_TICKET_SLOT);
			event.addSprite(WalletSlot.EMPTY_WALLET_SLOT);
			event.addSprite(EasySlot.EMPTY_SLOT_BG);
			event.addSprite(UpgradeInputSlot.EMPTY_UPGRADE_SLOT);
			event.addSprite(TicketModifierSlot.EMPTY_DYE_SLOT);
			event.addSprite(ItemTradeButtonRenderer.NBT_SLOT);
		}
	}

	@SubscribeEvent
	public static void registerAdditionalModels(ModelRegistryEvent event) {
		for(FreezerBlock block : ModBlocks.FREEZER.getAll())
			ModelLoader.addSpecialModel(block.getDoorModel());
	}
	
}
