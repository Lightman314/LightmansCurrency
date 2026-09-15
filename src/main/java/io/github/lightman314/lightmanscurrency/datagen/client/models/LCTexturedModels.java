package io.github.lightman314.lightmanscurrency.datagen.client.models;

import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.WoodType;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IColoredBlock;
import io.github.lightman314.lightmanscurrency.api.helpers.ColorHelper;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IWoodenBlock;
import io.github.lightman314.lightmanscurrency.datagen.helpers.WoodHelper;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.block.Block;

public final class LCTexturedModels {
    private LCTexturedModels() {}

    public static final TexturedModel.Provider COIN_BLOCK = TexturedModel.createDefault(LCTexturedModels::mainMapping,LCModelTemplates.COIN_BLOCK);
    public static final TexturedModel.Provider COIN_PILE = TexturedModel.createDefault(LCTexturedModels::mainMapping,LCModelTemplates.COIN_PILE);
    public static final TexturedModel.Provider DISPLAY_CASE = TexturedModel.createDefault(LCTexturedModels::woolMapping,LCModelTemplates.DISPLAY_CASE);
    public static final TexturedModel.Provider CARD_DISPLAY = TexturedModel.createDefault(LCTexturedModels::woolLogPlankMapping,LCModelTemplates.CARD_DISPLAY);

    public static TextureMapping mainMapping(Block block) { return TextureMapping.singleSlot(LCTextureSlots.MAIN,TextureMapping.getBlockTexture(block)); }
    public static TextureMapping woolMapping(Block block) { return TextureMapping.singleSlot(LCTextureSlots.WOOL,getWoolTexture(block)); }
    public static TextureMapping woolLogPlankMapping(Block block) {
        return new TextureMapping()
                .put(LCTextureSlots.WOOL,getWoolTexture(block))
                .put(LCTextureSlots.PLANK,getPlankTexture(block))
                .put(LCTextureSlots.LOG,getLogSideTexture(block))
                .put(LCTextureSlots.LOG_TOP,getLogTopTexture(block));
    }

    public static Material getWoolTexture(Block block) {
        VanillaColor color = VanillaColor.WHITE;
        if(block instanceof IColoredBlock c)
            color = c.getColor();
        return TextureMapping.getBlockTexture(ColorHelper.getWoolBlock(color));
    }
    public static Material getLogSideTexture(Block block) {
        WoodType type = WoodType.OAK;
        if(block instanceof IWoodenBlock w)
            type = w.getWoodType();
        return new Material(WoodHelper.getLogSideTexture(type));
    }
    public static Material getLogTopTexture(Block block) {
        WoodType type = WoodType.OAK;
        if(block instanceof IWoodenBlock w)
            type = w.getWoodType();
        return new Material(WoodHelper.getLogTopTexture(type));
    }
    public static Material getPlankTexture(Block block) {
        WoodType type = WoodType.OAK;
        if(block instanceof IWoodenBlock w)
            type = w.getWoodType();
        return new Material(WoodHelper.getPlankTexture(type));
    }

}