package io.github.lightman314.lightmanscurrency.entity.merchant.villager;

import com.google.common.collect.ImmutableSet;

import io.github.lightman314.lightmanscurrency.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.core.ModSounds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.RegistryObject;

public class CustomProfessions {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		BANKER = ModRegistries.PROFESSIONS.register("banker", () -> create("banker", CustomPointsOfInterest.BANKER_KEY, ModSounds.COINS_CLINKING.get()));
		
		CASHIER = ModRegistries.PROFESSIONS.register("cashier", () -> create("cashier", CustomPointsOfInterest.CASHIER_KEY, ModSounds.COINS_CLINKING.get()));
		
	}
	
	public static final RegistryObject<VillagerProfession> BANKER;
	public static final RegistryObject<VillagerProfession> CASHIER;
	
	private static VillagerProfession create(String name, ResourceKey<PoiType> poi, SoundEvent sound) {
		return new VillagerProfession(name, p -> p.is(poi), p -> p.is(poi), ImmutableSet.of(), ImmutableSet.of(), sound);
	}
	
}
