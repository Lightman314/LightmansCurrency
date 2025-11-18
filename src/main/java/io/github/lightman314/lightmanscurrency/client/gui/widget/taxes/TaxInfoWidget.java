package io.github.lightman314.lightmanscurrency.client.gui.widget.taxes;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TaxInfoWidget extends EasyWidgetWithChildren {

    public static final FixedSizeSprite SPRITE_IGNORE_TC = SpriteUtil.BUTTON_GREEN_X;
    public static final FixedSizeSprite SPRITE_STOP_IGNORING_TC = SpriteUtil.BUTTON_RED_X;

    public static final int HEIGHT = 30;
    public static final int WIDTH = 176;

    private final Supplier<ITaxCollector> entrySource;
    private final ITaxInfoInteractable parent;

    private TaxInfoWidget(Builder builder) {
        super(builder);
        this.entrySource = builder.entry;
        this.parent = Objects.requireNonNull(builder.parent);
    }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) {
        ITaxCollector entry = this.entrySource.get();
        TraderData trader = this.parent.getTrader();
        if(entry != null && trader != null)
        {
            //Get status color
            int statusColor = 0x404040;
            if(trader.ShouldIgnoreTaxEntryOnly(entry))
                statusColor = 0xFF0000;
            else if(trader.ShouldIgnoreAllTaxes())
                statusColor = 0xFF7F00;
            else if(entry.ShouldTax(trader))
                statusColor = 0x00FF00;
            gui.drawString(entry.getName(), 0, 0, statusColor);
            gui.drawString(LCText.GUI_TAX_COLLECTOR_TAX_RATE.get(entry.getTaxRate()), this.parent.canPlayerForceIgnore() ? 12 : 0, 15, 0x404040);
        }

    }

    @Override
    public void addChildren(ScreenArea area) {
        this.addChild(EasyTextButton.builder()
                .position(area.pos.offset(90,1))
                .width(60)
                .text(LCText.GUI_TAX_COLLECTOR_TAXABLE_ACCEPT_COLLECTOR)
                .pressAction(this::AcceptTaxCollector)
                .addon(EasyAddonHelper.visibleCheck(this::shouldAcceptBeVisible))
                .build());
        this.addChild(PlainButton.builder()
                .position(area.pos.offset(0,13))
                .pressAction(this::ToggleIgnoreState)
                .sprite(this::getForceIgnoreSprite)
                .addon(EasyAddonHelper.visibleCheck(() -> this.parent.canPlayerForceIgnore() && this.entrySource.get() != null))
                .addon(EasyAddonHelper.tooltip(this::getForceIgnoreTooltip))
                .build());

    }

    private boolean shouldAcceptBeVisible() {
        ITaxCollector entry = this.entrySource.get();
        TraderData trader = this.parent.getTrader();
        if(entry != null && trader != null)
            return entry.IsInArea(trader) && !entry.ShouldTax(trader);
        return false;
    }

    private boolean isCurrentlyIgnored() {
        ITaxCollector entry = this.entrySource.get();
        TraderData trader = this.parent.getTrader();
        if(entry != null && trader != null)
            return trader.ShouldIgnoreTaxEntryOnly(entry);
        return false;
    }

    private FixedSizeSprite getForceIgnoreSprite() { return this.isCurrentlyIgnored() ? SPRITE_STOP_IGNORING_TC : SPRITE_IGNORE_TC; }

    private Component getForceIgnoreTooltip() { return this.isCurrentlyIgnored() ? LCText.TOOLTIP_TAX_COLLECTOR_TAXABLE_PARDON_IGNORED.get() : LCText.TOOLTIP_TAX_COLLECTOR_TAXABLE_FORCE_IGNORE.get(); }

    private void AcceptTaxCollector(EasyButton button)
    {
        ITaxCollector entry = this.entrySource.get();
        if(entry != null)
            this.parent.AcceptTaxCollector(entry.getID());
    }

    private void ToggleIgnoreState(EasyButton button)
    {
        ITaxCollector entry = this.entrySource.get();
        TraderData trader = this.parent.getTrader();
        if(entry != null && trader != null)
        {
            if(trader.ShouldIgnoreTaxEntryOnly(entry))
                this.parent.PardonIgnoredTaxCollector(entry.getID());
            else
                this.parent.ForceIgnoreTaxCollector(entry.getID());
        }
    }

    public static Builder builder() { return new Builder(); }

    @FieldsAreNonnullByDefault
    public static class Builder extends EasyBuilder<Builder>
    {
        private Builder() { super(WIDTH,HEIGHT); }
        @Override
        protected Builder getSelf() { return this; }

        Supplier<ITaxCollector> entry = () -> null;
        @Nullable
        ITaxInfoInteractable parent = null;

        public Builder entry(Supplier<ITaxCollector> entry) { this.entry = entry; return this; }
        public Builder parent(ITaxInfoInteractable parent) { this.parent = parent; return this; }

        public TaxInfoWidget build() { return new TaxInfoWidget(this); }

    }

}
