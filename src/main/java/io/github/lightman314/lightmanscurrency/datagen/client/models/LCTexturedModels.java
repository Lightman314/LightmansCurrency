package io.github.lightman314.lightmanscurrency.datagen.client.models;

import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IColoredBlock;
import io.github.lightman314.lightmanscurrency.api.helpers.ColorHelper;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

public final class LCTexturedModels {
    private LCTexturedModels() {}

    public static final TexturedModel.Provider COIN_BLOCK = TexturedModel.createDefault(LCTexturedModels::mainMapping,LCModelTemplates.COIN_BLOCK);
    public static final TexturedModel.Provider COIN_PILE = TexturedModel.createDefault(LCTexturedModels::mainMapping,LCModelTemplates.COIN_PILE);
    public static final TexturedModel.Provider DISPLAY_CASE = TexturedModel.createDefault(LCTexturedModels::woolMapping,LCModelTemplates.DISPLAY_CASE);

    public static TextureMapping mainMapping(Block block) { return TextureMapping.singleSlot(LCTextureSlots.MAIN,TextureMapping.getBlockTexture(block)); }
    public static TextureMapping woolMapping(Block block) { return TextureMapping.singleSlot(LCTextureSlots.WOOL,getWoolTexture(block)); }

    public static Material getWoolTexture(Block block) {
        DyeColor color = DyeColor.WHITE;
        if(block instanceof IColoredBlock c)
            color = c.getColor();
        return TextureMapping.getBlockTexture(ColorHelper.getWoolBlock(color));
    }

}