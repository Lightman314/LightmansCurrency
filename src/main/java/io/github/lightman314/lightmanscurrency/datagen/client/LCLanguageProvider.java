package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.integration.tconstruct.TinkersCustomWoodTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class LCLanguageProvider extends LanguageProvider {

    protected final String modid;
    protected final String locale;

    public LCLanguageProvider(DataGenerator output) { this(output, LightmansCurrency.MODID, "generated"); }
    public LCLanguageProvider(DataGenerator output, String locale) { this(output, LightmansCurrency.MODID, locale); }

    protected LCLanguageProvider(DataGenerator output, String modid, String locale) {
        super(output, modid, locale);
        this.modid = modid;
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {

        Predicate<WoodType> currentTest = w -> w.isMod("tconstruct");

        //Will only contain newly added wooden blocks on the patch they are made as a reminder
        // to add names for other MC versions that have different wood compats
        /*
        this.addWoodenBlocks(ModBlocks.AUCTION_STAND, "%s Auction Stand", currentTest);
        this.addWoodenBlocks(ModBlocks.SHELF, "%s Shelf", currentTest);
        this.addWoodenBlocks(ModBlocks.SHELF_2x2, "%s 2x2 Shelf", currentTest);
        this.addWoodenBlocks(ModBlocks.CARD_DISPLAY, "%s Card Display", currentTest);
        this.addWoodenBlocks(ModBlocks.BOOKSHELF_TRADER, "%s Bookshelf Trader", currentTest);
        //*/

    }

    protected void addWoodenItems(@Nonnull RegistryObjectBundle<? extends Item, WoodType> item, @Nonnull String format, @Nonnull Predicate<WoodType> generate) { item.forEach((woodType, i) -> { if(generate.test(woodType)) this.addItem(i, String.format(format, woodType.displayName));}); }

    protected void addWoodenBlocks(@Nonnull RegistryObjectBundle<? extends Block, WoodType> block, @Nonnull String format, @Nonnull Predicate<WoodType> generate)
    {
        block.forEach((woodType, b) -> { if(generate.test(woodType)) this.addBlock(b, String.format(format, woodType.displayName)); });
    }

    protected void addWoodenBlocks(@Nonnull RegistryObjectBiBundle<? extends Block,WoodType,Color> block, @Nonnull String format, @Nonnull Predicate<WoodType> generate)
    {
        block.forEachKey1(w -> {
            if(generate.test(w))
                this.addBlock(() -> block.get(w,Color.RED), String.format(format, w.displayName));
        });
    }



}