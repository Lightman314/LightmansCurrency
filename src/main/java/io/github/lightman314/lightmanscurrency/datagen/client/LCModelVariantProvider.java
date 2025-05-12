package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariant;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.datagen.client.generators.ModelVariantProvider;
import io.github.lightman314.lightmanscurrency.datagen.util.ColorHelper;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LCModelVariantProvider extends ModelVariantProvider {

    public LCModelVariantProvider(PackOutput output) { super(output,LightmansCurrency.MODID); }

    @Override
    protected void addEntries() {

        //Glassless variant for the Display Case
        final ResourceLocation DISPLAY_CASE_GLASSLESS = VersionUtil.lcResource("display_case/glassless");

        this.add(DISPLAY_CASE_GLASSLESS,ModelVariant.builder()
                .withName(LCText.BLOCK_VARIANT_GLASSLESS.get())
                .withItem(VersionUtil.lcResource("block/display_case/glassless/base"))
                .withModel(VersionUtil.lcResource("block/display_case/glassless/base"))
                .asDummy()
                .build());


        ModBlocks.DISPLAY_CASE.forEach((color,block) ->
                this.add("display_case/glassless/" + color.getResourceSafeName(),
                        ModelVariant.builder()
                                .withParent(DISPLAY_CASE_GLASSLESS)
                                .withTarget(block)
                                .withTexture("wool", ColorHelper.GetWoolTextureOfColor(color))
                                .build())
        );

        //Glassless variant for the Armor Stand
        this.add("armor_display/glassless",
                ModelVariant.builder()
                        .withTarget(ModBlocks.ARMOR_DISPLAY)
                        .withName(LCText.BLOCK_VARIANT_GLASSLESS.get())
                        .withItem(VersionUtil.lcResource("block/armor_display/glassless/item"))
                        .withModel(VersionUtil.lcResource("block/armor_display/glassless/bottom"))
                        .withModel(VersionUtil.lcResource("block/armor_display/glassless/top"))
                        .build());

        //Skinned variants of the armor stand
        final ResourceLocation SKIN_DEFAULT = VersionUtil.lcResource("armor_display/skin_default");
        final ResourceLocation SKIN_GLASSLESS = VersionUtil.lcResource("armor_display/skin_glassless");
        this.add(SKIN_DEFAULT,
                ModelVariant.builder()
                        .withTarget(ModBlocks.ARMOR_DISPLAY)
                        .withItem(VersionUtil.lcResource("block/armor_display/skin/item"))
                        .withModel(VersionUtil.lcResource("block/armor_display/skin/bottom"),
                                VersionUtil.lcResource("block/armor_display/skin/top"))
                        .asDummy()
                        .build());

        this.add(SKIN_GLASSLESS,
                ModelVariant.builder()
                        .withTarget(ModBlocks.ARMOR_DISPLAY)
                        .withItem(VersionUtil.lcResource("block/armor_display/skin/glassless/item"))
                        .withModel(VersionUtil.lcResource("block/armor_display/skin/glassless/bottom"),
                                VersionUtil.lcResource("block/armor_display/skin/glassless/top"))
                        .asDummy()
                        .build());

        for(int i = 1; i <= 5; ++i)
        {
            Component name = LCText.BLOCK_VARIANT_ARMOR_SKINS.get(i - 1).get();
            this.add("armor_display/skin/" + i + "/default",
                    ModelVariant.builder()
                            .withParent(SKIN_DEFAULT)
                            .withName(LCText.BLOCK_VARIANT_ARMOR_SKIN.get(name))
                            .withTexture("skin",VersionUtil.lcResource("block/armor_display/skin" + i))
                            .build()
            );
            this.add("armor_display/skin/" + i + "/glassless",
                    ModelVariant.builder()
                            .withParent(SKIN_GLASSLESS)
                            .withName(LCText.BLOCK_VARIANT_ARMOR_GLASSLESS_SKIN.get(name))
                            .withTexture("skin",VersionUtil.lcResource("block/armor_display/skin" + i))
                            .build()
            );
        }

        final ResourceLocation FOOTLESS_VENDING_MACHINE = VersionUtil.lcResource("vending_machine/footless");
        this.add(FOOTLESS_VENDING_MACHINE,ModelVariant.builder()
                .withName(LCText.BLOCK_VARIANT_VENDING_MACHINE_FOOTLESS.get())
                .withItem(VersionUtil.lcResource("block/vending_machine/footless/base_item"))
                .withModel(VersionUtil.lcResource("block/vending_machine/footless/base_bottom"),
                        VersionUtil.lcResource("block/vending_machine/footless/base_top"))
                .asDummy()
                .build());

        //Footless variant for the Vending Machine
        ModBlocks.VENDING_MACHINE.forEach((color,block) ->
                this.add("vending_machine/footless/" + color.getResourceSafeName(),
                        ModelVariant.builder()
                                .withParent(FOOTLESS_VENDING_MACHINE)
                                .withTarget(block)
                                .withTexture("exterior",VersionUtil.lcResource("block/vending_machine/footless/" + color.getResourceSafeName() + "_exterior"))
                                .withTexture("interior",VersionUtil.lcResource("block/vending_machine/footless/" + color.getResourceSafeName() + "_interior"))
                                .build())
        );

        final ResourceLocation FOOTLESS_LARGE_VENDING_MACHINE = VersionUtil.lcResource("large_vending_machine/footless");
        this.add(FOOTLESS_LARGE_VENDING_MACHINE,ModelVariant.builder()
                .withName(LCText.BLOCK_VARIANT_VENDING_MACHINE_FOOTLESS.get())
                .withItem(VersionUtil.lcResource("block/large_vending_machine/footless/base_item"))
                .withModel(VersionUtil.lcResource("block/large_vending_machine/footless/base_bottom_left"),
                        VersionUtil.lcResource("block/large_vending_machine/footless/base_bottom_right"),
                        VersionUtil.lcResource("block/large_vending_machine/footless/base_top_left"),
                        VersionUtil.lcResource("block/large_vending_machine/footless/base_top_right"))
                .asDummy()
                .build());

        //Footless variant for the Large Vending Machine
        ModBlocks.VENDING_MACHINE_LARGE.forEach((color,block) ->
                this.add("large_vending_machine/footless/" + color.getResourceSafeName(),
                        ModelVariant.builder()
                                .withParent(FOOTLESS_LARGE_VENDING_MACHINE)
                                .withTarget(block)
                                .withTexture("exterior",VersionUtil.lcResource("block/large_vending_machine/footless/" + color.getResourceSafeName() + "_exterior"))
                                .withTexture("interior",VersionUtil.lcResource("block/large_vending_machine/footless/" + color.getResourceSafeName() + "_interior"))
                                .build())
        );

    }

}