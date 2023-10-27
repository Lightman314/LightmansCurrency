package io.github.lightman314.lightmanscurrency.client.gui.easy.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreenHelper;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class EasyGuiGraphics extends GuiComponent {

    public final Font font;
    private final PoseStack pose;
    public PoseStack getPose() { return this.pose; }
    private final LazyOptional<Screen> screen;

    public final ScreenPosition mousePos;
    public final float partialTicks;

    private final List<ScreenPosition> offsetStack = new ArrayList<>();
    private ScreenPosition offset = ScreenPosition.ZERO;

    public EasyGuiGraphics pushOffsetZero() { return this.pushOffset(ScreenPosition.ZERO); }
    public EasyGuiGraphics pushOffset(@Nonnull ScreenPosition offset) { this.offsetStack.add(0, offset); return this.refactorOffset(); }
    public EasyGuiGraphics pushOffset(@Nonnull AbstractWidget widget) { this.offsetStack.add(0, ScreenPosition.of(widget.x, widget.y)); return this.refactorOffset(); }
    public EasyGuiGraphics popOffset() { if(this.offsetStack.size() > 0) this.offsetStack.remove(0); return this.refactorOffset(); }
    private EasyGuiGraphics refactorOffset() { this.offset = this.offsetStack.size() > 0 ? this.offsetStack.get(0) : ScreenPosition.ZERO; return this; }

    private EasyGuiGraphics(@Nonnull PoseStack pose, Font font, int mouseX, int mouseY, float partialTicks)
    {
        this.pose = pose;
        this.font = font;
        this.mousePos = ScreenPosition.of(mouseX, mouseY);
        this.partialTicks = partialTicks;
        Minecraft mc = Minecraft.getInstance();
        if(mc != null && mc.screen != null)
        {
            Screen s = mc.screen;
            this.screen = LazyOptional.of(() -> s);
        }
        else
            this.screen = LazyOptional.empty();
    }
    public static EasyGuiGraphics create(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) { return create(pose, Minecraft.getInstance().font, mouseX, mouseY, partialTicks); }
    public static EasyGuiGraphics create(@Nonnull PoseStack pose, Font font, int mouseX, int mouseY, float partialTicks) { return new EasyGuiGraphics(pose, font, mouseX, mouseY, partialTicks); }
    public static EasyGuiGraphics create(@Nonnull ScreenEvent.Render event) { return new EasyGuiGraphics(event.getPoseStack(), event.getScreen().getMinecraft().font, event.getMouseX(), event.getMouseY(), event.getPartialTick()); }
    public static EasyGuiGraphics create(@Nonnull ContainerScreenEvent.Render event) { return new EasyGuiGraphics(event.getPoseStack(), event.getContainerScreen().getMinecraft().font, event.getMouseX(), event.getMouseY(), 0f); }

    //Color Rendering
    public void setColor(float r, float g, float b) { this.setColor(r,g,b,1f); }
    public void setColor(float r, float g, float b, float a) { RenderSystem.setShaderColor(r,g,b,a); }
    public void resetColor() { this.setColor(1f,1f,1f,1f); }


    //Texture Rendering
    public void renderNormalBackground(@Nonnull ResourceLocation image, @Nonnull IEasyScreen screen) { this.resetColor(); this.pushOffset(screen.getCorner()).blit(image, 0,0,0,0, screen.getXSize(), screen.getYSize()); this.popOffset(); }
    public void blit(@Nonnull ResourceLocation image, int x, int y, int u, int v, int width, int height) { RenderSystem.setShaderTexture(0, image); blit(this.pose, this.offset.x + x, this.offset.y + y, u, v, width, height); }
    public void blit(@Nonnull ResourceLocation image, @Nonnull ScreenPosition pos, int u, int v, int width, int height) { this.blit(image, pos.x, pos.y, u, v, width, height); }
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

    public void renderButtonBG(int x, int y, int width, int height, float alpha, int textureY)
    {
        this.setColor(1f, 1f, 1f, alpha);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(this.pose, this.offset.x + x, this.offset.y + y, 0, 46 + textureY * 20, width / 2, height);
        this.blit(this.pose, this.offset.x + x + width / 2, this.offset.y + y, 200 - width / 2, 46 + textureY * 20, width / 2, height);
        this.resetColor();
    }

    public void fill(int x, int y, int width, int height, int color) { fill(this.pose, this.offset.x + x, this.offset.y + y, this.offset.x + x + width, this.offset.y + y + height, color); }
    public void fill(ScreenPosition pos, int width, int height, int color) { this.fill(pos.x, pos.y, width, height, color); }
    public void fill(ScreenArea area, int color) { this.fill(area.x, area.y, area.width, area.height, color); }

    //Tooltip Related Rendering
    public void renderTooltip(@Nonnull Component tooltip) { this.pushOffset(this.mousePos).renderTooltip(tooltip, 0, 0); this.popOffset(); }
    public void renderTooltip(@Nonnull Component tooltip, int x, int y) { this.screen.ifPresent(s -> s.renderTooltip(this.pose, tooltip, this.offset.x + x, this.offset.y + y)); }
    public void renderComponentTooltip(@Nonnull List<Component> tooltip) { this.pushOffset(this.mousePos).renderComponentTooltip(tooltip, 0, 0); this.popOffset(); }
    public void renderComponentTooltip(@Nonnull List<Component> tooltip, int x, int y) { if(tooltip.size() == 0) return; this.screen.ifPresent(s -> s.renderComponentTooltip(this.pose, tooltip, this.offset.x + x, this.offset.y + y)); }
    public void renderTooltip(@Nonnull List<FormattedCharSequence> tooltip) { this.pushOffset(this.mousePos).renderTooltip(tooltip, 0,0); this.popOffset(); }
    public void renderTooltip(@Nonnull List<FormattedCharSequence> tooltip, int x, int y) { if(tooltip.size() == 0) return; this.screen.ifPresent(s -> s.renderTooltip(this.pose, tooltip, this.offset.x + x, this.offset.y + y)); }
    public void renderTooltip(@Nonnull ItemStack item) { this.pushOffset(this.mousePos).renderTooltip(item, 0,0); this.popOffset(); }
    public void renderTooltip(@Nonnull ItemStack item, int x, int y) { this.renderComponentTooltip(EasyScreenHelper.getTooltipFromItem(item), x, y); }

    //Text Related Rendering
    public void drawString(String text, int x, int y, int color) { this.font.draw(this.pose, text, this.offset.x + x, this.offset.y + y, color); }
    public void drawString(String text, ScreenPosition pos, int color) { this.drawString(text, pos.x, pos.y, color); }
    public void drawString(Component text, int x, int y, int color) { this.font.draw(this.pose, text, this.offset.x + x, this.offset.y + y, color); }
    public void drawString(Component text, ScreenPosition pos, int color) { this.drawString(text, pos.x, pos.y, color); }
    public void drawString(FormattedCharSequence text, int x, int y, int color) { this.font.draw(this.pose, text, this.offset.x + x, this.offset.y + y, color); }
    public void drawString(FormattedCharSequence text, ScreenPosition pos, int color) { this.drawString(text, pos.x, pos.y, color); }
    public void drawShadowed(String text, int x, int y, int color) { this.font.drawShadow(this.pose, text, this.offset.x + x, this.offset.y + y, color); }
    public void drawShadowed(Component text, int x, int y, int color) { this.font.drawShadow(this.pose, text, this.offset.x + x, this.offset.y + y, color); }
    public void drawShadowed(FormattedCharSequence text, int x, int y, int color) { this.font.drawShadow(this.pose, text, this.offset.x + x, this.offset.y + y, color); }

    public void drawWordWrap(String text, int x, int y, int columnWidth, int color) { this.font.drawWordWrap(EasyText.literal(text), this.offset.x + x, this.offset.y + y, columnWidth, color); }
    public void drawWordWrap(Component text, int x, int y, int columnWidth, int color) { this.font.drawWordWrap(text, this.offset.x + x, this.offset.y + y, columnWidth, color); }

    //Item Related Rendering
    public void renderItem(@Nonnull ItemStack item, int x, int y) { this.renderItem(item, x, y, null); }
    public void renderItem(@Nonnull ItemStack item, @Nonnull ScreenPosition pos) { this.renderItem(item, pos.x, pos.y); }
    public void renderItem(@Nonnull ItemStack item, @Nonnull ScreenPosition pos, @Nullable String countTextOverride) { this.renderItem(item, pos.x, pos.y, countTextOverride); }
    public void renderItem(@Nonnull ItemStack item, int x, int y, @Nullable String countTextOverride) {
        this.resetColor();
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        RenderSystem.enableDepthTest();
        itemRenderer.renderAndDecorateItem(item, this.offset.x + x, this.offset.y + y, 0);
        itemRenderer.renderGuiItemDecorations(this.font, item, this.offset.x + x, this.offset.y + y, countTextOverride);
    }

    public void renderSlotBackground(Pair<ResourceLocation,ResourceLocation> background, @Nonnull ScreenPosition pos) { this.renderSlotBackground(background, pos.x, pos.y); }
    public void renderSlotBackground(Pair<ResourceLocation,ResourceLocation> background, int x, int y) {
        if(background == null)
            return;
        Minecraft minecraft = Minecraft.getInstance();
        TextureAtlasSprite textureatlassprite = minecraft.getTextureAtlas(background.getFirst()).apply(background.getSecond());
        RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
        blit(this.pose, this.offset.x + x, this.offset.y + y, 100, 16, 16, textureatlassprite);
    }

    public void renderSlotHighlight(int x, int y) { fillGradient(this.pose, this.offset.x + x, this.offset.y + y, this.offset.x + x + 16, this.offset.y + y + 16, -2130706433, -2130706433, 0); }
    public void renderSlotHighlight(@Nonnull ScreenPosition pos) { this.renderSlotHighlight(pos.x, pos.y); }

    //PoseStack interactions
    public EasyGuiGraphics pushPose() { this.pose.pushPose(); return this; }
    public void TranslateToForeground() { this.pose.translate(0d,0d,0250d); }
    public void popPose() { this.pose.popPose(); }


}
