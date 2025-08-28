package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.configured;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.VillagerTradeMod;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class ConfiguredItemListing implements VillagerTrades.ItemListing
{

    final VillagerTrades.ItemListing tradeSource;
    final Supplier<VillagerTradeMod> modSupplier;

    /**
     * A modified Item Listing that takes an existing trade/listing and converts a given item into another item.
     * Warning: Replaced items do not keep any NBT data, so this should not be used for items that can be enchanted.
     * Used by LC to replace Emeralds with Emerald Coins.
     * @param tradeSource The Item Listing to modify.
     * @param modSupplier A supplier of the {@link VillagerTradeMod} that should be used to modify the item listing.
     */
    public ConfiguredItemListing(@Nonnull VillagerTrades.ItemListing tradeSource, @Nonnull Supplier<VillagerTradeMod> modSupplier) {
        this.tradeSource = tradeSource;
        this.modSupplier = modSupplier;
    }

    @Override
    public MerchantOffer getOffer(@Nonnull Entity trader, @Nonnull RandomSource random) {
        try {
            int attempts = 0;
            MerchantOffer offer = this.tradeSource.getOffer(trader, random);
            if(offer == null)
                return null;

            assert offer != null;
            VillagerTradeMod mod = this.modSupplier.get();
            ItemStack itemA = mod.modifyCost(trader,offer.getBaseCostA());
            ItemStack itemB = mod.modifyCost(trader,offer.getCostB());
            ItemStack itemC = mod.modifyResult(trader,offer.getResult());

            return new MerchantOffer(itemA, itemB, itemC, offer.getUses(), offer.getMaxUses(), offer.getXp(), offer.getPriceMultiplier(), offer.getDemand());
        } catch(Throwable t) {
            LightmansCurrency.LogWarning("Error converting trade:", t);
            return null;
        }
    }

}
