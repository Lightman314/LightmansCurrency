package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.button.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.button.IEasyButtonBuilder;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TextButton extends EasyButton {

    private Supplier<Component> text;
    public void setText(Component text) { this.text = () -> text; }
    public void setText(Supplier<Component> text) { this.text = text == null ? EasyText::empty : text; }

    protected TextButton(TextButtonBuilder builder) { super(builder); this.text = builder.getText(); }

    @Override
    protected void renderWidget(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int i = this.getYImage();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(pose, this.getPosX(), this.getPosY(), 0, 46 + i * 20, this.getWidth() / 2, this.getHeight());
        blit(pose, this.getPosX() + this.getWidth() / 2, this.getPosY(), 200 - this.getWidth() / 2, 46 + i * 20, this.getWidth() / 2, this.getHeight());
        drawCenteredString(pose, font, this.text.get(), this.getPosX() + this.getWidth() / 2, this.getPosY() + (this.getHeight() - 8) / 2, this.getFGColor() | Mth.ceil(255f) << 24);
    }

    public static TextButtonBuilder builder() { return new TextButtonBuilder(); }

    public static class TextButtonBuilder implements IEasyButtonBuilder
    {

        private TextButtonBuilder() {}

        Supplier<Component> text = EasyText::empty;
        public TextButtonBuilder withText(@NotNull Component text) { this.text = () -> text; return this; }
        public TextButtonBuilder withText(@NotNull Supplier<Component> text) { this.text = text; return this; }
        public Supplier<Component> getText() { return this.text; }

        ScreenPosition position = ScreenPosition.ZERO;
        public TextButtonBuilder atPosition(int x, int y) { this.position = ScreenPosition.of(x,y); return this; }
        public TextButtonBuilder atPosition(@NotNull ScreenPosition position) { this.position = position; return this; }
        @Override
        public ScreenPosition getPosition() { return this.position; }

        int width = 0;
        int height = 20;
        public TextButtonBuilder ofSize(int width) { this.width = width; return this; }
        public TextButtonBuilder ofSize(int width, int height) { this.width = width; this.height = height; return this; }
        @Override
        public boolean isFixedSize() { return false; }
        @Override
        public int getWidth() { return width; }
        @Override
        public int getHeight() { return height; }

        Supplier<List<Component>> tooltipSource = () -> null;
        public TextButtonBuilder withTooltip(@NotNull Component tooltip) { this.tooltipSource = () -> ImmutableList.of(tooltip); return this; }
        public TextButtonBuilder withTooltip(@NotNull List<Component> tooltip) { this.tooltipSource = () -> tooltip; return this; }
        public TextButtonBuilder withTooltip(@NotNull Supplier<List<Component>> tooltipSource) { this.tooltipSource = tooltipSource; return this; }
        @Override
        public Supplier<List<Component>> getTooltip() { return this.tooltipSource; }

        private NonNullSupplier<Boolean> visibilityCheck = () -> true;
        public TextButtonBuilder withVisibility(@NotNull NonNullSupplier<Boolean> visibilityCheck) { this.visibilityCheck = visibilityCheck; return this; }
        @Override
        public NonNullSupplier<Boolean> getVisibilityCheck() { return this.visibilityCheck; }

        private NonNullSupplier<Boolean> activeCheck = () -> true;
        public TextButtonBuilder withActivity(@NotNull NonNullSupplier<Boolean> activeCheck) { this.activeCheck = activeCheck; return this; }
        @Override
        public NonNullSupplier<Boolean> getActiveCheck() { return this.activeCheck; }


        private Consumer<Object> onClick = (b) -> {};
        public TextButtonBuilder onClick(@NotNull Runnable onClick) { this.onClick = (b) -> onClick.run(); return this; }
        public TextButtonBuilder onClick(@NotNull Consumer<Object> onClick) { this.onClick = onClick; return this; }
        @Override
        public Consumer<Object> getClickConsumer() { return this.onClick; }

        public TextButton build() { return new TextButton(this); }

    }

}
