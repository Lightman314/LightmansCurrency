package io.github.lightman314.lightmanscurrency.api.misc.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.OutlineUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.ScreenEvent;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class EasyGuiGraphics {

    public static final ResourceLocation GENERIC_BACKGROUND = new ResourceLocation(LightmansCurrency.MODID,"textures/gui/generic_background.png");

    private final GuiGraphics gui;
    public GuiGraphics getGui() { return this.gui; }
    public final Font font;
    public PoseStack getPose() { return this.gui.pose(); }

    public final ScreenPosition mousePos;
    public final float partialTicks;

    private final List<ScreenPosition> offsetStack = new ArrayList<>();
    private ScreenPosition offset = ScreenPosition.ZERO;
    //Made public because it's helpful information to have when rendering something complicated like Fluids, etc.
    @Nonnull
    public ScreenPosition getOffset() { return this.offset; }

    public EasyGuiGraphics pushOffsetZero() { return this.pushOffset(ScreenPosition.ZERO); }
    public EasyGuiGraphics pushOffset(@Nonnull ScreenPosition offset) { this.offsetStack.add(0, offset); return this.refactorOffset(); }
    public EasyGuiGraphics pushOffset(@Nonnull AbstractWidget widget) { this.offsetStack.add(0, ScreenPosition.of(widget.getX(), widget.getY())); return this.refactorOffset(); }
    public EasyGuiGraphics popOffset() { if(!this.offsetStack.isEmpty()) this.offsetStack.remove(0); return this.refactorOffset(); }
    private EasyGuiGraphics refactorOffset() { this.offset = !this.offsetStack.isEmpty() ? this.offsetStack.get(0) : ScreenPosition.ZERO; return this; }

    private EasyGuiGraphics(@Nonnull GuiGraphics gui, Font font, int mouseX, int mouseY, float partialTicks) { this.gui = gui; this.font = font; this.mousePos = ScreenPosition.of(mouseX, mouseY); this.partialTicks = partialTicks; }
    public static EasyGuiGraphics create(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTicks) { return create(gui, Minecraft.getInstance().font, mouseX, mouseY, partialTicks); }
    public static EasyGuiGraphics create(@Nonnull GuiGraphics gui, Font font, int mouseX, int mouseY, float partialTicks) { return new EasyGuiGraphics(gui, font, mouseX, mouseY, partialTicks); }
    public static EasyGuiGraphics create(@Nonnull ScreenEvent.Render event) { return new EasyGuiGraphics(event.getGuiGraphics(), event.getScreen().getMinecraft().font, event.getMouseX(), event.getMouseY(), event.getPartialTick()); }
    public static EasyGuiGraphics create(@Nonnull ContainerScreenEvent.Render event) { return new EasyGuiGraphics(event.getGuiGraphics(), event.getContainerScreen().getMinecraft().font, event.getMouseX(), event.getMouseY(), 0f); }

    //Color Rendering
    public void setColor(float r, float g, float b) { this.setColor(r,g,b,1f); }
    public void setColor(float r, float g, float b, float a) { this.gui.setColor(r,g,b,a); }
    public void setColor(int color) { this.setColor(OutlineUtil.decodeColor(color)); }
    public void setColor(int color, float alpha) { this.setColor(OutlineUtil.decodeColor(color,alpha)); }
    public void setColor(@Nonnull Vector4f color) { this.gui.setColor(color.x,color.y,color.z,color.w); }
    public void resetColor() { this.setColor(1f,1f,1f,1f); }


    //Texture Rendering
    public void renderNormalBackground(@Nonnull ResourceLocation image, @Nonnull IEasyScreen screen) { this.resetColor(); this.pushOffset(screen.getCorner()).blit(image, 0,0,0,0, screen.getXSize(), screen.getYSize()); this.popOffset(); }
    public void renderNormalBackground(@Nonnull IEasyScreen screen)
    {
        this.resetColor();
        this.pushOffset(screen.getCorner());
        this.blitBackgroundOfSize(GENERIC_BACKGROUND,0,0,screen.getXSize(),screen.getYSize(),0,0,256,256,16);
        this.popOffset();
    }
    public void renderSlot(@Nonnull IEasyScreen screen, @Nonnull Slot slot) { this.renderSlot(screen,ScreenPosition.of(slot.x,slot.y)); }
    public void renderSlot(@Nonnull IEasyScreen screen, @Nonnull ScreenPosition position)
    {
        this.resetColor();
        this.pushOffset(screen.getCorner());
        this.blit(IconAndButtonUtil.WIDGET_TEXTURE,position.offset(-1,-1),0, 128,18,18);
        this.popOffset();
    }
    public void blit(@Nonnull ResourceLocation image, int x, int y, int u, int v, int width, int height) { this.gui.blit(image, this.offset.x + x, this.offset.y + y, u, v, width, height); }
    public void blit(@Nonnull ResourceLocation image, @Nonnull ScreenPosition pos, int u, int v, int width, int height) { this.blit(image, pos.x, pos.y, u, v, width, height); }
    public void blit(@Nonnull ResourceLocation image, @Nonnull ScreenArea area, int u, int v) { this.blit(image, area.pos.x, area.pos.y, u, v, area.width, area.height); }
    public void blitSprite(@Nonnull Sprite sprite, int x, int y) { this.blitSprite(sprite, x, y, false); }
    public void blitSprite(@Nonnull Sprite sprite, @Nonnull ScreenPosition pos) { this.blitSprite(sprite, pos.x, pos.y); }
    public void blitSprite(@Nonnull Sprite sprite, int x, int y, boolean hovered) { this.blit(sprite.image, x, y, sprite.getU(hovered), sprite.getV(hovered), sprite.width, sprite.height); }
    public void blitSprite(@Nonnull Sprite sprite, @Nonnull ScreenPosition pos, boolean hovered) { this.blitSprite(sprite, pos.x, pos.y, hovered); }

    public void blitSpriteFadeHoriz(@Nonnull Sprite sprite, int x, int y, float percent) { this.blitSpriteFadeHoriz(sprite, x, y, percent, false); }
    public void blitSpriteFadeHoriz(@Nonnull Sprite sprite, @Nonnull ScreenPosition pos, float percent) { this.blitSpriteFadeHoriz(sprite, pos.x, pos.y, percent); }
    public void blitSpriteFadeHoriz(@Nonnull Sprite sprite, int x, int y, float percent, boolean hovered) {
        int blitWidth = MathUtil.clamp((int)((sprite.width + 1) * percent), 0, sprite.width);
        this.blit(sprite.image, x, y, sprite.getU(hovered), sprite.getV(hovered), blitWidth, sprite.height);
    }
    public void blitSpriteFadeHoriz(@Nonnull Sprite sprite, @Nonnull ScreenPosition pos, float percent, boolean hovered) { this.blitSpriteFadeHoriz(sprite, pos.x, pos.y, percent, hovered); }

    public void blitBackgroundOfSize(@Nonnull ResourceLocation image, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, int edge)
    {
        int uCenter = uWidth - edge - edge;
        if(uCenter < 1)
            throw new IllegalArgumentException("Invalid inputs resulted in uCenter of " + uCenter);
        int vCenter = vHeight - edge - edge;
        if(vCenter < 1)
            throw new IllegalArgumentException("Invalid inputs resulted in vCenter of " + vCenter);

        //Top-left corner
        this.blit(image, x, y, u, v, edge, edge);
        //Top edge
        int tempX = edge;
        while(tempX < width - edge)
        {
            int widthToDraw = Math.min(uCenter, width - edge - tempX);
            this.blit(image, x + tempX, y, u + edge, v, widthToDraw, edge);
            tempX += widthToDraw;
        }
        //Top-right corner
        this.blit(image, x + width - edge, y, u + uWidth - edge, v, edge, edge);

        //Draw center
        int tempY = edge;
        while(tempY < height - edge)
        {
            int heightToDraw = Math.min(vCenter, height - edge - tempY);
            //Left center
            this.blit(image, x, y + tempY, u, v + edge, edge, heightToDraw);
            tempX = edge;
            //Center
            while(tempX < width - edge)
            {
                int widthToDraw = Math.min(uCenter, width - edge - tempX);
                this.blit(image, x + tempX, y + tempY, u + edge, v + edge, widthToDraw, heightToDraw);
                tempX += widthToDraw;
            }
            //Right center
            this.blit(image, x + width - edge, y + tempY, u + uWidth - edge, v + edge, edge, heightToDraw);
            tempY += heightToDraw;
        }

        //Bottom-left corner
        this.blit(image, x, y + height - edge, u, v + vHeight - edge, edge, edge);
        //Bottom edge
        tempX = edge;
        while(tempX < width - edge)
        {
            int widthToDraw = Math.min(uCenter, width - edge - tempX);
            this.blit(image, x + tempX, y + height - edge, u + edge, v + vHeight - edge, widthToDraw, edge);
            tempX += widthToDraw;
        }
        //Bottom-right corner
        this.blit(image, x + width - edge, y + height - edge, u + uWidth - edge, v + vHeight - edge, edge, edge);
    }

    public void renderButtonBG(int x, int y, int width, int height, float alpha, int textureY) { this.renderButtonBG(x,y,width,height,alpha,textureY,0xFFFFFF); }
    public void renderButtonBG(int x, int y, int width, int height, float alpha, int textureY, int color)
    {
        Minecraft minecraft = Minecraft.getInstance();
        this.setColor(color, alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        this.gui.blitNineSliced(AbstractWidget.WIDGETS_LOCATION, this.offset.x + x, this.offset.y + y, width, height, 20, 4, 200, 20, 0, textureY);
        this.resetColor();
    }

    public void fill(int x, int y, int width, int height, int color) { this.gui.fill(this.offset.x + x, this.offset.y + y, this.offset.x + x + width, this.offset.y + y + height, color); }
    public void fill(ScreenPosition pos, int width, int height, int color) { this.fill(pos.x, pos.y, width, height, color); }
    public void fill(ScreenArea area, int color) { this.fill(area.x, area.y, area.width, area.height, color); }

    //Tooltip Related Rendering
    public void renderTooltip(@Nonnull Component tooltip) { this.pushOffset(this.mousePos).renderTooltip(tooltip, 0, 0); this.popOffset(); }
    public void renderTooltip(@Nonnull Component tooltip, int x, int y) { this.gui.renderTooltip(this.font, tooltip, this.offset.x + x, this.offset.y + y); }
    public void renderComponentTooltip(@Nonnull List<Component> tooltip) { this.pushOffset(this.mousePos).renderComponentTooltip(tooltip, 0, 0); this.popOffset(); }
    public void renderComponentTooltip(@Nonnull List<Component> tooltip, int x, int y) { if(tooltip.isEmpty()) return; this.gui.renderComponentTooltip(this.font, tooltip, this.offset.x + x, this.offset.y + y); }
    public void renderTooltip(@Nonnull List<FormattedCharSequence> tooltip) { this.pushOffset(this.mousePos).renderTooltip(tooltip, 0,0); this.popOffset(); }
    public void renderTooltip(@Nonnull List<FormattedCharSequence> tooltip, int x, int y) { if(tooltip.isEmpty()) return; this.gui.renderTooltip(this.font, tooltip, this.offset.x + x, this.offset.y + y); }
    public void renderTooltip(@Nonnull ItemStack item) { this.pushOffset(this.mousePos).renderTooltip(item, 0,0); this.popOffset(); }
    public void renderTooltip(@Nonnull ItemStack item, int x, int y) { this.gui.renderTooltip(this.font, item, x, y); }

    //Text Related Rendering
    public void drawString(String text, int x, int y, int color) { this.gui.drawString(this.font, text, this.offset.x + x, this.offset.y + y, color, false); }
    public void drawString(String text, ScreenPosition pos, int color) { this.drawString(text, pos.x, pos.y, color); }
    public void drawString(Component text, int x, int y, int color) { this.gui.drawString(this.font, text, this.offset.x + x, this.offset.y + y, color, false); }
    public void drawString(Component text, ScreenPosition pos, int color) { this.drawString(text, pos.x, pos.y, color); }
    public void drawString(FormattedCharSequence text, int x, int y, int color) { this.gui.drawString(this.font, text, this.offset.x + x, this.offset.y + y, color, false); }
    public void drawString(FormattedCharSequence text, ScreenPosition pos, int color) { this.drawString(text, pos.x, pos.y, color); }
    public void drawShadowed(String text, int x, int y, int color) { this.gui.drawString(this.font, text, this.offset.x + x, this.offset.y + y, color, true); }
    public void drawShadowed(String text, ScreenPosition pos, int color) { this.drawShadowed(text, pos.x, pos.y, color); }
    public void drawShadowed(Component text, int x, int y, int color) { this.gui.drawString(this.font, text, this.offset.x + x, this.offset.y + y, color, true); }
    public void drawShadowed(Component text, ScreenPosition pos, int color) { this.drawShadowed(text, pos.x, pos.y, color); }
    public void drawShadowed(FormattedCharSequence text, int x, int y, int color) { this.gui.drawString(this.font, text, this.offset.x + x, this.offset.y + y, color, true); }
    public void drawShadowed(FormattedCharSequence text, ScreenPosition pos, int color) { this.drawShadowed(text, pos.x, pos.y, color); }

    public void drawWordWrap(String text, int x, int y, int columnWidth, int color) { this.gui.drawWordWrap(this.font, EasyText.literal(text), this.offset.x + x, this.offset.y + y, columnWidth, color); }
    public void drawWordWrap(Component text, int x, int y, int columnWidth, int color) { this.gui.drawWordWrap(this.font, text, this.offset.x + x, this.offset.y + y, columnWidth, color); }

    //Item Related Rendering
    public void renderItem(@Nonnull ItemStack item, int x, int y) { this.renderItem(item, x, y, null); }
    public void renderItem(@Nonnull ItemStack item, @Nonnull ScreenPosition pos) { this.renderItem(item, pos.x, pos.y); }
    public void renderItem(@Nonnull ItemStack item, @Nonnull ScreenPosition pos, @Nullable String countTextOverride) { this.renderItem(item, pos.x, pos.y, countTextOverride); }
    public void renderItem(@Nonnull ItemStack item, int x, int y, @Nullable String countTextOverride) {
        this.resetColor();
        this.gui.renderItem(item, this.offset.x + x, this.offset.y + y);
        this.gui.renderItemDecorations(this.font, item, this.offset.x + x, this.offset.y + y, countTextOverride);
    }

    public void renderSlotBackground(Pair<ResourceLocation,ResourceLocation> background, @Nonnull ScreenPosition pos) { this.renderSlotBackground(background, pos.x, pos.y); }
    public void renderSlotBackground(Pair<ResourceLocation,ResourceLocation> background, int x, int y) {
        if(background == null)
            return;
        TextureAtlasSprite textureatlassprite = Minecraft.getInstance().getTextureAtlas(background.getFirst()).apply(background.getSecond());
        this.gui.blit(this.offset.x + x, this.offset.y + y, 0, 16, 16, textureatlassprite);
    }

    public void renderSlotHighlight(int x, int y) { this.gui.fillGradient(RenderType.guiOverlay(), this.offset.x + x, this.offset.y + y, this.offset.x + x + 16, this.offset.y + y + 16, -2130706433, -2130706433, 0); }
    public void renderSlotHighlight(@Nonnull ScreenPosition pos) { this.renderSlotHighlight(pos.x, pos.y); }

    //PoseStack interactions
    public EasyGuiGraphics pushPose() { this.gui.pose().pushPose(); return this; }
    public void TranslateToForeground() { this.gui.pose().translate(0d,0d,0250d); }
    public void popPose() { this.gui.pose().popPose(); }


}
