package io.github.lightman314.lightmanscurrency.entity.merchant.villager;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomProfessions {

	private static final List<VillagerProfession> PROFESSIONS = new ArrayList<>();
	
	public static final VillagerProfession BANKER = register("banker", CustomPointsOfInterest.BANKER, CurrencySoundEvents.COINS_CLINKING);
	public static final VillagerProfession CASHIER = register("cashier", CustomPointsOfInterest.CASHIER, CurrencySoundEvents.COINS_CLINKING);
	
	private static VillagerProfession register(String type, PointOfInterestType interestType, @Nullable SoundEvent soundEvent)
	{
		VillagerProfession profession = new VillagerProfession(type, interestType, ImmutableSet.of(), ImmutableSet.of(), soundEvent);
		profession.setRegistryName(new ResourceLocation(LightmansCurrency.MODID, type));
		PROFESSIONS.add(profession);
		return profession;
	}
	
	@SubscribeEvent
	public static void registerVillagerProfessions(RegistryEvent.Register<VillagerProfession> event)
	{
		PROFESSIONS.forEach(profession -> event.getRegistry().register(profession));
		PROFESSIONS.clear();
	}
	
	
}
