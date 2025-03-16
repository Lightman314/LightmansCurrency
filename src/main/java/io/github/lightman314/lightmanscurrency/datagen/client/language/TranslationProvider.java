package io.github.lightman314.lightmanscurrency.datagen.client.language;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.text.*;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.data.LanguageProvider;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

public abstract class TranslationProvider extends LanguageProvider {


    protected TranslationProvider(PackOutput output) { this(output, LightmansCurrency.MODID, "en_us_dev"); }
    protected TranslationProvider(PackOutput output, String locale) { this(output, LightmansCurrency.MODID, locale); }

    protected TranslationProvider(PackOutput output, String modid, String locale) { super(output, modid, locale); }

    /**
     * Gets the name of this color in this translations' language.<br>
     * If not overridden, it will return the English name.
     */
    protected String getColorName(@Nonnull Color color) { return color.getPrettyName(); }
    /**
     * Gets the name of this wood type in this translations' language.<br>
     * If not overridden, it will return the English name.
     */
    protected String getWoodTypeName(@Nonnull WoodType type) { return type.displayName; }

    protected final void translate(@Nonnull TextEntry entry, @Nonnull String translation)
    {
        this.add(entry.getKey(), translation);
    }

    protected final void translate(@Nonnull CombinedTextEntry entry, @Nonnull String translation)
    {
        entry.forEachKey(key -> this.add(key,translation));
    }

    protected final void translate(@Nonnull MultiLineTextEntry entry, @Nonnull String... translations)
    {
        List<String> lines = ImmutableList.copyOf(translations);
        for(int i = 0; i < lines.size(); ++i)
            this.add(entry.getKey(i),lines.get(i));
    }

    protected final void translate(@Nonnull TimeUnitTextEntry entry, @Nonnull String fullText, @Nonnull String pluralText, @Nonnull String shortText)
    {
        this.translate(entry.fullText, fullText);
        this.translate(entry.pluralText, pluralText);
        this.translate(entry.shortText, shortText);
    }

    protected final void translate(@Nonnull AdvancementTextEntry entry, @Nonnull String title, @Nonnull String description)
    {
        this.translate(entry.titleText,title);
        this.translate(entry.descriptionText,description);
    }

    protected final void translate(@Nonnull ResourcePackTextEntry entry, @Nonnull String name, @Nonnull String description)
    {
        this.translate(entry.nameText,name);
        this.translate(entry.descriptionText,description);
    }

    protected final void translate(@Nonnull StatKey<?,?> statistic, @Nonnull String text)
    {
        this.add(StatType.getTranslationKey(statistic.key), text);
    }

    protected final void translateWooden(@Nonnull TextEntryBundle<WoodType> bundle, @Nonnull String format)
    {
        this.translate(bundle,format,this::getWoodTypeName);
    }

    protected final void translateColored(@Nonnull TextEntryBundle<Color> bundle, @Nonnull String format)
    {
        this.translate(bundle,format,this::getColorName);
    }

    protected final <T> void translate(@Nonnull TextEntryBundle<T> bundle, @Nonnull String format, @Nonnull Function<T,String> keyToText)
    {
        bundle.forEach((key,entry) -> this.translate(entry,format.formatted(keyToText.apply(key))));
    }

    protected final void translateWoodenAndColored(@Nonnull TextEntryBiBundle<WoodType,Color> bundle, @Nonnull String format)
    {
        this.translate(bundle, format, this::getWoodTypeName, this::getColorName);
    }

    protected final <S,T> void translate(@Nonnull TextEntryBiBundle<S,T> bundle, @Nonnull String format, @Nonnull Function<S,String> key1ToText, @Nonnull Function<T,String> key2ToText)
    {
        bundle.forEach((key1,key2,entry) -> this.translate(entry,format.formatted(key1ToText.apply(key1),key2ToText.apply(key2))));
    }

    protected final void translateAncientCoin(@Nonnull AncientCoinType type, @Nonnull String text)
    {
        ItemStack item = type.asItem();
        this.add(item.getDescriptionId(),text);
    }

    protected final void translateAncientCoinInitial(@Nonnull AncientCoinType type, @Nonnull String text)
    {
        this.translate(new TextEntry(type.initialKey()),text);
    }

}
