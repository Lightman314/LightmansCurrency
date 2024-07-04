package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class LCEnchantmentTagProvider extends EnchantmentTagsProvider {
    public LCEnchantmentTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookup, LightmansCurrency.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider lookup) {
        //My Tag(s)
        this.tag(LCTags.Enchantments.MONEY_MENDING)
                .addOptional(ModEnchantments.MONEY_MENDING.location())
                //Chocolate money mending as an optional entry, just in case it gets removed or have a config file made for it later.
                .addOptional(ModEnchantments.MONEY_MENDING_CHOCOLATE.location());

        //Vanilla Tags
        this.tag(EnchantmentTags.TREASURE)
                .addOptional(ModEnchantments.MONEY_MENDING.location());
        this.tag(EnchantmentTags.NON_TREASURE)
                .addOptional(ModEnchantments.COIN_MAGNET.location());
        this.tag(EnchantmentTags.ON_RANDOM_LOOT)
                .addOptional(ModEnchantments.MONEY_MENDING.location());

        //Make Mending Enchantments Exclusive
        this.tag(LCTags.Enchantments.EXCUSIVE_SET_MENDING)
                .add(Enchantments.MENDING)
                .addTag(LCTags.Enchantments.MONEY_MENDING);
    }
}
