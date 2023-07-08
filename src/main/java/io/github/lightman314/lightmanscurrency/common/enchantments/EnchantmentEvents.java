package io.github.lightman314.lightmanscurrency.common.enchantments;

import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber
public class EnchantmentEvents {

	private static boolean canTick = false;

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if(event.phase == TickEvent.Phase.START)
		{
			//Only do money mending once per second to save resources
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			canTick = server != null && server.getTickCount() % 20 == 0;
		}
	}

	@SubscribeEvent
	public static void onEntityTick(LivingEvent.LivingTickEvent event)
	{

		//Do nothing client-side
		LivingEntity entity = event.getEntity();
		if(!canTick || entity.level().isClientSide)
			return;

		IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(entity);
		if(walletHandler != null)
		{
			MoneyMendingEnchantment.runEntityTick(walletHandler, entity);
			CoinMagnetEnchantment.runEntityTick(walletHandler, entity);
		}
		
	}
	
}
