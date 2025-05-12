package io.github.lightman314.lightmanscurrency.client.gui.widget;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.SlotType;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModelVariantButton extends EasyButton implements ITooltipWidget {

    private final IEasyScreen screen;
    private final Supplier<IVariantBlock> targetSource;
    private final Supplier<Pair<ResourceLocation,ModelVariant>> variantSource;
    private final Supplier<ResourceLocation> selectedVariant;
    private final Supplier<ResourceLocation> viewingVariant;
    protected ModelVariantButton(Builder builder) {
        super(builder);
        this.screen = builder.screen;
        this.targetSource = builder.target;
        this.variantSource = builder.source;
        this.selectedVariant = builder.selectedVariant;
        this.viewingVariant = builder.viewingVariant;
    }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) {
        Pair<ResourceLocation,ModelVariant> entry = this.variantSource.get();
        ScreenPosition slotOffset = ScreenPosition.of(this.getX() - this.screen.getGuiLeft() + 1,this.getY() - this.screen.getGuiTop() + 1);
        //Update ability to click on this button based on whether the entry exists or not
        this.setActive(entry != null);
        if(entry == null)
        {
            gui.renderSlot(this.screen,slotOffset);
            return;
        }
        ResourceLocation selected = this.selectedVariant.get();
        ResourceLocation viewed = this.viewingVariant.get();
        //Render the Slot
        SlotType type = EasyGuiGraphics.SLOT_NORMAL;
        if(Objects.equals(selected,entry.getFirst()))
            type = EasyGuiGraphics.SLOT_GREEN;
        else if(Objects.equals(viewed,entry.getFirst()))
            type = EasyGuiGraphics.SLOT_YELLOW;

        gui.renderSlot(this.screen,slotOffset,type);
        //Render the Icon
        if(entry.getSecond().getItemIcon() != null)
            gui.renderItem(entry.getSecond().getItemIcon(),1,1); //Render the actual item for the "default" model variant
        else
            gui.renderItemModel(entry.getSecond().getItem(this.targetSource.get()),1,1);
        if(this.isMouseOver(gui.mousePos))
            gui.renderSlotHighlight(1,1);
    }

    public static Builder builder() { return new Builder(); }

    @Override
    public List<Component> getTooltipText() {
        Pair<ResourceLocation,ModelVariant> entry = this.variantSource.get();
        if(entry == null)
            return new ArrayList<>();
        List<Component> tooltip = new ArrayList<>(TooltipHelper.splitTooltips(entry.getSecond().getName()));
        if(entry.getFirst() != null && Minecraft.getInstance().options.advancedItemTooltips)
            tooltip.add(EasyText.literal(entry.getFirst().toString()).withStyle(ChatFormatting.GRAY));
        return tooltip;
    }

    public static class Builder extends EasyButtonBuilder<Builder>
    {

        private Builder() { super(18,18); }

        @Override
        protected Builder getSelf() { return this; }

        private IEasyScreen screen;
        private Supplier<Pair<ResourceLocation,ModelVariant>> source = () -> null;
        private Supplier<IVariantBlock> target = () -> null;
        private Supplier<ResourceLocation> selectedVariant = () -> null;
        private Supplier<ResourceLocation> viewingVariant = () -> null;

        public Builder screen(IEasyScreen screen) { this.screen = screen; return this; }
        public Builder target(Supplier<IVariantBlock> target) { this.target = target; return this; }
        public Builder source(Supplier<Pair<ResourceLocation,ModelVariant>> source) { this.source = source; return this; }
        public Builder selected(Supplier<ResourceLocation> selectedVariant) { this.selectedVariant = selectedVariant; return this; }
        public Builder viewing(Supplier<ResourceLocation> viewingVariant) { this.viewingVariant = viewingVariant; return this; }

        public ModelVariantButton build() { return new ModelVariantButton(this); }

    }


}