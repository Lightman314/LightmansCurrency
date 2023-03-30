package io.github.lightman314.lightmanscurrency.client.gui.widget.easy;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.client.gui.util.ScreenUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationThunk;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public abstract class EasyWidget extends GuiComponent implements Renderable, GuiEventListener, NarratableEntry, ITooltipSource {

    private ScreenPosition position = ScreenPosition.ZERO;
    @NotNull
    public ScreenPosition getPosition() { return this.position; }
    public int getPosX() { return this.position.x; }
    public int getPosY() { return this.position.y; }
    public void setPosition(@NotNull ScreenPosition position) { this.position = position; }

    private int width;
    private int height;
    private final boolean fixedSize;
    public int getWidth() { return this.width; }
    public int getHeight() { return this.height; }
    protected void setWidth(int width) { if(this.fixedSize) return; this.width = width; }
    protected void setHeight(int height) { if(this.fixedSize) return; this.height = height; }

    private NonNullSupplier<Boolean> visibleCheck;
    public boolean isVisible() { return this.visibleCheck.get(); }
    public void setVisible(boolean visible) { this.visibleCheck = () -> visible; }
    public void setVisible(NonNullSupplier<Boolean> visibleCheck) { this.visibleCheck = visibleCheck == null ? () -> true : visibleCheck; }

    private NonNullSupplier<Boolean> activeCheck;
    @Override
    public boolean isActive() { return this.isVisible() && this.activeCheck.get(); }
    public void setActive(boolean active) { this.activeCheck = () -> active; }
    public void setActive(NonNullSupplier<Boolean> activeCheck) { this.activeCheck = activeCheck == null ? () -> true : activeCheck; }

    private boolean hovered;
    public boolean isHovered() { return this.hovered && this.isActive(); }

    private boolean focused = false;
    @Override
    public void setFocused(boolean focused) { this.focused = focused; }
    @Override
    public boolean isFocused() { return this.focused; }

    private Supplier<List<Component>> tooltipSource;
    public void setTooltipSource(Supplier<List<Component>> tooltipSource) { this.tooltipSource = tooltipSource == null ? () -> null : tooltipSource; }

    public final Font font;

    protected EasyWidget(IEasyWidgetBuilder builder)
    {
        this.setPosition(builder.getPosition());
        this.width = builder.getWidth();
        this.height = builder.getHeight();
        this.fixedSize = builder.isFixedSize();
        this.visibleCheck = builder.getVisibilityCheck();
        this.activeCheck = builder.getActiveCheck();
        this.tooltipSource = builder.getTooltip();
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public final void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        if (this.isVisible())
        {
            this.hovered = this.isMouseOver(mouseX, mouseY);
            this.renderWidget(pose, mouseX, mouseY, partialTicks);
        }
        else
            LightmansCurrency.LogDebug("Not rendering widget as it is not visible.");
    }

    protected abstract void renderWidget(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks);

    protected int getYImage() {
        if (!this.isActive())
            return 0;
        else if (this.hovered)
            return 2;
        return 1;
    }

    protected int getFGColor() { return this.isActive() ? 16777215 : 10526880; }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) { return this.isActive() && ScreenUtil.isMouseOver(mouseX, mouseY, this.position, this.width, this.height); }

    @Nullable
    @Override
    public List<Component> getTooltip(int mouseX, int mouseY) {
        if(this.isMouseOver(mouseX, mouseY))
            return this.getTooltip();
        return null;
    }

    @Nullable
    @Override
    public List<Component> getTooltip() { return this.tooltipSource.get(); }


    @Override
    @NotNull
    public NarratableEntry.NarrationPriority narrationPriority() { return this.isHovered() ? NarrationPriority.HOVERED : NarrationPriority.NONE; }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrator) {
        if(this.isHovered())
        {
            List<Component> tooltips = this.getTooltip();
            if(tooltips != null)
                narrator.add(NarratedElementType.HINT, NarrationThunk.from(tooltips));
        }
    }

}
