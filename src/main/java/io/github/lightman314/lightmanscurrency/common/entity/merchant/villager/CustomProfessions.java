package io.github.lightman314.lightmanscurrency.common.entity.merchant.villager;

import com.google.common.collect.ImmutableSet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(LightmansCurrency.MODID)
public class CustomProfessions {
	
	public static void init()
	{
		ModRegistries.PROFESSIONS.register("banker", () -> new VillagerProfession("banker", CustomPointsOfInterest.BANKER, ImmutableSet.of(), ImmutableSet.of(), ModSounds.COINS_CLINKING.get()));
		ModRegistries.PROFESSIONS.register("cashier", () -> new VillagerProfession("cashier", CustomPointsOfInterest.CASHIER, ImmutableSet.of(), ImmutableSet.of(), ModSounds.COINS_CLINKING.get()));
	}
	
	public static final VillagerProfession BANKER = null;
	public static final VillagerProfession CASHIER = null;
	
}
