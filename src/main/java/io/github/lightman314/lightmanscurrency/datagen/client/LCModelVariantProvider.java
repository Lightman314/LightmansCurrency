package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.UnbakedVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperties;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin.FreezerDoorData;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin.ItemPositionDataEntry;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin.ShowInCreative;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin.TooltipInfo;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.datagen.client.generators.ModelVariantProvider;
import io.github.lightman314.lightmanscurrency.datagen.util.ColorHelper;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodDataHelper;
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

        this.add(DISPLAY_CASE_GLASSLESS,UnbakedVariant.builder()
                .withName(LCText.BLOCK_VARIANT_DEFAULT.get())
                .withItem(VersionUtil.lcResource("block/display_case/glassless/base"))
                .withModel(VersionUtil.lcResource("block/display_case/glassless/base"))
                .withProperty(VariantProperties.ITEM_POSITION_DATA,ItemPositionDataEntry.create(VersionUtil.lcResource("display_case_open")))
                .withProperty(VariantProperties.TOOLTIP_INFO, TooltipInfo.ofModifier(LCText.BLOCK_VARIANT_MODIFIER_GLASSLESS))
                .asDummy()
                .build());


        ModBlocks.DISPLAY_CASE.forEach((color,block) ->
                this.add("display_case/glassless/" + color.getResourceSafeName(),
                        UnbakedVariant.builder()
                                .withParent(DISPLAY_CASE_GLASSLESS)
                                .withTarget(block)
                                .withTexture("wool", ColorHelper.GetWoolTextureOfColor(color))
                                .build())
        );

        //Glassless variant for the Armor Stand
        this.add("armor_display/glassless",
                UnbakedVariant.builder()
                        .withTarget(ModBlocks.ARMOR_DISPLAY)
                        .withName(LCText.BLOCK_VARIANT_DEFAULT.get())
                        .withItem(VersionUtil.lcResource("block/armor_display/glassless/item"))
                        .withModel(VersionUtil.lcResource("block/armor_display/glassless/bottom"))
                        .withModel(VersionUtil.lcResource("block/armor_display/glassless/top"))
                        .withProperty(VariantProperties.TOOLTIP_INFO,TooltipInfo.ofModifier(LCText.BLOCK_VARIANT_MODIFIER_GLASSLESS))
                        .build());

        //Skinned variants of the armor stand
        final ResourceLocation SKIN_DEFAULT = VersionUtil.lcResource("armor_display/skin_default");
        final ResourceLocation SKIN_GLASSLESS = VersionUtil.lcResource("armor_display/skin_glassless");
        this.add(SKIN_DEFAULT,
                UnbakedVariant.builder()
                        .withTarget(ModBlocks.ARMOR_DISPLAY)
                        .withItem(VersionUtil.lcResource("block/armor_display/skin/item"))
                        .withModel(VersionUtil.lcResource("block/armor_display/skin/bottom"),
                                VersionUtil.lcResource("block/armor_display/skin/top"))
                        .withProperty(VariantProperties.SHOW_IN_CREATIVE)
                        .asDummy()
                        .build());

        this.add(SKIN_GLASSLESS,
                UnbakedVariant.builder()
                        .withTarget(ModBlocks.ARMOR_DISPLAY)
                        .withItem(VersionUtil.lcResource("block/armor_display/skin/glassless/item"))
                        .withModel(VersionUtil.lcResource("block/armor_display/skin/glassless/bottom"),
                                VersionUtil.lcResource("block/armor_display/skin/glassless/top"))
                        .withProperty(VariantProperties.TOOLTIP_INFO,TooltipInfo.ofModifier(LCText.BLOCK_VARIANT_MODIFIER_GLASSLESS))
                        .asDummy()
                        .build());

        for(int i = 1; i <= 5; ++i)
        {
            Component name = LCText.BLOCK_VARIANT_ARMOR_SKIN.get(LCText.BLOCK_VARIANT_ARMOR_SKINS.get(i - 1).get());
            this.add("armor_display/skin/" + i + "/default",
                    UnbakedVariant.builder()
                            .withParent(SKIN_DEFAULT)
                            .withName(name)
                            .withTexture("skin",VersionUtil.lcResource("block/armor_display/skin" + i))
                            .build()
            );
            this.add("armor_display/skin/" + i + "/glassless",
                    UnbakedVariant.builder()
                            .withParent(SKIN_GLASSLESS)
                            .withName(name)
                            .withTexture("skin",VersionUtil.lcResource("block/armor_display/skin" + i))
                            .build()
            );
        }

        this.add("armor_display/skin/herobrine/default",
                UnbakedVariant.builder()
                        .withParent(SKIN_DEFAULT)
                        .withName(LCText.BLOCK_VARIANT_ARMOR_SKIN.get(LCText.BLOCK_VARIANT_ARMOR_SKIN_HEROBRINE.get()))
                        .withTexture("skin",VersionUtil.lcResource("block/armor_display/skin_herobrine"))
                        .withProperty(VariantProperties.HIDDEN)
                        .withProperty(VariantProperties.SHOW_IN_CREATIVE,ShowInCreative.LOCKED)
                        .build()
        );

        this.add("armor_display/skin/herobrine/glassless",
                UnbakedVariant.builder()
                        .withParent(SKIN_GLASSLESS)
                        .withName(LCText.BLOCK_VARIANT_ARMOR_SKIN.get(LCText.BLOCK_VARIANT_ARMOR_SKIN_HEROBRINE.get()))
                        .withTexture("skin",VersionUtil.lcResource("block/armor_display/skin_herobrine"))
                        .withProperty(VariantProperties.HIDDEN)
                        .withProperty(VariantProperties.SHOW_IN_CREATIVE,ShowInCreative.LOCKED)
                        .build()
        );

        final ResourceLocation FOOTLESS_VENDING_MACHINE = VersionUtil.lcResource("vending_machine/footless");
        this.add(FOOTLESS_VENDING_MACHINE,UnbakedVariant.builder()
                .withName(LCText.BLOCK_VARIANT_DEFAULT.get())
                .withItem(VersionUtil.lcResource("block/vending_machine/footless/base_item"))
                .withModel(VersionUtil.lcResource("block/vending_machine/footless/base_bottom"),
                        VersionUtil.lcResource("block/vending_machine/footless/base_top"))
                .withProperty(VariantProperties.TOOLTIP_INFO,TooltipInfo.ofModifier(LCText.BLOCK_VARIANT_MODIFIER_FOOTLESS))
                .asDummy()
                .build());

        //Footless variant for the Vending Machine
        ModBlocks.VENDING_MACHINE.forEach((color,block) ->
                this.add("vending_machine/footless/" + color.getResourceSafeName(),
                        UnbakedVariant.builder()
                                .withParent(FOOTLESS_VENDING_MACHINE)
                                .withTarget(block)
                                .withTexture("exterior",VersionUtil.lcResource("block/vending_machine/footless/" + color.getResourceSafeName() + "_exterior"))
                                .withTexture("interior",VersionUtil.lcResource("block/vending_machine/footless/" + color.getResourceSafeName() + "_interior"))
                                .build())
        );

        final ResourceLocation FOOTLESS_LARGE_VENDING_MACHINE = VersionUtil.lcResource("large_vending_machine/footless");
        this.add(FOOTLESS_LARGE_VENDING_MACHINE,UnbakedVariant.builder()
                .withName(LCText.BLOCK_VARIANT_DEFAULT.get())
                .withItem(VersionUtil.lcResource("block/large_vending_machine/footless/base_item"))
                .withModel(VersionUtil.lcResource("block/large_vending_machine/footless/base_bottom_left"),
                        VersionUtil.lcResource("block/large_vending_machine/footless/base_bottom_right"),
                        VersionUtil.lcResource("block/large_vending_machine/footless/base_top_left"),
                        VersionUtil.lcResource("block/large_vending_machine/footless/base_top_right"))
                .withProperty(VariantProperties.TOOLTIP_INFO,TooltipInfo.ofModifier(LCText.BLOCK_VARIANT_MODIFIER_FOOTLESS))
                .asDummy()
                .build());

        //Footless variant for the Large Vending Machine
        ModBlocks.VENDING_MACHINE_LARGE.forEach((color,block) ->
                this.add("large_vending_machine/footless/" + color.getResourceSafeName(),
                        UnbakedVariant.builder()
                                .withParent(FOOTLESS_LARGE_VENDING_MACHINE)
                                .withTarget(block)
                                .withTexture("exterior",VersionUtil.lcResource("block/large_vending_machine/footless/" + color.getResourceSafeName() + "_exterior"))
                                .withTexture("interior",VersionUtil.lcResource("block/large_vending_machine/footless/" + color.getResourceSafeName() + "_interior"))
                                .build())
        );

        final ResourceLocation FREEZER_INVERTED = VersionUtil.lcResource("freezer/inverted/base");
        //Inverted Freezer Door variants
        this.add(FREEZER_INVERTED,UnbakedVariant.builder()
                .withName(LCText.BLOCK_VARIANT_DEFAULT.get())
                .withModel(VersionUtil.lcResource("block/freezer/base_bottom")
                        ,VersionUtil.lcResource("block/freezer/base_top"),
                        VersionUtil.lcResource("block/freezer/inverted/door"))
                .withItem(VersionUtil.lcResource("block/freezer/inverted/item"))
                .withProperty(VariantProperties.FREEZER_DOOR_DATA,new FreezerDoorData(-90f,0.5f/16f,3.5f/16f))
                .withProperty(VariantProperties.TOOLTIP_INFO,TooltipInfo.ofModifier(LCText.BLOCK_VARIANT_MODIFIER_INVERTED))
                .asDummy()
                .build());

        ModBlocks.FREEZER.forEach((color,block) ->
                this.add("freezer/inverted/" + color.getResourceSafeName(),
                        UnbakedVariant.builder()
                                .withParent(FREEZER_INVERTED)
                                .withTarget(block)
                                .withTexture("concrete",ColorHelper.GetConcreteTextureOfColor(color))
                                .build())
        );

        //Card Display Variants
        ModBlocks.CARD_DISPLAY.forEachKey1((wood) -> {
            WoodData data = WoodDataHelper.get(wood);
            if(data != null)
            {
                createForCardDisplay(VersionUtil.lcResource("block/card_display/base_inner_corner"),LCText.BLOCK_VARIANT_INNER_CORNER.get(),VersionUtil.lcResource("variants/card_display/inner_corner"),"inner_corner");
                createForCardDisplay(VersionUtil.lcResource("block/card_display/base_outer_corner"),LCText.BLOCK_VARIANT_OUTER_CORNER.get(),VersionUtil.lcResource("variants/card_display/outer_corner"),"outer_corner");
            }
        });

        //Trading Core Variants for display use
        this.addItem("trading_core/alt1",LCText.BLOCK_VARIANT_ALT_NUMBERED.get(1), ModItems.TRADING_CORE,VersionUtil.lcResource("item/trading_core/1"));
        this.addItem("trading_core/alt2",LCText.BLOCK_VARIANT_ALT_NUMBERED.get(2), ModItems.TRADING_CORE,VersionUtil.lcResource("item/trading_core/2"));
        this.addItem("trading_core/alt3",LCText.BLOCK_VARIANT_ALT_NUMBERED.get(3), ModItems.TRADING_CORE,VersionUtil.lcResource("item/trading_core/3"));

        //Debug Examples
        /*
        this.add("example/property_example",
                UnbakedVariant.builder()
                        .withParent(VersionUtil.lcResource("display_case/glassless/white"))
                        .withName(EasyText.literal("Property Examples"))
                        .withSelectorTarget("lightmanscurrency:display_case*")
                        .withProperty(VariantProperties.INPUT_DISPLAY_OFFSET,new InputDisplayOffset(Map.of(Direction.UP, ScreenPosition.of(0,-16))))
                        .withProperty(VariantProperties.TOOLTIP_INFO,new TooltipInfo(Lists.newArrayList(EasyText.literal("Example Tooltip"))))
                        .withProperty(VariantProperties.ITEM_POSITION_DATA, ItemPositionDataEntry.create(ItemPositionBuilder.builder()
                                .withGlobalRotationType(RotationHandler.SPINNING)
                                .withSimpleEntry(new Vector3f(0.5f,1.5f,0.5f))))
                        .build());
        //*/

    }

    private void createForCardDisplay(ResourceLocation model,Component name,ResourceLocation itemPosition, String id)
    {
        ResourceLocation base = VersionUtil.lcResource("card_display/" + id + "/base");
        this.add(base,UnbakedVariant.builder()
                .withModel(model)
                .withItem(model)
                .withName(name)
                .withProperty(VariantProperties.ITEM_POSITION_DATA,ItemPositionDataEntry.create(itemPosition))
                .asDummy()
                .build());
        ModBlocks.CARD_DISPLAY.forEachKey1(wood -> {
            WoodData data = WoodDataHelper.get(wood);
            if(data != null)
                createCardDisplayChild(base,wood,data,id);
        });
    }

    private void createCardDisplayChild(ResourceLocation parent,WoodType woodType,WoodData data,String id)
    {
        ResourceLocation base = VersionUtil.lcResource(woodType.generateResourceLocation("card_display/" + id + "/","/base"));
        //Generate base variants
        this.add(base,UnbakedVariant.builder()
                .withParent(parent)
                .withTexture("log", data.logSideTexture)
                .withTexture("logtop", data.logTopTexture)
                .withTexture("plank", data.plankTexture)
                .asDummy()
                .build());
        for(Color color : Color.values())
        {
            this.add(woodType.generateResourceLocation("card_display/" + id + "/","/" + color.getResourceSafeName()),
                    UnbakedVariant.builder()
                            .withTarget(ModBlocks.CARD_DISPLAY.get(woodType,color))
                            .withParent(base)
                            .withTexture("wool",ColorHelper.GetWoolTextureOfColor(color))
                            .asOptional(woodType.isModded())
                            .build());
        }
    }

}