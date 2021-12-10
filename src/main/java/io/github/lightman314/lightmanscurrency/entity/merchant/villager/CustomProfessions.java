package io.github.lightman314.lightmanscurrency.entity.merchant.villager;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomProfessions {

	private static final List<VillagerProfession> PROFESSIONS = Lists.newArrayList();
	
	public static final VillagerProfession BANKER = register("banker", CustomPointsOfInterest.BANKER, CurrencySoundEvents.COINS_CLINKING);
	public static final VillagerProfession CASHIER = register("cashier", CustomPointsOfInterest.CASHIER, CurrencySoundEvents.COINS_CLINKING);
	
	private static VillagerProfession register(String type, PoiType interestType, @Nullable SoundEvent soundEvent)
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
