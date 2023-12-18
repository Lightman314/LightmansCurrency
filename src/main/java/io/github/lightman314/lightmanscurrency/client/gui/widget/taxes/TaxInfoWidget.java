package io.github.lightman314.lightmanscurrency.client.gui.widget.taxes;

import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class TaxInfoWidget extends EasyWidgetWithChildren {

    public static final Sprite SPRITE_IGNORE_TC = Sprite.SimpleSprite(IconAndButtonUtil.WIDGET_TEXTURE, 10, 0, 10, 10);
    public static final Sprite SPRITE_STOP_IGNORING_TC = Sprite.SimpleSprite(IconAndButtonUtil.WIDGET_TEXTURE, 0, 0, 10, 10);

    public static final int HEIGHT = 30;
    public static final int WIDTH = 176;

    private final Supplier<TaxEntry> entrySource;
    private final ITaxInfoInteractable parent;

    public TaxInfoWidget(@Nonnull ScreenPosition pos, @Nonnull Supplier<TaxEntry> entry, @Nonnull ITaxInfoInteractable parent) { this(pos.x, pos.y, entry, parent); }
    public TaxInfoWidget(int x, int y, @Nonnull Supplier<TaxEntry> entry, @Nonnull ITaxInfoInteractable parent) {
        super(x, y, WIDTH, HEIGHT);
        this.entrySource = entry;
        this.parent = parent;
    }

    @Override
    public TaxInfoWidget withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

    @Override
    protected void renderWidget(@Nonnull EasyGuiGraphics gui) {

        TaxEntry entry = this.entrySource.get();
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
            gui.drawString(EasyText.translatable("gui.lightmanscurrency.tax_collector.tax_rate", entry.getTaxRate()), this.parent.canPlayerForceIgnore() ? 12 : 0, 15, 0x404040);
        }

    }

    @Override
    public void addChildren() {
        this.addChild(new EasyTextButton(this.getPosition().offset(80, 10), 60, 16, EasyText.translatable("gui.lightmanscurrency.tax_collector.taxable.accept_collector"), this::AcceptTaxCollector)
                .withAddons(EasyAddonHelper.visibleCheck(this::shouldAcceptBeVisible)));
        this.addChild(new PlainButton(this.getPosition().offset(0, 13), this::ToggleIgnoreState, this::getForceIgnoreSprite)
                .withAddons(EasyAddonHelper.visibleCheck(() -> this.parent.canPlayerForceIgnore() && this.entrySource.get() != null),
                        EasyAddonHelper.tooltip(this::getForceIgnoreTooltip)));

    }

    private boolean shouldAcceptBeVisible() {
        TaxEntry entry = this.entrySource.get();
        TraderData trader = this.parent.getTrader();
        if(entry != null && trader != null)
            return entry.IsInArea(trader) && !entry.ShouldTax(trader);
        return false;
    }

    private boolean isCurrentlyIgnored() {
        TaxEntry entry = this.entrySource.get();
        TraderData trader = this.parent.getTrader();
        if(entry != null && trader != null)
            return trader.ShouldIgnoreTaxEntryOnly(entry);
        return false;
    }

    private Sprite getForceIgnoreSprite() { return this.isCurrentlyIgnored() ? SPRITE_STOP_IGNORING_TC : SPRITE_IGNORE_TC; }

    private Component getForceIgnoreTooltip() { return this.isCurrentlyIgnored() ? EasyText.translatable("tooltip.lightmanscurrency.tax_collector.taxable.pardon_ignored_collector") : EasyText.translatable("tooltip.lightmanscurrency.tax_collector.taxable.force_ignore_collector"); }

    private void AcceptTaxCollector(EasyButton button)
    {
        TaxEntry entry = this.entrySource.get();
        if(entry != null)
            this.parent.AcceptTaxCollector(entry.getID());
    }

    private void ToggleIgnoreState(EasyButton button)
    {
        TaxEntry entry = this.entrySource.get();
        TraderData trader = this.parent.getTrader();
        if(entry != null && trader != null)
        {
            if(trader.ShouldIgnoreTaxEntryOnly(entry))
                this.parent.PardonIgnoredTaxCollector(entry.getID());
            else
                this.parent.ForceIgnoreTaxCollector(entry.getID());
        }
    }

}
