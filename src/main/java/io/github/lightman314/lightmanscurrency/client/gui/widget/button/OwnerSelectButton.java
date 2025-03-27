package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OwnerSelectButton extends EasyButton implements ITooltipWidget {

    public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/buttons.png");

    private final Supplier<OwnerData> currentOwner;
    private final Supplier<PotentialOwner> ownerSupplier;
    private final Supplier<Boolean> parentVisible;
    public PotentialOwner getOwner() { return this.ownerSupplier.get(); }

    public static final int HEIGHT = 20;

    private OwnerSelectButton(@Nonnull Builder builder)
    {
        super(builder);
        this.currentOwner = builder.selectedOwner;
        this.ownerSupplier = builder.owner;
        this.parentVisible = builder.visible;
    }

    @Override
    protected void renderTick() {
        PotentialOwner owner = this.getOwner();
        this.setVisible(this.parentVisible.get() && owner != null);
        if(this.visible)
        {
            OwnerData data = this.currentOwner.get();
            if(data != null)
                this.setActive(!data.getValidOwner().matches(owner.asOwner()));
        }
    }

    @Override
    protected void renderWidget(@Nonnull EasyGuiGraphics gui) {
        PotentialOwner owner = this.getOwner();
        if(owner == null)
        {
            this.setVisible(false);
            return;
        }
        //Set to gray if not active
        float color = this.isActive() ? 1f : 0.5f;
        gui.setColor(color,color,color);
        //Render Background
        gui.blitHorizSplit(GUI_TEXTURE,0,0, this.width, this.height,0,0,256,5);

        //Render owner
        IconData icon = owner.getIcon();
        if(icon != null)
            icon.render(gui, 2, 2);
        //Render the name
        Component name = TextRenderUtil.fitString(owner.getName(), this.width - 22);
        int textColor = this.isActive() ? 0xFFFFFF : 0x7F7F7F / 2;
        gui.drawShadowed(name, 22, 6, textColor);
        //Reset color
        gui.resetColor();
    }

    @Override
    public List<Component> getTooltipText() {
        List<Component> tooltip = new ArrayList<>();
        PotentialOwner owner = this.getOwner();
        if(owner != null)
            owner.appendTooltip(tooltip);
        return tooltip;
    }

    @Nonnull
    public static Builder builder() { return new Builder(); }

    @MethodsReturnNonnullByDefault
    @FieldsAreNonnullByDefault
    public static class Builder extends EasyButtonBuilder<Builder>
    {
        private Builder() { super(100,HEIGHT); }
        @Override
        protected Builder getSelf() { return this; }

        private Supplier<OwnerData> selectedOwner = () -> null;
        private Supplier<PotentialOwner> owner = () -> null;
        private Supplier<Boolean> visible = () -> true;

        public Builder width(int width) { this.changeWidth(width); return this; }
        public Builder selected(Supplier<OwnerData> selectedOwner) { this.selectedOwner = selectedOwner; return this; }
        public Builder potentialOwner(Supplier<PotentialOwner> owner) { this.owner = owner; return this; }
        public Builder visible(Supplier<Boolean> visible) { this.visible = visible; return this; }

        public OwnerSelectButton build() { return new OwnerSelectButton(this); }

    }

}