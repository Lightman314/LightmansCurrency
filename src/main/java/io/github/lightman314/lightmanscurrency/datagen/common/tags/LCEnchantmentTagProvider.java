package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.features.enchantments.LCEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.concurrent.CompletableFuture;

public class LCEnchantmentTagProvider extends EnchantmentTagsProvider {
    public LCEnchantmentTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output,lookupProvider,LCApi.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {

        //My Tags
        this.tag(LCTags.Enchantments.MONEY_MENDING)
                .add(LCEnchantments.MONEY_MENDING);
        this.tag(LCTags.Enchantments.WALLET_ENCHANTMENT)
                .add(LCEnchantments.COIN_MAGNET);

        //Vanilla Tags
        this.tag(EnchantmentTags.TREASURE)
                .addTag(LCTags.Enchantments.MONEY_MENDING);
        this.tag(EnchantmentTags.NON_TREASURE)
                .add(LCEnchantments.COIN_MAGNET);
        this.tag(EnchantmentTags.ON_RANDOM_LOOT)
                .addTag(LCTags.Enchantments.MONEY_MENDING);
        this.tag(EnchantmentTags.TRADEABLE)
                .addTag(LCTags.Enchantments.MONEY_MENDING);

        //Make Mending Enchantments Exclusive
        this.tag(LCTags.Enchantments.EXCLUSIVE_SET_MENDING)
                .add(Enchantments.MENDING)
                .addTag(LCTags.Enchantments.MONEY_MENDING);

    }

}
