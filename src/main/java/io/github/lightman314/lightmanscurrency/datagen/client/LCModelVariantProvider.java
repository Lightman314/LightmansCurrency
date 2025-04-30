package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariant;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.datagen.client.generators.ModelVariantProvider;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class LCModelVariantProvider extends ModelVariantProvider {

    public LCModelVariantProvider(PackOutput output) { super(output,LightmansCurrency.MODID); }

    @Override
    protected void addEntries() {

        //Glassless variant for the Display Case
        ModBlocks.DISPLAY_CASE.forEach((color,block) -> {
            ResourceLocation model = VersionUtil.lcResource("block/display_case/glassless/" + color.getResourceSafeName());
            this.add("display_case/glassless/" + color.getResourceSafeName(),
                    ModelVariant.builder()
                            .withTarget(block)
                            .withName(LCText.BLOCK_VARIANT_GLASSLESS.get())
                            .withItem(model)
                            .withModel(model)
                            .build());
        });

        //Glassless variant for the Armor Stand
        this.add("armor_display/glassless",
                ModelVariant.builder()
                        .withTarget(ModBlocks.ARMOR_DISPLAY)
                        .withName(LCText.BLOCK_VARIANT_GLASSLESS.get())
                        .withItem(VersionUtil.lcResource("block/armor_display/glassless/item"))
                        .withModel(VersionUtil.lcResource("block/armor_display/glassless/bottom"))
                        .withModel(VersionUtil.lcResource("block/armor_display/glassless/top"))
                        .build());

        for(int i = 1; i <= 5; ++i)
        {
            this.add("armor_display/skin/" + i + "/default",
                    ModelVariant.builder()
                            .withTarget(ModBlocks.ARMOR_DISPLAY)
                            .withName(EasyText.translatable("lightmanscurrency.block_variant.armor_display.skin." + i))
                            .withItem(VersionUtil.lcResource("block/armor_display/skin/" + i + "/item"))
                            .withModel(VersionUtil.lcResource("block/armor_display/skin/" + i + "/bottom"))
                            .withModel(VersionUtil.lcResource("block/armor_display/skin/" + i + "/top"))
                            .build()
            );
            this.add("armor_display/skin/" + i + "/glassless",
                    ModelVariant.builder()
                            .withTarget(ModBlocks.ARMOR_DISPLAY)
                            .withName(EasyText.translatable("lightmanscurrency.block_variant.armor_display.skin." + i + ".glassless"))
                            .withItem(VersionUtil.lcResource("block/armor_display/skin/" + i + "/glassless/item"))
                            .withModel(VersionUtil.lcResource("block/armor_display/skin/" + i + "/glassless/bottom"))
                            .withModel(VersionUtil.lcResource("block/armor_display/skin/" + i + "/glassless/top"))
                            .build()
            );
        }

        //Footless variant for the Vending Machine
        ModBlocks.VENDING_MACHINE.forEach((color,block) ->
            this.add("vending_machine/footless/" + color.getResourceSafeName(),
                    ModelVariant.builder()
                            .withTarget(block)
                            .withName(LCText.BLOCK_VARIANT_VENDING_MACHINE_FOOTLESS.get())
                            .withItem(VersionUtil.lcResource("block/vending_machine/footless/" + color.getResourceSafeName() + "_item"))
                            .withModel(VersionUtil.lcResource("block/vending_machine/footless/" + color.getResourceSafeName() + "_bottom"))
                            .withModel(VersionUtil.lcResource("block/vending_machine/footless/" + color.getResourceSafeName() + "_top"))
                            .build())
        );

        //Footless variant for the Large Vending Machine
        ModBlocks.VENDING_MACHINE_LARGE.forEach((color,block) ->
            this.add("large_vending_machine/footless/" + color.getResourceSafeName(),
                    ModelVariant.builder()
                            .withTarget(block)
                            .withName(LCText.BLOCK_VARIANT_VENDING_MACHINE_FOOTLESS.get())
                            .withItem(VersionUtil.lcResource("block/large_vending_machine/footless/" + color.getResourceSafeName() + "_item"))
                            .withModel(VersionUtil.lcResource("block/large_vending_machine/footless/" + color.getResourceSafeName() + "_bottom_left"))
                            .withModel(VersionUtil.lcResource("block/large_vending_machine/footless/" + color.getResourceSafeName() + "_bottom_right"))
                            .withModel(VersionUtil.lcResource("block/large_vending_machine/footless/" + color.getResourceSafeName() + "_top_left"))
                            .withModel(VersionUtil.lcResource("block/large_vending_machine/footless/" + color.getResourceSafeName() + "_top_right"))
                            .build())
        );

        //Debug Texture override variant
        /*
        this.add("debug/red_vending_machine",ModelVariant.builder()
                .withTarget(ModBlocks.VENDING_MACHINE.get(Color.WHITE))
                .withName(EasyText.literal("Red Vending Machine (Debug)"))
                .withItem(VersionUtil.lcResource("block/vending_machine/white_item"))
                //.withModel(VersionUtil.lcResource("block/vending_machine/white_bottom"),
                //        VersionUtil.lcResource("block/vending_machine/white_top"))
                .withTexture("particle",VersionUtil.lcResource("block/vending_machine/red_exterior"))
                .withTexture("exterior",VersionUtil.lcResource("block/vending_machine/red_exterior"))
                .withTexture("interior",VersionUtil.lcResource("block/vending_machine/red_interior"))
                .build()
        );//*/

    }

}
