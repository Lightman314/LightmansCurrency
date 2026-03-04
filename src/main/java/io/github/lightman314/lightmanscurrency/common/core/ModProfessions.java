package io.github.lightman314.lightmanscurrency.common.core;

import com.google.common.collect.ImmutableSet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModProfessions {

    public static final DeferredRegister<VillagerProfession> REGISTER = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, LightmansCurrency.MODID);

	public static final Supplier<VillagerProfession> BANKER = register("banker", ModPointsOfInterest.BANKER_KEY,ModSounds.COINS_CLINKING);
	public static final Supplier<VillagerProfession> CASHIER = register("cashier", ModPointsOfInterest.CASHIER_KEY,ModSounds.COINS_CLINKING);

    public static DeferredHolder<VillagerProfession,VillagerProfession> register(String name,ResourceKey<PoiType> poi,Supplier<SoundEvent> sound) {
        return register(name,() -> new VillagerProfession(name,p -> p.is(poi),p -> p.is(poi),ImmutableSet.of(),ImmutableSet.of(),sound.get()));
    }
    public static DeferredHolder<VillagerProfession,VillagerProfession> register(String name,Supplier<VillagerProfession> factory) {
        return REGISTER.register(name,factory);
    }
	
}
