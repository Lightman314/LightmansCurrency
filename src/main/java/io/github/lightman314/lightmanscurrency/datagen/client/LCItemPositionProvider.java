package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation.FacingUpRotation;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation.SpinningRotation;
import io.github.lightman314.lightmanscurrency.datagen.client.builders.ItemPositionBuilder;
import io.github.lightman314.lightmanscurrency.datagen.client.generators.ItemPositionProvider;
import net.minecraft.data.PackOutput;

public class LCItemPositionProvider extends ItemPositionProvider {

    public LCItemPositionProvider(PackOutput output) { super(output,LCApi.MODID); }

    @Override
    protected void addEntries() {

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

        this.addDataWithBlocks("display_case",ItemPositionBuilder.builder()
                .withGlobalScale(0.75f)
                .withGlobalRotationType(SpinningRotation.createDefault())
                .withSimpleEntry(0.5f,0.5f + 1f/8f,0.5f),
                LCTags.Blocks.GROUP_DISPLAY_CASE);

    }

}
