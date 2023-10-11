package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class LCLanguageProvider extends LanguageProvider {

    protected final String modid;
    protected final String locale;

    public LCLanguageProvider(PackOutput output) { this(output, LightmansCurrency.MODID, "en_us_temp"); }
    public LCLanguageProvider(PackOutput output, String locale) { this(output, LightmansCurrency.MODID, locale); }

    protected LCLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.modid = modid;
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {

        //Only Generates wooden block names, so I don't have to manually add them every time I add a wood compat
        //Will only contain newly added wooden blocks on the patch they are made as a reminder
        // to add names for other MC versions that have different wood compats
        //this.addWoodenBlocks(ModBlocks.SHELF_2x2, "%s 2x2 Shelf", w -> true);

    }

    protected void addWoodenItems(@Nonnull RegistryObjectBundle<? extends Item, WoodType> item, @Nonnull String format, @Nonnull Predicate<WoodType> generate) { item.forEach((woodType, i) -> { if(generate.test(woodType)) this.addItem(i, String.format(format, woodType.displayName));}); }

    protected void addWoodenBlocks(@Nonnull RegistryObjectBundle<? extends Block, WoodType> block, @Nonnull String format, @Nonnull Predicate<WoodType> generate)
    {
        block.forEach((woodType, b) -> { if(generate.test(woodType)) this.addBlock(b, String.format(format, woodType.displayName)); });
    }

}