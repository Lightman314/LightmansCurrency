package io.github.lightman314.lightmanscurrency.features.villagers;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.features.villagers.mods.ConfiguredTradeMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import javax.annotation.Nullable;
import java.util.Optional;

public final class VillagerTradeHelper {

    public static final Identifier WANDERING_TRADER_TYPE = Identifier.withDefaultNamespace("wandering_trader");

    private VillagerTradeHelper() {}

    public static MerchantOffer modifyOffer(Entity villager,MerchantOffer original) {
        Identifier type = getVillagerProfession(villager);
        if(type == null) //Nothing to do if we couldn't extract the type
            return original;
        //Obey the wandering trader config
        if(type.equals(WANDERING_TRADER_TYPE) && !LCConfig.COMMON.changeWanderingTrades.get())
            return original;
        boolean vanilla = type.getNamespace().equals("minecraft");
        if(vanilla && !LCConfig.COMMON.changeVanillaTrades.get())
            return original;
        if(!vanilla && !LCConfig.COMMON.changeModdedTrades.get())
            return original;
        //Apply mods
        ConfiguredTradeMod mod = LCConfig.COMMON.professionEmeraldReplacementOverrides.get().getModFor(type.toString());
        ItemCost itemA = mod.modifyCost(villager,original.getItemCostA());
        Optional<ItemCost> itemB = original.getItemCostB().map(c -> mod.modifyCost(villager,c));
        ItemStack itemC = mod.modifyResult(villager,original.getResult());
        return new MerchantOffer(itemA,itemB,itemC,original.getMaxUses(),original.getXp(),original.getPriceMultiplier());
    }

    @Nullable
    public static Identifier getVillagerProfession(Entity villager) {
        if(villager instanceof Villager v)
            return v.getVillagerData().profession().unwrapKey().map(ResourceKey::identifier).orElse(null);
        //Use the villagers entity type as a fallback if the villager doesn't have a profession
        //Should automatically provide compatibility with other mods wandering trader types
        return BuiltInRegistries.ENTITY_TYPE.getKey(villager.getType());
    }

}