package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OwnerSelectButton extends FancyButton {

    public static final int HEIGHT = 20;

    private final Supplier<PotentialOwner> owner;

    private OwnerSelectButton(Builder builder) {
        super(builder);
        this.owner = builder.owner;
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        PotentialOwner owner = this.owner.get();
        if(owner == null) {
            this.visible = false;
            return;
        }
        //Render BG
        gui.blitSprite(LCSprites.BUTTON_BROWN.get(this.active,this.isHovered),0,0,area.width,area.height,this.getSpriteColor());

        //Render Icon
        IconData icon = owner.getIcon();
        if(icon != null)
            IconRenderer.extractState(gui,icon,2,2);
        //Render Owner Name
        Component name = owner.getName();
        int textColor = this.isActive() ? 0xFFFFFFFF : 0xFF404040;
        gui.textWithScrollingOverflow(name,22,6,area.width - 24,textColor,false);
    }

    @Override
    protected List<Component> collectTooltips(ScreenPosition mousePos) {
        List<Component> tooltip = new ArrayList<>();
        PotentialOwner owner = this.owner.get();
        if(owner != null)
            owner.appendTooltip(tooltip);
        return tooltip;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends ButtonBuilder<Builder,OwnerSelectButton> {

        private Builder() { super(100,HEIGHT); }

        private Supplier<PotentialOwner> owner = () -> null;

        public Builder ofWidth(int width) { this.setWidth(width); return this; }
        public Builder selected(Supplier<Owner> selectedOwner) { return this.active1(w -> {
                if(w instanceof OwnerSelectButton b) {
                    Owner data = selectedOwner.get();
                    if(data != null) {
                        PotentialOwner po = b.owner.get();
                        return po != null && !data.equals(po.asOwner());
                    }
                }
                return false;
            });
        }
        public Builder owner(Supplier<PotentialOwner> owner) { this.owner = owner; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public OwnerSelectButton build() { return new OwnerSelectButton(this); }
    }

}
