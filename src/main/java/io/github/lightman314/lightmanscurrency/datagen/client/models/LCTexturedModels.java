package io.github.lightman314.lightmanscurrency.datagen.client.models;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.WoodType;
import io.github.lightman314.lightmanscurrency.api.helpers.ColorHelper;
import io.github.lightman314.lightmanscurrency.datagen.client.models.multi.TallModelTemplate;
import io.github.lightman314.lightmanscurrency.datagen.client.models.multi.TallWideModelTemplate;
import io.github.lightman314.lightmanscurrency.datagen.helpers.WoodHelper;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public final class LCTexturedModels {
    private LCTexturedModels() {}

    public static WoodType currentWood = WoodType.OAK;
    public static VanillaColor currentColor = VanillaColor.WHITE;
    public static Identifier currentDirectory = LCApi.id("null");

    public static final TexturedModel.Provider COIN_BLOCK = TexturedModel.createDefault(LCTexturedModels::mainMapping,LCModelTemplates.COIN_BLOCK);
    public static final TexturedModel.Provider COIN_PILE = TexturedModel.createDefault(LCTexturedModels::mainMapping,LCModelTemplates.COIN_PILE);
    public static final TexturedModel.Provider DISPLAY_CASE = TexturedModel.createDefault(LCTexturedModels::woolMapping,LCModelTemplates.DISPLAY_CASE);
    public static final TexturedModel.Provider SINGLE_SHELF = TexturedModel.createDefault(LCTexturedModels::plankMapping,LCModelTemplates.SINGLE_SHELF);
    public static final TexturedModel.Provider DOUBLE_SHELF = TexturedModel.createDefault(LCTexturedModels::plankMapping,LCModelTemplates.DOUBLE_SHELF);
    public static final TexturedModel.Provider CARD_DISPLAY = TexturedModel.createDefault(LCTexturedModels::woolLogPlankMapping,LCModelTemplates.CARD_DISPLAY);

    public static final TallModelTemplate.TexturedModelProvider VENDING_MACHINE = LCModelTemplates.VENDING_MACHINE.asTexturedModelProvider(template -> TexturedModel.createDefault(LCTexturedModels::coloredInteriorExteriorMapping,template));

    public static final TallWideModelTemplate.TexturedModelProvider LARGE_VENDING_MACHINE = LCModelTemplates.LARGE_VENDING_MACHINE.asTexturedModelProvider(template -> TexturedModel.createDefault(LCTexturedModels::coloredInteriorExteriorMapping,template));

    public static TextureMapping mainMapping(Block block) { return TextureMapping.singleSlot(LCTextureSlots.MAIN,TextureMapping.getBlockTexture(block)); }
    public static TextureMapping woolMapping(Block block) { return TextureMapping.singleSlot(LCTextureSlots.WOOL,getWoolTexture(block)); }
    public static TextureMapping plankMapping(Block block) { return TextureMapping.singleSlot(LCTextureSlots.PLANK,getPlankTexture(block)); }

    public static TextureMapping woolLogPlankMapping(Block block) {
        return new TextureMapping()
                .put(LCTextureSlots.WOOL,getWoolTexture(block))
                .put(LCTextureSlots.PLANK,getPlankTexture(block))
                .put(LCTextureSlots.LOG,getLogSideTexture(block))
                .put(LCTextureSlots.LOG_TOP,getLogTopTexture(block));
    }

    public static TextureMapping coloredInteriorExteriorMapping(Block block) {
        return new TextureMapping()
                .put(LCTextureSlots.INTERIOR,getColoredInteriorTexture(block))
                .put(LCTextureSlots.EXTERIOR,getColoredExteriorTexture(block));
    }

    public static Material getWoolTexture(Block block) {
        return TextureMapping.getBlockTexture(ColorHelper.getWoolBlock(currentColor));
    }
    public static Material getLogSideTexture(Block block) {
        return new Material(WoodHelper.getLogSideTexture(currentWood));
    }
    public static Material getLogTopTexture(Block block) {
        return new Material(WoodHelper.getLogTopTexture(currentWood));
    }
    public static Material getPlankTexture(Block block) {
        return new Material(WoodHelper.getPlankTexture(currentWood));
    }

    public static Material getColoredInteriorTexture(Block block) {
        return new Material(currentDirectory.withPrefix("block/").withSuffix("/" + currentColor.getResourceSafeName() + "_interior"));
    }
    public static Material getColoredExteriorTexture(Block block) {
        return new Material(currentDirectory.withPrefix("block/").withSuffix("/" + currentColor.getResourceSafeName() + "_exterior"));
    }

}