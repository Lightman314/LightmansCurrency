package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation.FacingUpRotation;
import io.github.lightman314.lightmanscurrency.datagen.client.builders.ItemPositionBuilder;
import io.github.lightman314.lightmanscurrency.datagen.client.generators.ItemPositionProvider;
import net.minecraft.data.PackOutput;

public class LCCloserItemPositionProvider extends ItemPositionProvider {

    public LCCloserItemPositionProvider(PackOutput output) { super(output, LCApi.MODID,LCApi.MODID + "/closer_items"); }

    @Override
    protected void addEntries() {

        //Card Display
        this.addData("card_display",ItemPositionBuilder.builder()
                .withGlobalScale(0.375f)
                .withGlobalRotationType(FacingUpRotation.createDefault())
                .withGlobalExtraCount(3)
                .withGlobalExtraOffset(MO, 0.075f, MO)
                .withSimpleEntry(5f/16f, 12f/16f,12f/16f)
                .withSimpleEntry(11f/16f, 12f/16f,12f/16f)
                .withSimpleEntry(5f/16f, 9f/16f,4.5f/16f)
                .withSimpleEntry(11f/16f, 9f/16f,4.5f/16f));


    }

}