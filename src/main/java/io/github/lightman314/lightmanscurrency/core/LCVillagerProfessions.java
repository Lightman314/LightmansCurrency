package io.github.lightman314.lightmanscurrency.core;

import com.google.common.collect.ImmutableSet;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.features.villagers.LCTradeSets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class LCVillagerProfessions {

    private LCVillagerProfessions() {}

    public static final DeferredRegister<VillagerProfession> REGISTER = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION,LCApi.MODID);

    public static final DeferredHolder<VillagerProfession,VillagerProfession> BANKER = register("banker",LCPoiTypes.BANKER,LCSounds.COINS_CLINKING,LCTradeSets.BANKER_SET);
    public static final DeferredHolder<VillagerProfession,VillagerProfession> CASHIER = register("cashier",LCPoiTypes.CASHIER,LCSounds.COINS_CLINKING,LCTradeSets.CASHIER_SET);

    private static DeferredHolder<VillagerProfession,VillagerProfession> register(String name,DeferredHolder<PoiType,PoiType> jobSite,Supplier<SoundEvent> workSound,Int2ObjectMap<ResourceKey<TradeSet>> trades) {
        return REGISTER.register(name,() -> new VillagerProfession(
                Component.translatable("entity." + LCApi.MODID + ".villager." + name),
                poi -> poi.is(jobSite.getKey()),
                poi -> poi.is(jobSite.getKey()),
                ImmutableSet.of(),
                ImmutableSet.of(),
                workSound.get(),
                trades
        ));
    }

}