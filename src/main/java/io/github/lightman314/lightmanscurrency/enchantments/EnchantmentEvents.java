package io.github.lightman314.lightmanscurrency.enchantments;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EnchantmentEvents {

	@SubscribeEvent
	public static void onEntityTick(LivingEvent.LivingTickEvent event)
	{
		LivingEntity entity = event.getEntity();
		//Do nothing client-side
		if(entity.level.isClientSide)
			return;
		
		MoneyMendingEnchantment.runEntityTick(entity);
		
		CoinMagnetEnchantment.runEntityTick(entity);
		
	}
	
}
