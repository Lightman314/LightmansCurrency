package io.github.lightman314.lightmanscurrency.entity.merchant.villager;

import com.google.common.collect.ImmutableSet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(LightmansCurrency.MODID)
public class CustomProfessions {
	
	public static void init()
	{
		ModRegistries.PROFESSIONS.register("banker", () -> new VillagerProfession("banker", CustomPointsOfInterest.BANKER, ImmutableSet.of(), ImmutableSet.of(), CurrencySoundEvents.COINS_CLINKING));
		ModRegistries.PROFESSIONS.register("cashier", () -> new VillagerProfession("cashier", CustomPointsOfInterest.CASHIER, ImmutableSet.of(), ImmutableSet.of(), CurrencySoundEvents.COINS_CLINKING));
	}
	
	public static final VillagerProfession BANKER = null;
	public static final VillagerProfession CASHIER = null;
	
}
