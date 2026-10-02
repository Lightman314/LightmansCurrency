package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation.FacingRotation;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation.FacingUpRotation;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation.SpinningRotation;
import io.github.lightman314.lightmanscurrency.datagen.client.builders.ItemPositionBuilder;
import io.github.lightman314.lightmanscurrency.datagen.client.generators.ItemPositionProvider;
import net.minecraft.data.PackOutput;

public class LCItemPositionProvider extends ItemPositionProvider {

    public LCItemPositionProvider(PackOutput output) { super(output,LCApi.MODID); }

    @Override
    protected void addEntries() {

        //Display Case
        this.addDataWithBlocks("display_case",ItemPositionBuilder.builder()
                .withGlobalScale(0.75f)
                .withGlobalRotationType(SpinningRotation.createDefault())
                .withSimpleEntry(0.5f,0.5f + 1f/8f,0.5f),
                LCTags.Blocks.GROUP_DISPLAY_CASE);

        //Single Shelf
        this.addDataWithBlocks("single_shelf",ItemPositionBuilder.builder()
                .withGlobalScale(14f/16f)
                .withGlobalRotationType(FacingRotation.INSTANCE)
                .withSimpleEntry(0.5f,9f/16f,14.5f/16f),
                LCTags.Blocks.GROUP_SINGLE_SHELF);
        //Double Shelf
        this.addDataWithBlocks("double_shelf",ItemPositionBuilder.builder()
                .withGlobalScale(5.5f/16f)
                .withGlobalRotationType(FacingRotation.INSTANCE)
                .withSimpleEntry(0.25f,13f/16f,14.5f/16f)
                .withSimpleEntry(0.75f,13f/16f,14.5f/16f)
                .withSimpleEntry(0.25f,5f/16f,14.5f/16f)
                .withSimpleEntry(0.75f,5f/16f,14.5f/16f),
                LCTags.Blocks.GROUP_DOUBLE_SHELF);

        //Card Display
        this.addDataWithBlocks("card_display", ItemPositionBuilder.builder()
                .withGlobalScale(0.375f)
                .withGlobalRotationType(FacingUpRotation.createDefault())
                .withGlobalExtraCount(1)
                .withGlobalExtraOffset(MO,0.2f,MO)
                .withSimpleEntry(5f/16f,12f/16f,12f/16f)
                .withSimpleEntry(11f/16f,12f/16f,12f/16f)
                .withSimpleEntry(5f/16f,9f/16f,4.5f/16f)
                .withSimpleEntry(11f/16f,9f/16f,4.5f/16f),
                LCTags.Blocks.GROUP_CARD_DISPLAY);

        //Vending Machine
        this.addDataWithBlocks("vending_machine",ItemPositionBuilder.builder()
                .withGlobalScale(0.3f)
                .withGlobalRotationType(FacingRotation.INSTANCE)
                .withGlobalExtraCount(2)
                .withGlobalExtraOffset(MO,MO,0.2f)
                .withGlobalMinLight(10)
                .withSimpleEntry(3.5f/16f,27f/16f,6f/16f)
                .withSimpleEntry(9.5f/16f,27f/16f,6f/16f)
                .withSimpleEntry(3.5f/16f,20f/16f,6f/16f)
                .withSimpleEntry(9.5f/16f,20f/16f,6f/16f)
                .withSimpleEntry(3.5f/16f,13f/16f,6f/16f)
                .withSimpleEntry(9.5f/16f,13f/16f,6f/16f),
                LCTags.Blocks.GROUP_VENDING_MACHINE);

        //Large Vending Machine
        this.addDataWithBlocks("large_vending_machine",ItemPositionBuilder.builder()
                .withGlobalScale(0.3f)
                .withGlobalRotationType(FacingRotation.INSTANCE)
                .withGlobalExtraCount(2)
                .withGlobalExtraOffset(MO,MO,0.2f)
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
                .withSimpleEntry(25.5f/16f,13f/16f,6f/16f),
                LCTags.Blocks.GROUP_LARGE_VENDING_MACHINE);

        //Auction Stand
        this.addDataWithBlocks("auction_stand",ItemPositionBuilder.builder()
                .withEntry(0.5f,0.75f,0.5f)
                .withScale(0.4f)
                .withRotationType(SpinningRotation.createDefault())
                .back());

    }

}
