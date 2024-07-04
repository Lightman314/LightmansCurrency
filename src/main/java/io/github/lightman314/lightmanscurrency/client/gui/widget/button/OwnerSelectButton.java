package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OwnerSelectButton extends EasyButton implements ITooltipWidget {

    public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"textures/gui/buttons.png");

    private final Supplier<OwnerData> currentOwner;
    private final Supplier<PotentialOwner> ownerSupplier;
    private final Supplier<Boolean> parentVisible;
    public PotentialOwner getOwner() { return this.ownerSupplier.get(); }

    public static final int HEIGHT = 20;

    public OwnerSelectButton(ScreenPosition pos, int width, @Nonnull Runnable press, @Nonnull Supplier<OwnerData> currentOwner, @Nonnull Supplier<PotentialOwner> ownerSupplier, @Nonnull Supplier<Boolean> parentVisible) {
        super(ScreenArea.of(pos,width,HEIGHT), press);
        this.currentOwner = currentOwner;
        this.ownerSupplier = ownerSupplier;
        this.parentVisible = parentVisible;
    }

    @Override
    public OwnerSelectButton withAddons(WidgetAddon... addons) {
        this.withAddonsInternal(addons);
        return this;
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
        gui.blitBackgroundOfSize(GUI_TEXTURE,0,0, this.width, this.height,0,0,256,20,2);

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

}
