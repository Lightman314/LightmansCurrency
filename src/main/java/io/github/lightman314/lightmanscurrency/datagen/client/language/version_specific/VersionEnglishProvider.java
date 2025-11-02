package io.github.lightman314.lightmanscurrency.datagen.client.language.version_specific;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.datagen.client.language.TranslationAttachment;
import io.github.lightman314.lightmanscurrency.datagen.client.language.TranslationProvider;
import net.minecraft.data.PackOutput;

public class VersionEnglishProvider extends TranslationAttachment {

    public VersionEnglishProvider(PackOutput output, TranslationProvider parent) { super(output,parent); }

    @Override
    protected void createTranslations() {

        //1.21 exclusive as 1.20.1 has the entire item override and enchantment override as a config option
        this.translateConfigOption(LCConfig.SERVER.moneyMendingInfinityCost,"MM Extra Infinity Cost","The additional cost to repair an item with Infinity applied to it.");

    }

}
