package io.github.lightman314.lightmanscurrency.client.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class LCRenderTypes extends RenderStateShard {

    public static final ResourceLocation AREA_TEXTURE = VersionUtil.vanillaResource("textures/misc/forcefield.png");

    private LCRenderTypes() { super(null, null, null); }

    private static final RenderType TAX_AREA = RenderType.create(LightmansCurrency.MODID + ":tax_area",
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 16, false, true, RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(AREA_TEXTURE, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static RenderType getTaxArea() { return TAX_AREA; }

}