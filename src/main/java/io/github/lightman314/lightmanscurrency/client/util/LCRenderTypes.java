package io.github.lightman314.lightmanscurrency.client.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class LCRenderTypes extends RenderStateShard {

    public static final ResourceLocation BLANK_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/blank.png");

    private LCRenderTypes() { super(null, null, null); }

    private static final RenderType OUTLINE_TRANSLUCENT = RenderType.create(LightmansCurrency.MODID + ":" + "outline_translucent",
    DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(BLANK_TEXTURE, false, false))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false));

    public static RenderType getOutlineTranslucent() { return OUTLINE_TRANSLUCENT; }

}
