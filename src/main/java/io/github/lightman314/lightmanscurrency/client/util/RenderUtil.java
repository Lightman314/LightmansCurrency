package io.github.lightman314.lightmanscurrency.client.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;

public class RenderUtil extends AbstractGui{

    private static final RenderUtil INSTANCE = new RenderUtil();

    private RenderUtil() {}

    public static void bindTexture(ResourceLocation texture) { Minecraft.getInstance().getTextureManager().bind(texture); }

    @SuppressWarnings("deprecation")
    public static void color4f(float r, float g, float b, float a) { RenderSystem.color4f(r,g,b,a); }

    public static void renderSlotHighlight(MatrixStack pose, int x, int y, int blitOffset) { INSTANCE.renderSlotHighlightInternal(pose, x, y, blitOffset); }

    private void renderSlotHighlightInternal(MatrixStack pose, int x, int y, int blitOffset)
    {
        this.setBlitOffset(blitOffset);
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        int slotColor = -2130706433;
        this.fillGradient(pose, x, y, x + 16, y + 16, slotColor, slotColor);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
        this.setBlitOffset(0);
    }

}
