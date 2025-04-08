package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.RotationHandler;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.datagen.client.builders.ItemPositionBuilder;
import io.github.lightman314.lightmanscurrency.datagen.client.generators.ItemPositionProvider;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.data.PackOutput;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public class LCItemPositionProvider extends ItemPositionProvider {

    public LCItemPositionProvider(@Nonnull PackOutput output) { super(output, LightmansCurrency.MODID); }

    @Override
    protected void addEntries() {
        //Card Display
        this.addDataWithBlocks(VersionUtil.lcResource("card_display"),ItemPositionBuilder.builder()
                .withGlobalScale(0.4f)
                .withGlobalRotationType(RotationHandler.FACING_UP)
                .withGlobalExtraCount(1)
                .withGlobalExtraOffset(new Vector3f(MO, 0.2f, MO))
                .withEntry(new Vector3f(5f/16f, 9f/16f,4.5f/16f))
                .withEntry(new Vector3f(11f/16f, 9f/16f,4.5f/16f))
                .withEntry(new Vector3f(5f/16f, 12f/16f,12f/16f))
                .withEntry(new Vector3f(11f/16f, 12f/16f,12f/16f)),
                LCTags.Blocks.CARD_DISPLAY
        );
        //Display Case
        this.addDataWithBlocks(VersionUtil.lcResource("display_case"),ItemPositionBuilder.builder()
                .withGlobalScale(0.75f)
                .withGlobalRotationType(RotationHandler.SPINNING)
                .withEntry(new Vector3f(0.5f, 0.5f + 2f/16f, 0.5f)),
                ModBlocks.DISPLAY_CASE
        );
        //Freezer
        this.addDataWithBlocks(VersionUtil.lcResource("freezer"), ItemPositionBuilder.builder()
                .withGlobalScale(0.4f)
                .withGlobalRotationType(RotationHandler.FACING)
                .withGlobalExtraCount(2)
                .withGlobalExtraOffset(new Vector3f(MO,MO,0.2f))
                .withEntry(new Vector3f(5f/16f,28f/16f,6f/16f))
                .withEntry(new Vector3f(11f/16f,28f/16f,6f/16f))
                .withEntry(new Vector3f(5f/16f,21f/16f,6f/16f))
                .withEntry(new Vector3f(11f/16f,21f/16f,6f/16f))
                .withEntry(new Vector3f(5f/16f,14f/16f,6f/16f))
                .withEntry(new Vector3f(11f/16f,14f/16f,6f/16f))
                .withEntry(new Vector3f(5f/16f,7f/16f,6f/16f))
                .withEntry(new Vector3f(11f/16f,7f/16f,6f/16f)),
                ModBlocks.FREEZER
        );
        //Shelf
        this.addDataWithBlocks(VersionUtil.lcResource("shelf"), ItemPositionBuilder.builder()
                .withGlobalScale(14f/16f)
                .withGlobalRotationType(RotationHandler.FACING)
                .withEntry(new Vector3f(0.5f, 9f/16f,14.5f/16f)),
                LCTags.Blocks.SHELF
        );
        //Shelf 2x2
        this.addDataWithBlocks(VersionUtil.lcResource("shelf_2x2"), ItemPositionBuilder.builder()
                        .withGlobalScale(5.5f/16f)
                        .withGlobalRotationType(RotationHandler.FACING)
                        .withEntry(new Vector3f(0.25f, 13f/16f,14.5f/16f))
                        .withEntry(new Vector3f(0.75f, 13f/16f,14.5f/16f))
                        .withEntry(new Vector3f(0.25f, 5f/16f,14.5f/16f))
                        .withEntry(new Vector3f(0.75f, 5f/16f,14.5f/16f)),
                LCTags.Blocks.SHELF_2x2
        );
        //Vending Machine
        this.addDataWithBlocks(VersionUtil.lcResource("vending_machine"), ItemPositionBuilder.builder()
                .withGlobalScale(0.3f)
                .withGlobalRotationType(RotationHandler.FACING)
                .withGlobalExtraCount(2)
                .withGlobalExtraOffset(new Vector3f(MO,MO,0.2f))
                .withEntry(new Vector3f(3.5f/16f,27f/16f,6/16f))
                .withEntry(new Vector3f(9.5f/16f,27f/16f,6/16f))
                .withEntry(new Vector3f(3.5f/16f,20f/16f,6/16f))
                .withEntry(new Vector3f(9.5f/16f,20f/16f,6/16f))
                .withEntry(new Vector3f(3.5f/16f,13f/16f,6/16f))
                .withEntry(new Vector3f(9.5f/16f,13f/16f,6/16f)),
                ModBlocks.VENDING_MACHINE
        );
        //Large Vending Machine
        this.addDataWithBlocks(VersionUtil.lcResource("large_vending_machine"), ItemPositionBuilder.builder()
                .withGlobalScale(0.3f)
                .withGlobalRotationType(RotationHandler.FACING)
                .withGlobalExtraCount(2)
                .withGlobalExtraOffset(new Vector3f(MO,MO,0.2f))
                .withEntry(new Vector3f(3.5f/16f,27f/16f,6f/16f))
                .withEntry(new Vector3f(10.5f/16f,27f/16f,6f/16f))
                .withEntry(new Vector3f(17.5f/16f,27f/16f,6f/16f))
                .withEntry(new Vector3f(24.5f/16f,27f/16f,6f/16f))
                .withEntry(new Vector3f(3.5f/16f,20f/16f,6f/16f))
                .withEntry(new Vector3f(10.5f/16f,20f/16f,6f/16f))
                .withEntry(new Vector3f(17.5f/16f,20f/16f,6f/16f))
                .withEntry(new Vector3f(24.5f/16f,20f/16f,6f/16f))
                .withEntry(new Vector3f(3.5f/16f,13f/16f,6f/16f))
                .withEntry(new Vector3f(10.5f/16f,13f/16f,6f/16f))
                .withEntry(new Vector3f(17.5f/16f,13f/16f,6f/16f))
                .withEntry(new Vector3f(24.5f/16f,13f/16f,6f/16f)),
                ModBlocks.VENDING_MACHINE_LARGE
        );

        //Auction House
        this.addDataWithBlocks(VersionUtil.lcResource("auction_stand"), ItemPositionBuilder.builder()
                .withEntry(new Vector3f(0.5f,0.75f,0.5f), 0.4f, RotationHandler.SPINNING),
                LCTags.Blocks.AUCTION_STAND
        );

    }

}
