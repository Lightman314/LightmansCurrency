package io.github.lightman314.lightmanscurrency.api.misc.client.rendering;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.util.OutlineUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.joml.Vector4f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public final class EasyGuiGraphics {

    private static final List<ModelResourceLocation> debuggedModels = new ArrayList<>();

    public static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            VersionUtil.vanillaResource("widget/button"),
            VersionUtil.vanillaResource("widget/button_disabled"),
            VersionUtil.vanillaResource("widget/button_highlighted")
    );

    private final GuiGraphics gui;
    public GuiGraphics getGui() { return this.gui; }
    public final Font font;
    public PoseStack getPose() { return this.gui.pose(); }
    public int guiWidth() { return this.gui.guiWidth(); }
    public int guiHeight() { return this.gui.guiHeight(); }

    public final ScreenPosition mousePos;
    public final float partialTicks;

    private final List<ScreenPosition> offsetStack = new ArrayList<>();
    private ScreenPosition offset = ScreenPosition.ZERO;
    //Made public because it's helpful information to have when rendering something complicated like Fluids, etc.
    public ScreenPosition getOffset() { return this.offset; }

    public EasyGuiGraphics pushOffsetZero() { return this.pushOffset(ScreenPosition.ZERO); }
    public EasyGuiGraphics pushOffset(ScreenPosition offset) { this.offsetStack.addFirst(offset); return this.refactorOffset(); }
    public EasyGuiGraphics pushOffset(AbstractWidget widget) { this.offsetStack.addFirst(ScreenPosition.of(widget.getX(), widget.getY())); return this.refactorOffset(); }
    public EasyGuiGraphics popOffset() { if(!this.offsetStack.isEmpty()) this.offsetStack.removeFirst(); return this.refactorOffset(); }
    private EasyGuiGraphics refactorOffset() { this.offset = !this.offsetStack.isEmpty() ? this.offsetStack.getFirst() : ScreenPosition.ZERO; return this; }

    private EasyGuiGraphics(GuiGraphics gui, Font font, int mouseX, int mouseY, float partialTicks) { this.gui = gui; this.font = font; this.mousePos = ScreenPosition.of(mouseX, mouseY); this.partialTicks = partialTicks; }
    public static EasyGuiGraphics create(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) { return create(gui, Minecraft.getInstance().font, mouseX, mouseY, partialTicks); }
    public static EasyGuiGraphics create(GuiGraphics gui, Font font, int mouseX, int mouseY, float partialTicks) { return new EasyGuiGraphics(gui, font, mouseX, mouseY, partialTicks); }
    public static EasyGuiGraphics create(ScreenEvent.Render event) { return new EasyGuiGraphics(event.getGuiGraphics(), event.getScreen().getMinecraft().font, event.getMouseX(), event.getMouseY(), event.getPartialTick()); }
    public static EasyGuiGraphics create(ContainerScreenEvent.Render event) { return new EasyGuiGraphics(event.getGuiGraphics(), event.getContainerScreen().getMinecraft().font, event.getMouseX(), event.getMouseY(), 0f); }

    //Color Rendering
    public void setColor(float r, float g, float b) { this.setColor(r,g,b,1f); }
    public void setColor(float r, float g, float b, float a) { this.gui.setColor(r,g,b,a); }
    public void setColor(int color) { this.setColor(OutlineUtil.decodeColor(color)); }
    public void setColor(int color, float alpha) { this.setColor(OutlineUtil.decodeColor(color,alpha)); }
    public void setColor(Vector4f color) { this.gui.setColor(color.x,color.y,color.z,color.w); }
    public void resetColor() { this.setColor(1f,1f,1f,1f); }

    //Texture Rendering
    public void renderNormalBackground(ResourceLocation image, IEasyScreen screen)
    {
        this.resetColor();
        this.pushOffset(screen.getCorner()).blit(image, 0,0,0,0, screen.getXSize(), screen.getYSize());
        this.popOffset();
    }
    public void renderNormalBackground(ResourceLocation image, IEasyScreen screen, int color)
    {
        this.setColor(color,1f);
        this.pushOffset(screen.getCorner()).blit(image, 0,0,0,0, screen.getXSize(), screen.getYSize());
        this.resetColor();
        this.popOffset();
    }
    public void renderNormalBackground(IEasyScreen screen)
    {
        this.resetColor();
        this.pushOffset(screen.getCorner());
        SpriteUtil.GENERIC_BACKGROUND.render(this,0,0,screen.getXSize(),screen.getYSize());
        this.popOffset();
    }
    public void renderSlot(IEasyScreen screen, Slot slot) { if(slot.isActive()) this.renderSlot(screen, ScreenPosition.of(slot.x, slot.y));}
    public void renderSlot(IEasyScreen screen, int posX, int posY) { this.renderSlot(screen,ScreenPosition.of(posX,posY)); }
    public void renderSlot(IEasyScreen screen, int posX, int posY, FixedSizeSprite type) { this.renderSlot(screen,ScreenPosition.of(posX,posY),type); }
    public void renderSlot(IEasyScreen screen, ScreenPosition position) { this.renderSlot(screen,position,SpriteUtil.EMPTY_SLOT_NORMAL); }
    public void renderSlot(IEasyScreen screen, ScreenPosition position, FixedSizeSprite type)
    {
        this.resetColor();
        this.pushOffset(screen.getCorner());
        type.render(this,position.offset(-1,-1));
        this.popOffset();
    }
    public void blit(ResourceLocation image, int x, int y, int u, int v, int width, int height) { this.gui.blit(image, this.offset.x + x, this.offset.y + y, u, v, width, height); }
    public void blit(ResourceLocation image, ScreenPosition pos, int u, int v, int width, int height) { this.blit(image, pos.x, pos.y, u, v, width, height); }
    public void blit(ResourceLocation image, ScreenArea area, int u, int v) { this.blit(image, area.pos.x, area.pos.y, u, v, area.width, area.height); }
    public void blit(ResourceLocation image, int x, int y, int u, int v, int width, int height, int imageWidth, int imageHeight) { this.gui.blit(image, this.offset.x + x, this.offset.y + y, u, v, width, height, imageWidth, imageHeight); }
    public void blit(ResourceLocation image, ScreenPosition pos, int u, int v, int width, int height, int imageWidth, int imageHeight) { this.blit(image, pos.x, pos.y, u, v, width, height, imageWidth, imageHeight); }
    public void blit(ResourceLocation image, ScreenArea area, int u, int v, int imageWidth, int imageHeight) { this.blit(image, area.pos.x, area.pos.y, u, v, area.width, area.height, imageWidth, imageHeight); }

    public void blitSpriteFadeHoriz(SpriteSource sprite, int x, int y, float percent) {
        int blitWidth = MathUtil.clamp((int)((sprite.width() + 1) * percent), 0, sprite.width());
        this.blit(sprite.texture(), x, y, sprite.u(), sprite.v(), blitWidth, sprite.height(),sprite.textureWidth(),sprite.textureHeight());
    }
    public void blitSpriteFadeHoriz(SpriteSource sprite, ScreenPosition pos, float percent) { this.blitSpriteFadeHoriz(sprite, pos.x, pos.y, percent); }

    public void renderButtonBG(int x, int y, int width, int height, float alpha, AbstractWidget widget) { this.renderButtonBG(x,y,width,height,alpha,widget,0xFFFFFF); }
    public void renderButtonBG(int x, int y, int width, int height, float alpha, AbstractWidget widget, int color)
    {
        Minecraft minecraft = Minecraft.getInstance();
        this.setColor(color, alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        this.gui.blitSprite(BUTTON_SPRITES.get(widget.active, widget.isHoveredOrFocused()), x + this.offset.x, y + this.offset.y, width, height);
        this.resetColor();
    }

    public void fill(int x, int y, int width, int height, int color) { this.gui.fill(this.offset.x + x, this.offset.y + y, this.offset.x + x + width, this.offset.y + y + height, color); }
    public void fill(ScreenPosition pos, int width, int height, int color) { this.fill(pos.x, pos.y, width, height, color); }
    public void fill(ScreenArea area, int color) { this.fill(area.x, area.y, area.width, area.height, color); }

    //Tooltip Related Rendering
    public void renderTooltip(Component tooltip) { this.pushOffset(this.mousePos).renderTooltip(tooltip, 0, 0); this.popOffset(); }
    public void renderTooltip(Component tooltip, int x, int y) { this.gui.renderTooltip(this.font, tooltip, this.offset.x + x, this.offset.y + y); }
    public void renderComponentTooltip(List<Component> tooltip) { this.pushOffset(this.mousePos).renderComponentTooltip(tooltip, 0, 0); this.popOffset(); }
    public void renderComponentTooltip(List<Component> tooltip, int x, int y) { if(tooltip.isEmpty()) return; this.gui.renderComponentTooltip(this.font, tooltip, this.offset.x + x, this.offset.y + y); }
    public void renderTooltip(List<FormattedCharSequence> tooltip) { this.pushOffset(this.mousePos).renderTooltip(tooltip, 0,0); this.popOffset(); }
    public void renderTooltip(List<FormattedCharSequence> tooltip, int x, int y) { if(tooltip.isEmpty()) return; this.gui.renderTooltip(this.font, tooltip, this.offset.x + x, this.offset.y + y); }
    public void renderTooltip(ItemStack item) { this.pushOffset(this.mousePos).renderTooltip(item, 0,0); this.popOffset(); }
    public void renderTooltip(ItemStack item, int x, int y) { this.gui.renderTooltip(this.font, item, this.offset.x + x, this.offset.y + y); }

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

    public void drawScrollingString(String text, int x, int y, int width, int height, int color) { this.drawScrollingString(EasyText.literal(text),x,y,width,height,color); }
    public void drawScrollingString(Component text, int x, int y, int width, int height, int color) { this.drawScrollingString(text,ScreenArea.of(x,y,width,height),color); }
    public void drawScrollingString(String text, ScreenArea area, int color) { this.drawScrollingString(EasyText.literal(text),area,color); }
    public void drawScrollingString(Component text, ScreenArea area, int color)
    {
        //Offset position based on the current offset
        area = area.offsetPosition(this.offset);
        AbstractWidget.renderScrollingString(this.gui,this.font,text,area.x,area.y,area.x + area.width,area.y + area.height,color);
    }

    //Item Related Rendering
    public void renderItem(ItemStack item, int x, int y) { this.renderItem(item, x, y, null); }
    public void renderItem(ItemStack item, ScreenPosition pos) { this.renderItem(item, pos.x, pos.y); }
    public void renderItem(ItemStack item, ScreenPosition pos, @Nullable String countTextOverride) { this.renderItem(item, pos.x, pos.y, countTextOverride); }
    public void renderItem(ItemStack item, int x, int y, @Nullable String countTextOverride) {
        this.resetColor();
        this.gui.renderItem(item, this.offset.x + x, this.offset.y + y);
        this.gui.renderItemDecorations(this.font, item, this.offset.x + x, this.offset.y + y, countTextOverride);
    }

    public void renderScaledItem(ItemStack item, ScreenPosition pos, float scale) { this.renderScaledItem(item,pos.x,pos.y,scale); }
    public void renderScaledItem(ItemStack item, int x, int y, float scale) {
        this.resetColor();
        //Copied from GuiGraphics#renderItem
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel bakedmodel = minecraft.getItemRenderer().getModel(item,null,null,0);
        PoseStack pose = this.getPose();
        pose.pushPose();
        //Translate to the top-left corner without the additional offset to center the model
        pose.translate((float)(this.offset.x + x), (float)(this.offset.y + y), 150f);
        try {
            //Apply custom scale
            pose.scale(scale,scale,scale);
            //Translate the additional 8 pixels to center the model after the custom scale has been applied
            pose.translate(8,8,0);
            //Then apply the vanilla item scaling
            pose.scale(16.0F, -16.0F, 16.0F);

            boolean flag = !bakedmodel.usesBlockLight();
            if (flag) {
                Lighting.setupForFlatItems();
            }

            minecraft.getItemRenderer()
                    .render(new ItemStack(ModItems.COIN_COPPER.get()), ItemDisplayContext.GUI, false, pose, this.gui.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
            this.gui.flush();
            if (flag) {
                Lighting.setupFor3DItems();
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Item Model");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
            crashreportcategory.setDetail("Item Type", () -> String.valueOf(item.getItem()));
            crashreportcategory.setDetail("Item Components", () -> String.valueOf(item.getComponents()));
            crashreportcategory.setDetail("Item Foil", () -> String.valueOf(item.hasFoil()));
            throw new ReportedException(crashreport);
        }
        pose.popPose();
    }

    public void renderItemModel(ModelResourceLocation model, int x, int y) { this.renderItemModel(model,x,y,new ItemStack(Items.BARRIER)); }
    public void renderItemModel(ModelResourceLocation model, int x, int y, ItemStack fallback) {
        this.resetColor();
        //Copied from GuiGraphics#renderItem
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel bakedmodel = minecraft.getModelManager().getModel(model);
        if(bakedmodel == minecraft.getModelManager().getMissingModel())
        {
            if(!debuggedModels.contains(model))
            {
                LightmansCurrency.LogWarning("Missing model for " + model + ". Rendering fallback item.");
                debuggedModels.add(model);
            }
            this.renderItem(fallback,x,y);
            return;
        }
        PoseStack pose = this.getPose();
        pose.pushPose();
        pose.translate((float)(this.offset.x + x + 8), (float)(this.offset.y + y + 8), 150f);

        try {
            pose.scale(16.0F, -16.0F, 16.0F);
            boolean flag = !bakedmodel.usesBlockLight();
            if (flag) {
                Lighting.setupForFlatItems();
            }

            minecraft.getItemRenderer()
                    .render(new ItemStack(ModItems.COIN_COPPER.get()), ItemDisplayContext.GUI, false, pose, this.gui.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
            this.gui.flush();
            if (flag) {
                Lighting.setupFor3DItems();
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Item Model");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Model being rendered");
            crashreportcategory.setDetail("Model ID", () -> String.valueOf(model));
            throw new ReportedException(crashreport);
        }
        pose.popPose();
    }

    public void renderSlotBackground(Pair<ResourceLocation,ResourceLocation> background, ScreenPosition pos) { this.renderSlotBackground(background, pos.x, pos.y); }
    public void renderSlotBackground(Pair<ResourceLocation,ResourceLocation> background, int x, int y) {
        if(background == null)
            return;
        TextureAtlasSprite textureatlassprite = Minecraft.getInstance().getTextureAtlas(background.getFirst()).apply(background.getSecond());
        this.gui.blit(this.offset.x + x, this.offset.y + y, 0, 16, 16, textureatlassprite);
    }

    public void renderSlotHighlight(int x, int y) { this.gui.fillGradient(RenderType.guiOverlay(), this.offset.x + x, this.offset.y + y, this.offset.x + x + 16, this.offset.y + y + 16, -2130706433, -2130706433, 0); }
    public void renderSlotHighlight(ScreenPosition pos) { this.renderSlotHighlight(pos.x, pos.y); }

    public void enableScissor(int x, int y, int width, int height) { this.enableScissor(ScreenArea.of(x,y,width,height)); }
    public void enableScissor(ScreenArea area) {
        area = area.offsetPosition(this.offset);
        this.gui.enableScissor(area.x,area.y,area.x + area.width, area.y + area.height);
    }
    public void disableScissor() { this.gui.disableScissor(); }

    //PoseStack interactions
    public EasyGuiGraphics pushPose() { this.gui.pose().pushPose(); return this; }
    public void TranslateToForeground() { this.gui.pose().translate(0d,0d,250d); }
    public void popPose() { this.gui.pose().popPose(); }


}
