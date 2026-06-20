package io.github.lightman314.lightmanscurrency.api.client.gui.helpers;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FancyGuiExtractor {

    public static final WidgetSprites DEFAULT_BUTTON_SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/button"),
            Identifier.withDefaultNamespace("widget/button_disabled"),
            Identifier.withDefaultNamespace("widget/button_highlighted")
    );

    public static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back");
    public static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front");

    public static final Identifier SLOT_NORMAL = Identifier.withDefaultNamespace("container/slot");
    public static final Identifier SLOT_YELLOW = LCApi.id("container/slot_yellow");
    public static final Identifier SLOT_GREEN = LCApi.id("container/slot_green");

    private final GuiGraphicsExtractor gui;
    public GuiGraphicsExtractor getGui() { return this.gui; }
    public Matrix3x2fStack getPose() { return this.gui.pose(); }
    private final ScreenPosition mousePos;
    public ScreenPosition getMousePos() { return this.mousePos; }
    private final float partial;
    public float getPartialTicks() { return this.partial; }

    private final List<ScreenPosition> offset = new ArrayList<>();
    public ScreenPosition getOffset() { return this.offset.isEmpty() ? ScreenPosition.ZERO : this.offset.getLast(); }
    public ScreenPosition getPosition(int x,int y) { return this.getOffset().offset(x,y); }
    public ScreenPosition getPosition(ScreenPosition position) { return this.getOffset().offset(position); }

    private final Font font = Minecraft.getInstance().font;
    public Font getFont() { return this.font; }

    public FancyGuiExtractor(GuiGraphicsExtractor gui,float partial) { this(gui,-1,-1,partial); }
    public FancyGuiExtractor(GuiGraphicsExtractor gui,int mouseX,int mouseY,float partial)
    {
        this.gui = gui;
        this.mousePos = ScreenPosition.of(mouseX,mouseY);
        this.partial = partial;
    }

    //Offset push/pops
    public FancyGuiExtractor push(ScreenPosition position) { this.offset.add(position); return this; }
    public FancyGuiExtractor pushZero() { return this.push(ScreenPosition.ZERO); }
    public FancyGuiExtractor popPush(ScreenPosition position) { return this.pop().push(position); }
    public FancyGuiExtractor pop() { this.offset.removeLast(); return this; }

    //Scissor
    public void enableScissor(int x,int y,int width,int height) {
        ScreenPosition p = this.getPosition(x,y);
        this.gui.enableScissor(p.x,p.y,p.x + width,p.y + height);
    }
    public void disableScissor() { this.gui.disableScissor(); }
    public boolean pointInScissor(int x,int y) {
        ScreenPosition p = this.getPosition(x,y);
        return this.gui.containsPointInScissor(p.x,p.y);
    }

    //Text
    public void text(String text,int x,int y,int color) { this.text(text,x,y,color,true); }
    public void text(String text,int x,int y,int color,boolean shadow) {
        ScreenPosition p = this.getPosition(x,y);
        this.gui.text(this.font,text,p.x,p.y,color,shadow);
    }
    public void text(FormattedCharSequence text, int x, int y, int color) { this.text(text,x,y,color,true); }
    public void text(FormattedCharSequence text,int x,int y,int color,boolean shadow) {
        ScreenPosition p = this.getPosition(x,y);
        this.gui.text(this.font,text,p.x,p.y,color,shadow);
    }
    public void text(Component text, int x, int y, int color) { this.text(text,x,y,color,true); }
    public void text(Component text,int x,int y,int color,boolean shadow) {
        ScreenPosition p = this.getPosition(x,y);
        this.gui.text(this.font,text,p.x,p.y,color,shadow);
    }
    public void centeredText(String text,int centerX,int y,int color,boolean shadow) {
        ScreenPosition p = this.getPosition(centerX,y);
        this.gui.text(this.font,text,p.x - (this.font.width(text) / 2),p.y,color,shadow);
    }
    public void centeredText(FormattedCharSequence text,int centerX,int y,int color,boolean shadow) {
        ScreenPosition p = this.getPosition(centerX,y);
        this.gui.text(this.font,text,p.x - (this.font.width(text) / 2),p.y,color,shadow);
    }
    public void centeredText(Component text,int centerX,int y,int color,boolean shadow) {
        ScreenPosition p = this.getPosition(centerX,y);
        this.gui.text(this.font,text,p.x - (this.font.width(text) / 2),p.y,color,shadow);
    }
    public void textWithWordWrap(FormattedText text,int x,int y,int width,int color) { this.textWithWordWrap(text,x,y,width,color,true); }
    public void textWithWordWrap(FormattedText text,int x,int y,int width,int color,boolean shadow) {
        ScreenPosition p = this.getPosition(x,y);
        int yOff = 0;
        for(FormattedCharSequence line : this.font.split(text,width))
        {
            this.gui.text(this.font,line,p.x,p.y + yOff,color,shadow);
            yOff += this.font.lineHeight;
        }
    }
    public ActiveTextCollector textRendererForWidget(AbstractWidget owner,GuiGraphicsExtractor.HoveredTextEffects effects) { return this.gui.textRendererForWidget(owner,effects); }
    public ActiveTextCollector textRenderer() { return this.gui.textRenderer(); }
    public ActiveTextCollector textRenderer(GuiGraphicsExtractor.HoveredTextEffects effects) { return this.gui.textRenderer(effects); }

    //Blit
    public void blit(Identifier texture,int x,int y,int u,int v,int width,int height) { this.blit(RenderPipelines.GUI_TEXTURED,texture,x,y,u,v,width,height,256,256); }
    public void blit(Identifier texture,int x,int y,int u,int v,int width,int height,int textureWidth,int textureHeight) { this.blit(RenderPipelines.GUI_TEXTURED,texture,x,y,u,v,width,height,textureWidth,textureHeight); }
    public void blit(RenderPipeline pipeline,Identifier texture,int x,int y,int u,int v,int width,int height,int textureWidth,int textureHeight) {
        ScreenPosition p = this.getPosition(x,y);
        this.gui.blit(pipeline,texture,p.x,p.y,u,v,width,height,textureWidth,textureHeight);
    }
    public void blitButtonSprite(int x,int y,int width,int height,boolean active,boolean hoveredOrFocused) {
        this.blitSprite(DEFAULT_BUTTON_SPRITES.get(active,hoveredOrFocused),x,y,width,height);
    }
    public void blitSprite(Identifier sprite,int x,int y,int width,int height) { this.blitSprite(RenderPipelines.GUI_TEXTURED,sprite,x,y,width,height); }
    public void blitSprite(RenderPipeline pipeline,Identifier sprite,int x,int y,int width,int height) {
        ScreenPosition p = this.getPosition(x,y);
        this.gui.blitSprite(pipeline,sprite,p.x,p.y,width,height);
    }
    public void blitSprite(TextureAtlasSprite sprite, int x, int y, int width, int height) { this.blitSprite(RenderPipelines.GUI_TEXTURED,sprite,x,y,width,height); }
    public void blitSprite(RenderPipeline pipeline,TextureAtlasSprite sprite,int x,int y,int width,int height) {
        ScreenPosition p = this.getPosition(x,y);
        this.gui.blitSprite(pipeline,sprite,p.x,p.y,width,height);
    }

    public void blitBackground(Identifier texture, ScreenArea area) {
        this.push(area.pos);
        this.blit(texture,0,0,0,0,area.width,area.height);
        this.pop();
    }

    public void blitSlot(Slot slot) { this.blitSlot(slot,SLOT_NORMAL);}
    public void blitSlot(Slot slot,Identifier sprite) { this.blitSlot(slot.x - 1,slot.y - 1,sprite); }
    public void blitSlot(int x,int y) { this.blitSlot(x,y,SLOT_NORMAL); }
    public void blitSlot(int x,int y,Identifier sprite) { this.blitSprite(sprite,x,y,18,18); }
    public void blitSlot(ScreenPosition position) { this.blitSlot(position,SLOT_NORMAL); }
    public void blitSlot(ScreenPosition position,Identifier sprite) { this.blitSprite(sprite,position.x,position.y,18,18); }
    public void blitSlots(List<? extends Slot> slots) { this.blitSlots(slots,SLOT_NORMAL); }
    public void blitSlots(List<? extends Slot> slots,Identifier sprite) {
        for(Slot s : slots)
            this.blitSlot(s,sprite);
    }

    public void blitSlotHighlightBack(ScreenPosition position) { this.blitSlotHighlightBack(position.x,position.y);}
    public void blitSlotHighlightBack(int x,int y) {
        this.blitSprite(SLOT_HIGHLIGHT_BACK_SPRITE,x - 3,y - 3,24,24);
    }
    public void blitSlotHighlightFront(ScreenPosition position) { this.blitSlotHighlightFront(position.x,position.y); }
    public void blitSlotHighlightFront(int x,int y) {
        this.blitSprite(SLOT_HIGHLIGHT_FRONT_SPRITE,x - 3,y - 3,24,24);
    }

    //Items
    public void item(ItemStack item,int x,int y) { this.item(item,x,y,null); }
    public void item(ItemStack item,ScreenPosition position) { this.item(item,position,null); }
    public void item(ItemStack item,int x,int y,@Nullable String countText) { this.item(item,ScreenPosition.of(x,y),countText); }
    public void item(ItemStack item,ScreenPosition position,@Nullable String countText) {
        ScreenPosition p = this.getPosition(position);
        this.gui.item(item,p.x,p.y);
        this.gui.itemDecorations(this.font,item,p.x,p.y,countText);
    }
    public void undecoratedItem(ItemStack item,ScreenPosition position) { this.undecoratedItem(item,position.x,position.y); }
    public void undecoratedItem(ItemStack item,int x,int y) {
        ScreenPosition p = this.getPosition(x,y);
        this.gui.fakeItem(item,p.x,p.y);
    }

    //Tooltips
    public void renderItemTooltipAtMouse(ItemStack item) {
        this.gui.setTooltipForNextFrame(this.font,item,this.mousePos.x,this.mousePos.y);
    }
    public void renderTooltipAtMouse(Component tooltip) { this.renderTooltipAtMouse(ImmutableList.of(tooltip)); }
    public void renderTooltipAtMouse(List<Component> tooltip)
    {
        this.gui.setComponentTooltipForNextFrame(this.font,tooltip,this.mousePos.x,this.mousePos.y);
    }

    public void renderPositionedTooltip(List<Component> tooltip, ClientTooltipPositioner positioner)
    {
        List<FormattedCharSequence> list = tooltip.stream().map(Component::getVisualOrderText).toList();
        this.gui.setTooltipForNextFrame(this.font,list, Optional.empty(),positioner,this.mousePos.x,this.mousePos.y,true,null);
    }

}