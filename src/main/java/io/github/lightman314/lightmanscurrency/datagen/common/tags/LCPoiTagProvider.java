package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.CustomPointsOfInterest;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.tags.PoiTypeTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class LCPoiTagProvider extends PoiTypeTagsProvider {

    public LCPoiTagProvider(DataGenerator output, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, LightmansCurrency.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {

        this.tag(PoiTypeTags.ACQUIRABLE_JOB_SITE)
                .add(CustomPointsOfInterest.BANKER_KEY)
                .add(CustomPointsOfInterest.CASHIER_KEY);

    }
}