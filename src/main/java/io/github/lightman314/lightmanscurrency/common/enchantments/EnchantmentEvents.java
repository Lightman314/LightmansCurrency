package io.github.lightman314.lightmanscurrency.common.enchantments;

import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class EnchantmentEvents {

	private static boolean canTick = false;

	private static final Map<Integer,Thread> threadCache = new HashMap<>();

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if(event.phase == TickEvent.Phase.START)
		{
			//Only do money mending once per second to saveItem resources
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			canTick = server != null && server.getTickCount() % 20 == 0;
		}
	}

	@SubscribeEvent
	public static void onEntityTick(LivingEvent.LivingTickEvent event)
	{
		//Do nothing client-side
		LivingEntity entity = event.getEntity();
		if(!canTick || entity.level.isClientSide)
			return;

		//Place enchantment ticks on a different thread to avoid causing more lag than necessary
		Thread latestThread = threadCache.get(entity.getId());
		if(latestThread != null && latestThread.isAlive())
			return;

		latestThread = new Thread(() -> {
			if(entity instanceof Player player)
				MoneyMendingEnchantment.runEntityTick(player);

			IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(entity);
			if(walletHandler != null)
				CoinMagnetEnchantment.runEntityTick(walletHandler, entity);
		});
		threadCache.put(entity.getId(),latestThread);
		latestThread.start();
		
	}
	
}
