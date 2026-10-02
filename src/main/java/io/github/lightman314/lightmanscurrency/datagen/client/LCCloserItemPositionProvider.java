package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation.FacingRotation;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation.FacingUpRotation;
import io.github.lightman314.lightmanscurrency.datagen.client.builders.ItemPositionBuilder;
import io.github.lightman314.lightmanscurrency.datagen.client.generators.ItemPositionProvider;
import net.minecraft.data.PackOutput;

public class LCCloserItemPositionProvider extends ItemPositionProvider {

    public LCCloserItemPositionProvider(PackOutput output) { super(output, LCApi.MODID,LCApi.MODID + "/closer_items"); }

    @Override
    protected void addEntries() {

        //Single Shelf
        this.addData("single_shelf",ItemPositionBuilder.builder()
                        .withGlobalScale(14f/16f)
                        .withGlobalExtraCount(2)
                        .withGlobalExtraOffset(MO,MO,-0.075f)
                        .withGlobalRotationType(FacingRotation.INSTANCE)
                        .withSimpleEntry(0.5f,9f/16f,14.5f/16f));
        //Double Shelf
        this.addData("double_shelf",ItemPositionBuilder.builder()
                        .withGlobalScale(5.5f/16f)
                        .withGlobalExtraCount(2)
                        .withGlobalExtraOffset(MO,MO,-0.075f)
                        .withGlobalRotationType(FacingRotation.INSTANCE)
                        .withSimpleEntry(0.25f,13f/16f,14.5f/16f)
                        .withSimpleEntry(0.75f,13f/16f,14.5f/16f)
                        .withSimpleEntry(0.25f,5f/16f,14.5f/16f)
                        .withSimpleEntry(0.75f,5f/16f,14.5f/16f));

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

        //Vending Machine
        this.addData("vending_machine",ItemPositionBuilder.builder()
                .withGlobalScale(0.3f)
                .withGlobalRotationType(FacingRotation.INSTANCE)
                .withGlobalExtraCount(6)
                .withGlobalExtraOffset(MO,MO,0.075f)
                .withGlobalMinLight(10)
                .withSimpleEntry(3.5f/16f,27f/16f,6f/16f)
                .withSimpleEntry(9.5f/16f,27f/16f,6f/16f)
                .withSimpleEntry(3.5f/16f,20f/16f,6f/16f)
                .withSimpleEntry(9.5f/16f,20f/16f,6f/16f)
                .withSimpleEntry(3.5f/16f,13f/16f,6f/16f)
                .withSimpleEntry(9.5f/16f,13f/16f,6f/16f));

        //Large Vending Machine
        this.addData("large_vending_machine",ItemPositionBuilder.builder()
                .withGlobalScale(0.3f)
                .withGlobalRotationType(FacingRotation.INSTANCE)
                .withGlobalExtraCount(6)
                .withGlobalExtraOffset(MO,MO,0.075f)
                .withGlobalMinLight(10)
                .withSimpleEntry(3.5f/16f,27f/16f,6f/16f)
                .withSimpleEntry(10.5f/16f,27f/16f,6f/16f)
                .withSimpleEntry(18.5f/16f,27f/16f,6f/16f)
                .withSimpleEntry(25.5f/16f,27f/16f,6f/16f)
                .withSimpleEntry(3.5f/16f,20f/16f,6f/16f)
                .withSimpleEntry(10.5f/16f,20f/16f,6f/16f)
                .withSimpleEntry(18.5f/16f,20f/16f,6f/16f)
                .withSimpleEntry(25.5f/16f,20f/16f,6f/16f)
                .withSimpleEntry(3.5f/16f,13f/16f,6f/16f)
                .withSimpleEntry(10.5f/16f,13f/16f,6f/16f)
                .withSimpleEntry(18.5f/16f,13f/16f,6f/16f)
                .withSimpleEntry(25.5f/16f,13f/16f,6f/16f));


    }

}