package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.configured;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.VillagerTradeMod;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import javax.annotation.Nonnull;
import java.util.Optional;
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
            MerchantOffer offer;
            do {
                offer = this.tradeSource.getOffer(trader, random);
            } while(offer == null && attempts++ < 100);

            if(attempts > 1)
            {
                if(offer == null)
                {
                    LightmansCurrency.LogError("Original Item Listing Class: " + this.tradeSource.getClass().getName());
                    throw new NullPointerException("The original Item Listing of the converted trade returned a null trade offer " + attempts + " times!");
                }
                else
                {
                    LightmansCurrency.LogWarning("Original Item Listing Class: " + this.tradeSource.getClass().getName());
                    LightmansCurrency.LogWarning("Converted Trade took " + attempts + " attempts to receive a non-null trade offer from the original Item Listing!");
                }
            }

            assert offer != null;
            VillagerTradeMod mod = this.modSupplier.get();
            ItemCost itemA = mod.modifyCost(trader,offer.getItemCostA());
            Optional<ItemCost> itemB = offer.getItemCostB();
            if(itemB.isPresent())
                itemB = Optional.of(mod.modifyCost(trader,itemB.get()));
            ItemStack itemC = mod.modifyResult(trader,offer.getResult());

            return new MerchantOffer(itemA, itemB, itemC, offer.getUses(), offer.getMaxUses(), offer.getXp(), offer.getPriceMultiplier(), offer.getDemand());
        } catch(Throwable t) {
            LightmansCurrency.LogDebug("Error converting trade:", t);
            return null;
        }
    }

}
