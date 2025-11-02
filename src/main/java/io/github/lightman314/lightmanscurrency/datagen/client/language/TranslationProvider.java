package io.github.lightman314.lightmanscurrency.datagen.client.language;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.text.*;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.data.LanguageProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TranslationProvider extends LanguageProvider {


    private final PackOutput output;
    protected TranslationProvider(PackOutput output) { this(output, LightmansCurrency.MODID, "en_us_dev"); }
    protected TranslationProvider(PackOutput output, String locale) { this(output, LightmansCurrency.MODID, locale); }

    protected TranslationProvider(PackOutput output, String modid, String locale) { super(output, modid, locale); this.output = output; }

    @Override
    protected final void addTranslations() {
        this.createTranslations();
        this.callAttachments(this.output,TranslationProvider::addTranslations);
    }

    protected abstract void createTranslations();

    protected void callAttachments(PackOutput output, Consumer<TranslationAttachment> handler) { }

    /**
     * Gets the name of this color in this translations' language.<br>
     * If not overridden, it will return the English name.
     */
    protected String getColorName(Color color) { return color.getPrettyName(); }
    /**
     * Gets the name of this wood type in this translations' language.<br>
     * If not overridden, it will return the English name.
     */
    protected String getWoodTypeName(WoodType type) { return type.displayName; }

    protected final void translate(TextEntry entry, String translation)
    {
        this.add(entry.getKey(), translation);
    }

    protected final void translate(CombinedTextEntry entry, String translation)
    {
        entry.forEachKey(key -> this.add(key,translation));
    }

    protected final void translate(MultiLineTextEntry entry, String... translations)
    {
        List<String> lines = ImmutableList.copyOf(translations);
        for(int i = 0; i < lines.size(); ++i)
            this.add(entry.getKey(i),lines.get(i));
    }

    protected final void translate(TimeUnitTextEntry entry, String fullText, String pluralText, String shortText)
    {
        this.translate(entry.fullText, fullText);
        this.translate(entry.pluralText, pluralText);
        this.translate(entry.shortText, shortText);
    }

    protected final void translate(DualTextEntry entry, String first, String second)
    {
        this.translate(entry.first,first);
        this.translate(entry.second,second);
    }

    protected final void translate(StatKey<?,?> statistic, String text)
    {
        this.add(StatType.getTranslationKey(statistic.key), text);
    }

    protected final void translateWooden(TextEntryBundle<WoodType> bundle, String format)
    {
        this.translate(bundle,format,this::getWoodTypeName);
    }

    protected final void translateColored(TextEntryBundle<Color> bundle, String format)
    {
        this.translate(bundle,format,this::getColorName);
    }

    protected final <T> void translate(TextEntryBundle<T> bundle, String format, Function<T,String> keyToText)
    {
        bundle.forEach((key,entry) -> this.translate(entry,format.formatted(keyToText.apply(key))));
    }

    protected final void translateWoodenAndColored(TextEntryBiBundle<WoodType,Color> bundle, String format)
    {
        this.translate(bundle, format, this::getWoodTypeName, this::getColorName);
    }

    protected final <S,T> void translate(TextEntryBiBundle<S,T> bundle, String format, Function<S,String> key1ToText, Function<T,String> key2ToText)
    {
        bundle.forEach((key1,key2,entry) -> this.translate(entry,format.formatted(key1ToText.apply(key1),key2ToText.apply(key2))));
    }

    protected final void translateAncientCoin(AncientCoinType type, String text)
    {
        ItemStack item = type.asItem();
        this.add(item.getDescriptionId(),text);
    }

    protected final void translateAncientCoinInitial(AncientCoinType type, String text)
    {
        this.translate(new TextEntry(type.initialKey()),text);
    }


    protected final void translateGuide(ResourceLocation guide, String name, String landingText)
    {
        String prefix = "guide." + guide.getNamespace() + "." + guide.getPath() + ".";
        this.add(prefix + "name",name);
        this.add(prefix + "landing_text",landingText);
    }

    protected final void translateConfigName(ConfigFile file,String name) {
        file.confirmSetup();
        this.add(ConfigFile.translationForFile(file.getFileID()),name);
    }

    protected final void translateConfigSection(ConfigFile file, String section, String name, String... comments)
    {
        file.confirmSetup();
        this.add(ConfigFile.translationForSection(file.getFileID(),section),name);
        this.translateConfigComment(file,section,comments);
    }

    protected final void translateConfigOption(ConfigOption<?> option, String optionName, String... comments)
    {
        ConfigFile file = option.getFile();
        if(file == null)
        {
            LightmansCurrency.LogError("Cannot translate config option as its config file has not been initialized!");
            return;
        }
        String optionKey = option.getFullName();
        this.add(ConfigFile.translationForOption(option.getFile().getFileID(),option.getFullName()),optionName);
        this.translateConfigComment(file,optionKey,comments);
    }

    protected final void translateConfigComment(ConfigFile file, String section, String... translation)
    {
        file.confirmSetup();
        this.translate(new MultiLineTextEntry(ConfigFile.translationForComment(file.getFileID(),section)),translation);
    }

}
